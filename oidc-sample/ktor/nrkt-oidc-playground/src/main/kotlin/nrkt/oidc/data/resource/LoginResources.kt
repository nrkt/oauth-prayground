package nrkt.oidc.data.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/login")
class LoginResources {
    @Serializable
    @Resource("")
    class Get(
        val parent: LoginResources = LoginResources(),
        val from: String? = null,
    )

    @Serializable
    @Resource("")
    class Post(
        val parent: LoginResources = LoginResources(),
        val from: String? = null
    )
}