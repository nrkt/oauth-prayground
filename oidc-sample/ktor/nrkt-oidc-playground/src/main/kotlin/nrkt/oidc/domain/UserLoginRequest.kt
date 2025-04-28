package nrkt.oidc.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(
    val name: String,
    val password: String,
)
