package nrkt.oidc.data

data class UserSession(
    val userId: String,
    val username: String,
    val csrfToken: String? = null,
)
