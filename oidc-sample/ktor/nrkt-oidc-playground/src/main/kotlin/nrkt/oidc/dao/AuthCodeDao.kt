package nrkt.oidc.dao

import nrkt.oidc.dao.entity.AuthCodeEntity
import nrkt.oidc.dao.tables.AuthCode
import nrkt.oidc.domain.AuthorizationCode
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.UserId
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AuthCodeDao {

    fun insert(
        code: AuthorizationCode,
        userId: UserId,
        clientId: ClientId,
        expiresAt: Long,
    ): AuthCodeEntity {
        return transaction {
            AuthCode.insert {
                it[AuthCode.code] = code.code
                it[AuthCode.userId] = userId.value
                it[AuthCode.clientId] = clientId.value
                it[AuthCode.expiresAt] = expiresAt
            }
            AuthCodeEntity(
                code = code,
                userId = userId,
                clientId = clientId,
                used = false,
                expiresAt = expiresAt,
            )
        }
    }

    fun selectByCode(code: String): AuthCodeEntity? {
        return transaction {
            AuthCode.selectAll().where { AuthCode.code eq code }
                .map { row ->
                    AuthCodeEntity(
                        code = AuthorizationCode(row[AuthCode.code]),
                        userId = UserId(row[AuthCode.userId]),
                        clientId = ClientId(row[AuthCode.clientId]),
                        used = row[AuthCode.used] == 1,
                        expiresAt = row[AuthCode.expiresAt],
                    )
                }.singleOrNull()
        }
    }

    fun updateUsed(code: String) {
        transaction {
            AuthCode.update({ AuthCode.code eq code }) {
                it[used] = 1
            }
        }
    }
}