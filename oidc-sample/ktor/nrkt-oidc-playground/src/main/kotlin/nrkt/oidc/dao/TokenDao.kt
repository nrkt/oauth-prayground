package nrkt.oidc.dao

import nrkt.oidc.dao.entity.TokenEntity
import nrkt.oidc.dao.tables.Token
import nrkt.oidc.domain.AccessToken
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.RefreshToken
import nrkt.oidc.domain.UserId
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class TokenDao {
    fun saveAccessToken(
        accessToken: AccessToken,
        refreshToken: RefreshToken,
        userId: UserId,
        clientId: ClientId,
        expiresAt: Instant,
    ) {
        return transaction {
            Token.insert {
                it[access_token] = accessToken.value
                it[refresh_token] = refreshToken.value
                it[Token.userId] = userId.value
                it[Token.clientId] = clientId.value
                it[Token.expiresAt] = expiresAt.toEpochMilli()
            }
        }
    }

    fun getByAccessToken(accessToken: AccessToken): TokenEntity? {
        return transaction {
            Token.selectAll().where { Token.access_token eq accessToken.value }
                .map { row ->
                    TokenEntity(
                        accessToken = AccessToken(row[Token.access_token]),
                        refreshToken = RefreshToken(row[Token.refresh_token]),
                        userId = UserId(row[Token.userId]),
                        clientId = ClientId(row[Token.clientId]),
                        expiresAt = Instant.ofEpochMilli(row[Token.expiresAt])
                    )
                }.singleOrNull()
        }
    }
}