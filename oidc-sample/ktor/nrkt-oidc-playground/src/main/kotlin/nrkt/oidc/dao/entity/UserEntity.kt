package nrkt.oidc.dao.entity

data class UserEntity(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
)
