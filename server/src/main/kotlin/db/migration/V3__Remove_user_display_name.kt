package db.migration

import org.flywaydb.core.api.migration.Context

class V3__Remove_user_display_name : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            exec("ALTER TABLE users DROP COLUMN IF EXISTS display_name")
        }
    }
}
