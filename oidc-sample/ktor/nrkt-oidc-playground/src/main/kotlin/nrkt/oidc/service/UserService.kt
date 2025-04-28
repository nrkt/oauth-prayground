package nrkt.oidc.service

import com.auth0.jwt.JWT
import io.ktor.server.plugins.*
import nrkt.oidc.dao.TokenDao
import nrkt.oidc.dao.UserDao
import nrkt.oidc.dao.entity.UserEntity
import nrkt.oidc.domain.AccessToken
import java.time.Instant

class UserService {
    val tokenDao = TokenDao()
    val userDao = UserDao()

    fun getUserInfo(accessToken: AccessToken): UserEntity {
        val tokenEntity = tokenDao.getByAccessToken(accessToken)
            ?: throw BadRequestException("Invalid access token")

        val decodedAccessToken = JWT.decode(tokenEntity.accessToken.value)
        if (decodedAccessToken.expiresAt.toInstant() <= Instant.now()) {
            throw BadRequestException("Access token expired")
        }
        // TODO: check scope
        val userId = tokenEntity.userId
        val userEntity = userDao.selectByUserId(userId)
            ?: throw NotFoundException("User not found")
        return userEntity
    }
}