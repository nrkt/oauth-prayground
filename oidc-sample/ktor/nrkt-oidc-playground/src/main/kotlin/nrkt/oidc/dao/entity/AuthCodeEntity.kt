package nrkt.oidc.dao.entity

import nrkt.oidc.domain.AuthorizationCode
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.UserId
import java.time.Instant

data class AuthCodeEntity(
    val code: AuthorizationCode,
    val userId: UserId,
    val clientId: ClientId,
    val used: Boolean,
    val expiresAt: Instant,
)
