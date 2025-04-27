package nrkt.oidc.data.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import nrkt.oidc.data.ResponseType

@Serializable
@Resource("/authorize")
class AuthorizeResources {
    @Serializable
    @Resource("")
    class Get(
        val parent: AuthorizeResources = AuthorizeResources(),
        val responseType: ResponseType,
        val clientId: String,
        val redirectUri: String,
    )

    @Serializable
    @Resource("")
    class Post(
        val parent: AuthorizeResources = AuthorizeResources(),
        val responseType: ResponseType,
        val clientId: String,
        val redirectUri: String,
    )
}
