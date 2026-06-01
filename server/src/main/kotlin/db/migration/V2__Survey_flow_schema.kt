package db.migration

import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.transactions.transaction

class V2__Survey_flow_schema : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            exec(
                """
                ALTER TABLE survey_results ALTER COLUMN completed_at DROP NOT NULL;
                """.trimIndent(),
            )
            exec(
                """
                ALTER TABLE surveys ADD COLUMN IF NOT EXISTS group_code VARCHAR(50);
                ALTER TABLE surveys ADD COLUMN IF NOT EXISTS sort_order INT;
                UPDATE surveys SET group_code = 'legacy', sort_order = 0 WHERE group_code IS NULL;
                ALTER TABLE surveys ALTER COLUMN group_code SET NOT NULL;
                ALTER TABLE surveys ALTER COLUMN sort_order SET NOT NULL;
                """.trimIndent(),
            )
            exec(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_survey_results_seeker_survey_completed
                ON survey_results (seeker_id, survey_id)
                WHERE completed_at IS NOT NULL;
                """.trimIndent(),
            )
        }
    }
}
