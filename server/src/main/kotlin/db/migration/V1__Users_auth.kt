package db.migration

import jobs.vibehunt.auth.UserRole
import jobs.vibehunt.db.tables.SessionsTable
import jobs.vibehunt.db.tables.UsersTable
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils

class V1__Users_auth : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            SchemaUtils.create(UsersTable, SessionsTable)
            val roles = UserRole.entries.joinToString(", ") { "'${it.name}'" }
            exec(
                """
                ALTER TABLE users ADD CONSTRAINT users_role_check
                CHECK (role IS NULL OR role IN ($roles));
                """.trimIndent(),
            )
        }
    }
}
