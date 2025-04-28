package nrkt.oidc.routing

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import nrkt.oidc.domain.UserSession
import nrkt.oidc.resource.LoginResources
import nrkt.oidc.service.LoginService

fun Route.loginRoutes() {
    get<LoginResources> {
        call.respondText("Login base path accessed")
    }
    val loginService = LoginService()
    get<LoginResources.Get> { param: LoginResources.Get ->
        val session = call.sessions.get("USER_SESSION") as? UserSession
        if (session != null) {
            call.respondText("already logged in, user: ${session.username}")
        } else {
            val from = param.from
            call.respondHtml {
                body {
                    form(
                        action = if (from != null) application.href(LoginResources.Post(from = from))
                        else application.href(LoginResources.Post),
                        method = FormMethod.post,
                    ) {
                        label {
                            +"Username:"
                            textInput(name = "name")
                        }
                        label {
                            +"Password:"
                            textInput(name = "password")
                        }
                        button(type = ButtonType.submit) {
                            +"Login"
                        }
                    }
                }
            }
        }
    }

    post<LoginResources.Post> { param: LoginResources.Post ->
        loginService.login(call, param)
    }
}

