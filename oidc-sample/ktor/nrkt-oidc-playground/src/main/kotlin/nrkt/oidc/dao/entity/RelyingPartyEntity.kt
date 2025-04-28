package nrkt.oidc.dao.entity

import nrkt.oidc.domain.ClientId

data class RelyingPartyEntity(
    val id: String,
    val name: String,
    val clientId: ClientId,
    val clientSecret: String,
    val redirectUri: String,
)