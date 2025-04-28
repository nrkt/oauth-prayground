package nrkt.oidc.routing

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import nrkt.oidc.resource.TokenResources
import nrkt.oidc.service.TokenService

fun Route.tokenRoutes() {
    val tokenService = TokenService()

    post<TokenResources.Post> { param: TokenResources.Post ->
        val request = call.receive<TokenResources.Post.Request>()
        tokenService.generateTokenByAuthorizationCode(call = call, request = request)
    }
}