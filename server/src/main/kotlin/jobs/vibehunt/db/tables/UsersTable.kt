package jobs.vibehunt.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val displayName = varchar("display_name", 255).nullable()
    val role = varchar("role", 20).nullable()
    val oauthProvider = varchar("oauth_provider", 20)
    val oauthSubject = varchar("oauth_subject", 255)
    val createdAt = timestampWithTimeZone("created_at")
}
