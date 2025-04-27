package nrkt.oidc.dao.entity

data class RelyingPartyEntity(
    val id: String,
    val name: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)