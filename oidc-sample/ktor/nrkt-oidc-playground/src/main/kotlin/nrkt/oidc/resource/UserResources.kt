package nrkt.oidc.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Resource("/user")
class UserResources {
    @Serializable
    @Resource("")
    class GetInfo(
        val parent: UserResources = UserResources(),
    )
}