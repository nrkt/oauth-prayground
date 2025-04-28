package nrkt.oidc.service

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import nrkt.oidc.dao.UserDao
import nrkt.oidc.domain.UserLoginRequest
import nrkt.oidc.domain.UserSession
import nrkt.oidc.resource.LoginResources

class LoginService {
    val userDao = UserDao()
    suspend fun login(call: ApplicationCall, param: LoginResources.Post) {
        val formParameters = call.receiveParameters()
        val name = formParameters["name"] ?: ""
        val password = formParameters["password"] ?: ""

        val request = UserLoginRequest(name = name, password = password)

        userDao.selectByNameAndPassword(name = request.name, password = request.password)?.let { user ->
            call.sessions.set(UserSession(userId = user.id, username = user.name))
            val redirectTo = param.from ?: "/"
            call.respondRedirect(redirectTo)
        } ?: call.respondText("Login failed", status = HttpStatusCode.Unauthorized)
    }
}