package db.migration

import org.flywaydb.core.api.migration.Context

class V4__Seeker_personal_profiles_jsonb_columns : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            for (column in JSONB_COLUMNS) {
                exec(
                    """
                    ALTER TABLE seeker_personal_profiles
                    ALTER COLUMN $column TYPE jsonb
                    USING CASE
                        WHEN $column IS NULL OR btrim($column) = '' THEN NULL
                        ELSE $column::jsonb
                    END;
                    """.trimIndent(),
                )
            }
        }
    }

    private companion object {
        val JSONB_COLUMNS =
            listOf(
                "connections",
                "creativity",
                "drive",
                "thinking",
                "energy_sources",
                "stop_factors",
            )
    }
}
