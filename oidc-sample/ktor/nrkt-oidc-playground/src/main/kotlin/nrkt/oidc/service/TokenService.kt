package nrkt.oidc.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import nrkt.oidc.dao.AuthCodeDao
import nrkt.oidc.dao.RelyingPartyDao
import nrkt.oidc.dao.entity.AuthCodeEntity
import nrkt.oidc.data.resource.TokenResources
import java.util.*

class TokenService {
    val relyingPartyDao = RelyingPartyDao()
    val authCodeDao = AuthCodeDao()

    suspend fun generateTokenByAuthorizationCode(call: ApplicationCall, request: TokenResources.Post.Request) {
        // check clientId and clientSecret
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
        val idToken = generateToken(userId = authCodeEntity.userId)
        // TODO: save token
        call.respondText("Token generated: $idToken", status = HttpStatusCode.OK)
    }

    private fun validClientIdAndSecret(clientId: String, clientSecret: String): Boolean {
        return relyingPartyDao.selectByClientIdAndClientSecret(clientId = clientId, clientSecret = clientSecret) != null
    }

    private fun validAuthCode(authCodeEntity: AuthCodeEntity): Boolean {
        if (authCodeEntity.used) {
            return false
        }
        return authCodeEntity.expiresAt >= System.currentTimeMillis()
    }

    private fun generateToken(userId: String): String {
        val secret = "your-256-bit-secret"
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withIssuer("my-issuer")
            .withSubject(userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600 * 1000))
            .sign(algorithm)
    }
}