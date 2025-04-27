package nrkt.oidc.dao.tables

import org.jetbrains.exposed.sql.Table

object RelyingParty : Table() {
    val id = varchar("id", 255)
    val name = varchar("name", 255)
    val clientId = varchar("client_id", 255)
    val clientSecret = varchar("client_secret", 255)
    val redirectUri = varchar("redirect_uri", 255)

    override val primaryKey = PrimaryKey(id)
}
