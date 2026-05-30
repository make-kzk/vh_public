package jobs.vibehunt.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object OAuthStatesTable : Table("oauth_states") {
    val state = varchar("state", 64)
    val codeVerifier = varchar("code_verifier", 128)
    val provider = varchar("provider", 20)
    val redirectUri = text("redirect_uri")
    val expiresAt = timestampWithTimeZone("expires_at")

    override val primaryKey = PrimaryKey(state)
}
