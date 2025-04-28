package nrkt.oidc.dao

import nrkt.oidc.dao.entity.UserEntity
import nrkt.oidc.dao.tables.User
import nrkt.oidc.domain.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class UserDao {

    fun selectByNameAndPassword(
        name: String,
        password: String,
    ): UserEntity? {
        return transaction {
            User.selectAll().where { (User.name eq name) and (User.password eq password) }
                .map { row ->
                    UserEntity(
                        id = UserId(row[User.id]),
                        name = row[User.name],
                        email = row[User.email],
                        password = row[User.password],
                    )
                }.singleOrNull()
        }
    }

    fun selectByUserId(
        id: UserId,
    ): UserEntity? {
        return transaction {
            User.selectAll().where { User.id eq id.value }
                .map { row ->
                    UserEntity(
                        id = UserId(row[User.id]),
                        name = row[User.name],
                        email = row[User.email],
                        password = row[User.password],
                    )
                }.singleOrNull()
        }
    }

    fun insert(
        id: UserId,
        name: String,
        email: String,
        password: String
    ): UserEntity {
        return transaction {
            User.insert {
                it[User.id] = id.value
                it[User.name] = name
                it[User.email] = email
                it[User.password] = password
            }
            UserEntity(
                id = id,
                name = name,
                email = email,
                password = password,
            )
        }
    }
}