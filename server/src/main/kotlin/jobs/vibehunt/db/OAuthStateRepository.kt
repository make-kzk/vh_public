package jobs.vibehunt.db

import jobs.vibehunt.auth.OAuthProvider
import jobs.vibehunt.db.tables.OAuthStatesTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime

data class StoredOAuthState(
    val codeVerifier: String,
    val provider: OAuthProvider,
    val redirectUri: String,
)

class OAuthStateRepository {
    fun save(
        state: String,
        codeVerifier: String,
        provider: OAuthProvider,
        redirectUri: String,
        expiresAt: OffsetDateTime,
    ) {
        transaction {
            OAuthStatesTable.insert {
                it[OAuthStatesTable.state] = state
                it[OAuthStatesTable.codeVerifier] = codeVerifier
                it[OAuthStatesTable.provider] = provider.name.lowercase()
                it[OAuthStatesTable.redirectUri] = redirectUri
                it[OAuthStatesTable.expiresAt] = expiresAt
            }
        }
    }

    fun consume(state: String): StoredOAuthState? =
        transaction {
            val row =
                OAuthStatesTable
                    .selectAll()
                    .where { OAuthStatesTable.state eq state }
                    .firstOrNull()
                    ?: return@transaction null
            OAuthStatesTable.deleteWhere { OAuthStatesTable.state eq state }
            val expiresAt = row[OAuthStatesTable.expiresAt]
            if (expiresAt.isBefore(OffsetDateTime.now())) {
                return@transaction null
            }
            StoredOAuthState(
                codeVerifier = row[OAuthStatesTable.codeVerifier],
                provider = OAuthProvider.valueOf(row[OAuthStatesTable.provider].uppercase()),
                redirectUri = row[OAuthStatesTable.redirectUri],
            )
        }

    fun purgeExpired() {
        transaction {
            OAuthStatesTable.deleteWhere {
                OAuthStatesTable.expiresAt less OffsetDateTime.now()
            }
        }
    }
}
