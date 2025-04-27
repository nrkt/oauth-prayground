package nrkt.oidc.data

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(
    val name: String,
    val password: String,
)
