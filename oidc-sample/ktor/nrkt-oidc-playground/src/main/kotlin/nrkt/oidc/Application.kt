package nrkt.oidc

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import nrkt.oidc.dao.RelyingPartyDao
import nrkt.oidc.dao.UserDao
import nrkt.oidc.dao.tables.AuthCode
import nrkt.oidc.dao.tables.RelyingParty
import nrkt.oidc.dao.tables.Token
import nrkt.oidc.dao.tables.User
import nrkt.oidc.domain.ClientId
import nrkt.oidc.domain.UserId
import nrkt.oidc.domain.UserSession
import nrkt.oidc.routing.authRoutes
import nrkt.oidc.routing.loginRoutes
import nrkt.oidc.routing.tokenRoutes
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.configureDatabase() {
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", // in-memory database
        driver = "org.h2.Driver",
        user = "root",
        password = ""
    )

    // initialize database
    transaction {
        // create tables
        create(RelyingParty)
        create(User)
        create(AuthCode)
        create(Token)

        // insert test data
        RelyingPartyDao().insert(
            id = "1",
            name = "Test Relying Party",
            clientId = ClientId("client_id"),
            clientSecret = "client_secret",
            redirectUri = "http://localhost:8080/callback",
        )
        UserDao().insert(
            id = UserId("1"),
            name = "user",
            password = "password",
            email = "email",
        )
    }
}

fun Application.module() {
    configureDatabase()
    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.httpOnly = true
            cookie.secure = false // local
        }
    }
    install(Resources)
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        loginRoutes()

        route("/oauth2") {
            authRoutes()
            tokenRoutes()
        }
    }
}
