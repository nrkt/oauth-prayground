package nrkt.oidc.dao.tables

import org.jetbrains.exposed.sql.Table

object AuthCode : Table() {
    val code = varchar("code", 255)
    val userId = varchar("user_id", 255)
    val clientId = varchar("client_id", 255)
    val used = integer("used").default(0)
    val expiresAt = long("expires_at")

    override val primaryKey = PrimaryKey(code)
}