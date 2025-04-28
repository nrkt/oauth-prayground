package nrkt.oidc.domain

data class UserSession(
    val userId: UserId,
    val username: String,
    val csrfToken: String? = null,
)
