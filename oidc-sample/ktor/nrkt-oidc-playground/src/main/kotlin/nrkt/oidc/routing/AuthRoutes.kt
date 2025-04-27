package nrkt.oidc.routing

import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import nrkt.oidc.data.resource.AuthorizeResources
import nrkt.oidc.service.AuthorizeService

fun Route.authRoutes() {
    val authorizeService = AuthorizeService()

    get<AuthorizeResources.Get> { param: AuthorizeResources.Get ->
        authorizeService.confirmAuthorize(call = call, param = param)
    }

    post<AuthorizeResources.Post> { param: AuthorizeResources.Post ->
        authorizeService.getAuthorizationCode(call = call, param = param)
    }
}
