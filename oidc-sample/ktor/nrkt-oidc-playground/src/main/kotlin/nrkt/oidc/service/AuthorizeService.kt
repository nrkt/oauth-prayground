package nrkt.oidc.service

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import nrkt.oidc.dao.AuthCodeDao
import nrkt.oidc.dao.RelyingPartyDao
import nrkt.oidc.domain.AuthorizationCode
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.UserSession
import nrkt.oidc.resource.AuthorizeResources
import nrkt.oidc.resource.LoginResources
import java.time.Instant
import java.util.*

class AuthorizeService {

    val relyingPartyDao = RelyingPartyDao()
    val authCodeDao = AuthCodeDao()

    suspend fun confirmAuthorize(call: ApplicationCall, param: AuthorizeResources.Get) {
        val userSession = call.sessions.get<UserSession>()
        if (userSession == null) {
            val originalUrl =
                "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}${call.request.uri}"
            val redirectUri = call.application.href(LoginResources.Get(from = originalUrl))
            call.respondRedirect(redirectUri)
        } else {
            val csrfToken = UUID.randomUUID().toString()
            val newSession = userSession.copy(csrfToken = csrfToken)

            call.sessions.set(newSession) // set csrfToken in the session

            val actionUrl = "/oauth2" + call.application.href(
                AuthorizeResources.Post(
                    responseType = param.responseType,
                    clientId = param.clientId,
                    redirectUri = param.redirectUri,
                )
            )
            val clientId = param.clientId
            call.respondHtml {
                head {
                    title { +"Authorization Request" }
                }
                body {
                    h1 { +"Authorization Request" }
                    p { +"User: ${userSession.username}" }
                    p { +"Client ID: $clientId" }
                    p { +"Do you approve or reject the authorization request?" }
                    form(action = actionUrl, method = FormMethod.post) {
                        hiddenInput(name = "csrf_token") { value = csrfToken }
                        button(name = "action") {
                            value = AuthorizeAction.APPROVE.value
                            +"Approve"
                        }
                        button(name = "action") {
                            value = AuthorizeAction.REJECT.value
                            +"Reject"
                        }
                    }
                }
            }
        }
    }

    suspend fun getAuthorizationCode(call: ApplicationCall, param: AuthorizeResources.Post) {
        validateClientRedirectUri(
            clientId = param.clientId,
            redirectUri = param.redirectUri,
        )

        val formParameters = call.receiveParameters()
        val action = formParameters["action"]?.let {
            AuthorizeAction.fromValue(it)
        } ?: return call.respondText(
            text = "Action not found",
            status = HttpStatusCode.BadRequest,
        )
        val csrfToken = formParameters["csrf_token"]
            ?: return call.respondText(
                text = "CSRF Token not found",
                status = HttpStatusCode.BadRequest,
            )

        val originalUrl =
            "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}${call.request.uri}"
        val redirectLoginUri = call.application.href(LoginResources.Get(from = originalUrl))
        val userSession = call.sessions.get<UserSession>()
            ?: return call.respondRedirect(redirectLoginUri)

        if (userSession.csrfToken != csrfToken) {
            return call.respondText("Invalid csrf token", status = HttpStatusCode.BadRequest)
        }

        when (action) {
            AuthorizeAction.APPROVE -> {
                val authCode = generateAuthorizationCode()
                authCodeDao.insert(
                    code = authCode,
                    clientId = param.clientId,
                    userId = userSession.userId,
                    expiresAt = Instant.now().plusSeconds(3600),
                )

                // append code to redirect_uri
                val redirectUrl = buildString {
                    append(param.redirectUri)
                    append(if (param.redirectUri.contains('?')) "&" else "?")
                    append("code=${authCode.code}")
                }

                call.respondRedirect(redirectUrl)
            }

            AuthorizeAction.REJECT -> {
                // append error parameter to redirect_uri
                val redirectUrl = buildString {
                    append(param.redirectUri)
                    append(if (param.redirectUri.contains('?')) "&" else "?")
                    append("error=access_denied")
                }

                call.respondRedirect(redirectUrl)
            }
        }
    }

    private fun validateClientRedirectUri(clientId: ClientId, redirectUri: String) {
        relyingPartyDao.selectByClientId(clientId)?.let {
            if (it.redirectUri == redirectUri) {
                return
            } else {
                throw BadRequestException("Invalid client_id or redirect_uri")
            }
        } ?: throw BadRequestException("Invalid client_id or redirect_uri")
    }

    private fun generateAuthorizationCode(): AuthorizationCode {
        val code = (('a'..'z') + ('0'..'9')).shuffled().take(16).joinToString("")
        return AuthorizationCode(code = code)
    }

    companion object {
        enum class AuthorizeAction(
            val value: String,
        ) {
            APPROVE("approve"),
            REJECT("reject"),
            ;

            companion object {
                fun fromValue(value: String): AuthorizeAction? {
                    return entries.find { it.value == value }
                }
            }
        }
    }
}