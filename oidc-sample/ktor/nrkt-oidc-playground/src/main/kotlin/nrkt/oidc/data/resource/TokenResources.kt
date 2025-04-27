package nrkt.oidc.data.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable

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
            val clientId: String,
            val clientSecret: String,
            val code: String,
        )
    }
}