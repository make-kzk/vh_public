package db.migration

import org.flywaydb.core.api.migration.Context

class V3__Personal_profile_generation_status : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            exec(
                """
                ALTER TABLE seeker_personal_profiles
                ADD COLUMN IF NOT EXISTS generation_status VARCHAR(20) NOT NULL DEFAULT 'NOT_READY';
                """.trimIndent(),
            )
            exec(
                """
                ALTER TABLE seeker_personal_profiles
                ADD COLUMN IF NOT EXISTS generation_error TEXT NULL;
                """.trimIndent(),
            )
        }
    }
}
