package nrkt.oidc.routing

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import nrkt.oidc.resource.TokenResources
import nrkt.oidc.service.TokenService

fun Route.tokenRoutes() {
    val tokenService = TokenService()

    get<TokenResources> {
        call.respondText("Token base path accessed")
    }

    post<TokenResources.Post> {
        val request = call.receive<TokenResources.Post.Request>()
        println("Received token request: $request")
        tokenService.generateTokenByAuthorizationCode(call = call, request = request)
    }
}