package nrkt.oidc.dao

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import nrkt.oidc.dao.entity.UserEntity
import nrkt.oidc.dao.tables.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserDaoTest {

    private val userDao = UserDao()

    @BeforeEach
    fun setup() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )

        transaction {
            drop(User)
            create(User)
        }
    }

    @Test
    fun `insert should add a user to the database`() {
        val user = userDao.insert(
            id = "1",
            name = "testuser",
            email = "test@example.com",
            password = "password123"
        )

        assertNotNull(user)
        assertEquals("1", user.id)
        assertEquals("testuser", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals("password123", user.password)
    }

    @Test
    fun `selectByNameAndPassword should return the correct user`() {
        userDao.insert(
            id = "1",
            name = "testuser",
            email = "test@example.com",
            password = "password123"
        )

        val user = userDao.selectByNameAndPassword(
            name = "testuser",
            password = "password123"
        )

        assertThat(user).isNotNull().isEqualTo(
            UserEntity(
                id = "1",
                name = "testuser",
                email = "test@example.com",
                password = "password123",
            )
        )
    }

    @Test
    fun `selectByNameAndPassword should return null for incorrect credentials`() {
        userDao.insert(
            id = "1",
            name = "testuser",
            email = "test@example.com",
            password = "password123"
        )

        assertThat(
            userDao.selectByNameAndPassword(
                name = "wronguser",
                password = "wrongpassword",
            )
        ).isNull()
    }
}