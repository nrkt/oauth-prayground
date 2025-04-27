package nrkt.oidc.dao.entity

import nrkt.oidc.data.AuthorizationCode

data class AuthCodeEntity(
    val code: AuthorizationCode,
    val userId: String,
    val clientId: String,
    val used: Boolean,
    val expiresAt: Long,
)
