package nrkt.oidc.dao.tables

import org.jetbrains.exposed.sql.Table

object User : Table() {
    val id = varchar("id", 255)
    val name = varchar("name", 255)
    val password = varchar("password", 255)
    val email = varchar("email", 255)

    override val primaryKey = PrimaryKey(id)
}