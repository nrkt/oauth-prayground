package nrkt.oidc.dao

import nrkt.oidc.dao.entity.RelyingPartyEntity
import nrkt.oidc.dao.tables.RelyingParty
import nrkt.oidc.domain.ClientId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class RelyingPartyDao {

    fun selectByClientId(clientId: ClientId): RelyingPartyEntity? {
        return transaction {
            RelyingParty.selectAll().where { RelyingParty.clientId eq clientId.value }
                .map { row ->
                    RelyingPartyEntity(
                        id = row[RelyingParty.id],
                        name = row[RelyingParty.name],
                        clientId = ClientId(row[RelyingParty.clientId]),
                        clientSecret = row[RelyingParty.clientSecret],
                        redirectUri = row[RelyingParty.redirectUri]
                    )
                }.singleOrNull()
        }
    }

    fun selectByClientIdAndClientSecret(
        clientId: ClientId,
        clientSecret: String,
    ): RelyingPartyEntity? {
        return transaction {
            RelyingParty.selectAll()
                .where { (RelyingParty.clientId eq clientId.value) and (RelyingParty.clientSecret eq clientSecret) }
                .map { row ->
                    RelyingPartyEntity(
                        id = row[RelyingParty.id],
                        name = row[RelyingParty.name],
                        clientId = ClientId(row[RelyingParty.clientId]),
                        clientSecret = row[RelyingParty.clientSecret],
                        redirectUri = row[RelyingParty.redirectUri]
                    )
                }.singleOrNull()
        }
    }

    fun insert(
        id: String,
        name: String,
        clientId: ClientId,
        clientSecret: String,
        redirectUri: String
    ): RelyingPartyEntity {
        return transaction {
            RelyingParty.insert {
                it[RelyingParty.id] = id
                it[RelyingParty.name] = name
                it[RelyingParty.clientId] = clientId.value
                it[RelyingParty.clientSecret] = clientSecret
                it[RelyingParty.redirectUri] = redirectUri
            }
            RelyingPartyEntity(
                id = id,
                name = name,
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = redirectUri
            )
        }
    }
}