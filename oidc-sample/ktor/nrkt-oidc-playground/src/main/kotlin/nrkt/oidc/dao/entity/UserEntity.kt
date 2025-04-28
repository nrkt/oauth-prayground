package nrkt.oidc.dao.entity

import nrkt.oidc.domain.UserId

data class UserEntity(
    val id: UserId,
    val name: String,
    val email: String,
    val password: String,
)
