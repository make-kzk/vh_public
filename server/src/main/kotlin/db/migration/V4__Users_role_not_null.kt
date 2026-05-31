package db.migration

import jobs.vibehunt.auth.UserRole
import org.flywaydb.core.api.migration.Context

class V4__Users_role_not_null : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            exec("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check")
            val roles = UserRole.entries.joinToString(", ") { "'${it.name}'" }
            exec(
                """
                ALTER TABLE users ADD CONSTRAINT users_role_check
                CHECK (role IN ($roles));
                """.trimIndent(),
            )
            exec("ALTER TABLE users ALTER COLUMN role SET NOT NULL")
        }
    }
}
