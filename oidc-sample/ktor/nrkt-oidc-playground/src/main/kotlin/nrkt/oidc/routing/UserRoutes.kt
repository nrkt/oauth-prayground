package nrkt.oidc.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nrkt.oidc.domain.AccessToken
import nrkt.oidc.resource.UserResources
import nrkt.oidc.service.UserService

fun Route.userRoutes() {
    val userService = UserService()

    get<UserResources.GetInfo> {
        val authHeader = call.request.headers["Authorization"]
        if (authHeader == null) {
            call.respondText("Authorization header is missing", status = HttpStatusCode.Unauthorized)
            return@get
        }
        val token = authHeader.removePrefix("Bearer ")
        val user = userService.getUserInfo(accessToken = AccessToken(token))
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK,
            text = """
                {
                    "userId": "${user.id}",
                    "username": "${user.name}"
                }
                """.trimIndent()
        )
    }
}