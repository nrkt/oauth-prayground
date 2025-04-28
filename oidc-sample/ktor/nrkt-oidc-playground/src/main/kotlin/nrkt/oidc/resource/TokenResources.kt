package nrkt.oidc.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import nrkt.oidc.domain.ClientId

@Serializable
@Resource("/token")
class TokenResources {

    @Serializable
    @Resource("")
    class Post(
        val parent: TokenResources = TokenResources(),
    ) {
        @Serializable
        data class Request(
            val clientId: ClientId,
            val clientSecret: String,
            val code: String,
        )
    }
}