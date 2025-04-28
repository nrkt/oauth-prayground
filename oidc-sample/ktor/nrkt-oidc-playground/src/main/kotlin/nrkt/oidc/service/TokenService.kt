package nrkt.oidc.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import nrkt.oidc.dao.AuthCodeDao
import nrkt.oidc.dao.RelyingPartyDao
import nrkt.oidc.dao.TokenDao
import nrkt.oidc.dao.entity.AuthCodeEntity
import nrkt.oidc.domain.*
import nrkt.oidc.resource.TokenResources
import java.time.Instant

class TokenService {
    val relyingPartyDao = RelyingPartyDao()
    val authCodeDao = AuthCodeDao()
    val tokenDao = TokenDao()

    suspend fun generateTokenByAuthorizationCode(call: ApplicationCall, request: TokenResources.Post.Request) {
        if (!validClientIdAndSecret(clientId = request.clientId, clientSecret = request.clientSecret)) {
            call.respondText("Invalid clientId or clientSecret", status = HttpStatusCode.BadRequest)
            return
        }
        val authCodeEntity = authCodeDao.selectByCode(code = request.code)
            ?: throw BadRequestException("Invalid auth code")
        if (!validAuthCode(authCodeEntity = authCodeEntity)) {
            call.respondText("Invalid auth code", status = HttpStatusCode.BadRequest)
            return
        }

        authCodeDao.updateUsed(code = request.code)
        val accessToken = generateAccessToken(userId = authCodeEntity.userId)
        val refreshToken = generateRefreshToken()
        val idToken = generateIdToken(userId = authCodeEntity.userId, clientId = request.clientId)
        tokenDao.saveAccessToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = authCodeEntity.userId,
            clientId = authCodeEntity.clientId,
            expiresAt = Instant.now().plusSeconds(3600)
        )
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK,
            text = """
                {
                    "access_token": "${accessToken.value}",
                    "token_type": "Bearer",
                    "refresh_token": "${refreshToken.value}", 
                    "id_token": "${idToken.value}",
                    "expires_in": 3600
                }
                """.trimIndent(),
        )
    }

    private fun validClientIdAndSecret(clientId: ClientId, clientSecret: String): Boolean {
        return relyingPartyDao.selectByClientIdAndClientSecret(
            clientId = clientId,
            clientSecret = clientSecret
        ) != null
    }

    private fun validAuthCode(authCodeEntity: AuthCodeEntity): Boolean {
        if (authCodeEntity.used) {
            return false
        }
        return authCodeEntity.expiresAt >= Instant.now()
    }

    private fun generateAccessToken(userId: UserId): AccessToken {
        val secret = "your-256-bit-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val iss = "http://0.0.0.0:8080"
        val aud = "http://0.0.0.0:8080/protected/userInfo"
        return AccessToken(
            JWT.create()
                .withIssuer(iss)
                .withSubject(userId.value)
                .withAudience(aud)
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .withIssuedAt(Instant.now())
                .sign(algorithm)
        )
    }

    private fun generateRefreshToken(): RefreshToken {
        val token = (('a'..'z') + ('0'..'9')).shuffled().take(32).joinToString("")
        return RefreshToken(token)
    }

    private fun generateIdToken(userId: UserId, clientId: ClientId): IdToken {
        val secret = "your-256-bit-secret"
        val algorithm = Algorithm.HMAC256(secret)
        val iss = "http://0.0.0.0:8080"
        return IdToken(
            JWT.create()
                .withIssuer(iss)
                .withSubject(userId.value)
                .withAudience(clientId.value)
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .withIssuedAt(Instant.now())
                .sign(algorithm)
        )
    }
}