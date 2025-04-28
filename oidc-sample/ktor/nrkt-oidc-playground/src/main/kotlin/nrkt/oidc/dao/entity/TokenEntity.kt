package nrkt.oidc.dao.entity

import nrkt.oidc.domain.AccessToken
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.RefreshToken
import nrkt.oidc.domain.UserId
import java.time.Instant

data class TokenEntity(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
    val userId: UserId,
    val clientId: ClientId,
    val expiresAt: Instant,
)