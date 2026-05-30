package jobs.vibehunt.db

import jobs.vibehunt.auth.AuthUserDto
import jobs.vibehunt.auth.OAuthProvider
import jobs.vibehunt.auth.UserRole
import jobs.vibehunt.db.tables.UsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.util.UUID

class UserRepository {
    fun findByOAuth(provider: OAuthProvider, subject: String): AuthUserDto? =
        transaction {
            UsersTable
                .selectAll()
                .where {
                    (UsersTable.oauthProvider eq provider.name.lowercase()) and
                        (UsersTable.oauthSubject eq subject)
                }
                .firstOrNull()
                ?.toDto()
        }

    fun findById(id: UUID): AuthUserDto? =
        transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.id eq id }
                .firstOrNull()
                ?.toDto()
        }

    fun create(
        email: String,
        displayName: String?,
        provider: OAuthProvider,
        subject: String,
    ): AuthUserDto =
        transaction {
            val now = OffsetDateTime.now()
            val id =
                UsersTable.insert {
                    it[UsersTable.email] = email
                    it[UsersTable.displayName] = displayName
                    it[UsersTable.role] = null
                    it[UsersTable.oauthProvider] = provider.name.lowercase()
                    it[UsersTable.oauthSubject] = subject
                    it[UsersTable.createdAt] = now
                }[UsersTable.id].value
            AuthUserDto(
                id = id.toString(),
                email = email,
                displayName = displayName,
                role = null,
            )
        }

    fun setRole(userId: UUID, role: UserRole): AuthUserDto? =
        transaction {
            val updated =
                UsersTable.update({ UsersTable.id eq userId }) {
                    it[UsersTable.role] = role.name
                }
            if (updated == 0) return@transaction null
            findById(userId)
        }

    private fun ResultRow.toDto(): AuthUserDto {
        val roleValue = this[UsersTable.role]
        return AuthUserDto(
            id = this[UsersTable.id].value.toString(),
            email = this[UsersTable.email],
            displayName = this[UsersTable.displayName],
            role = roleValue?.let { UserRole.valueOf(it) },
        )
    }
}
