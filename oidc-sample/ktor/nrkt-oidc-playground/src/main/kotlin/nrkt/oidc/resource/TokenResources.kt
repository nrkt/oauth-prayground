package nrkt.oidc.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.ResponseType

@Serializable
@Resource("/token")
class TokenResources {

    @Serializable
    @Resource("")
    class Post(
        val parent: TokenResources = TokenResources(),
        val responseType: ResponseType,
    ) {
        init {
            require(responseType == ResponseType.token) {
                "Invalid response type: $responseType. Only 'token' is supported."
            }
        }

        @Serializable
        data class Request(
            val clientId: ClientId,
            val clientSecret: String,
            val code: String,
        )
    }
}