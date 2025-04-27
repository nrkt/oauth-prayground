package nrkt.oidc.dao

import nrkt.oidc.dao.entity.AuthCodeEntity
import nrkt.oidc.dao.tables.AuthCode
import nrkt.oidc.data.AuthorizationCode
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AuthCodeDao {

    fun insert(
        code: AuthorizationCode,
        userId: String,
        clientId: String,
        expiresAt: Long,
    ): AuthCodeEntity {
        return transaction {
            AuthCode.insert {
                it[AuthCode.code] = code.code
                it[AuthCode.userId] = userId
                it[AuthCode.clientId] = clientId
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
                        userId = row[AuthCode.userId],
                        clientId = row[AuthCode.clientId],
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