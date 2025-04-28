package nrkt.oidc.dao.tables

import org.jetbrains.exposed.sql.Table

object Token : Table() {
    val access_token = varchar("access_token", 255)
    val refresh_token = varchar("refresh_token", 255)
    val userId = varchar("user_id", 255)
    val clientId = varchar("client_id", 255)
    val expiresAt = long("expires_at")
}