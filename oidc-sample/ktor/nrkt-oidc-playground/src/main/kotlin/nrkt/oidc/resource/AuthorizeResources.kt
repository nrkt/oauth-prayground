package nrkt.oidc.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.ResponseType

@Serializable
@Resource("/authorize")
class AuthorizeResources {
    @Serializable
    @Resource("")
    class Get(
        val parent: AuthorizeResources = AuthorizeResources(),
        val responseType: ResponseType,
        val clientId: ClientId,
        val redirectUri: String,
    )

    @Serializable
    @Resource("")
    class Post(
        val parent: AuthorizeResources = AuthorizeResources(),
        val responseType: ResponseType,
        val clientId: ClientId,
        val redirectUri: String,
    )
}
