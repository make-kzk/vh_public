package db.migration

import org.flywaydb.core.api.migration.Context

class V5__Restructure_survey_groups : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            exec(
                """
                UPDATE surveys SET group_code = 'core', sort_order = 1 WHERE code = '2501-10-5KEY';
                UPDATE surveys SET group_code = 'core', sort_order = 2 WHERE code = '2520-10-ETYA';
                UPDATE surveys SET group_code = 'core', sort_order = 3 WHERE code = '2521-10-NEYA';
                UPDATE surveys SET group_code = 'core', sort_order = 4 WHERE code = '2530-10-12F4';
                UPDATE surveys SET group_code = 'core', sort_order = 5 WHERE code = '2550-10-1IN2';
                UPDATE surveys SET group_code = 'core', sort_order = 6 WHERE code = '2551-10-GNFL';
                UPDATE surveys SET group_code = 'core', sort_order = 7 WHERE code = '2552-10-RDFL';
                UPDATE surveys SET group_code = 'core', sort_order = 8 WHERE code = '2560-10-BLBN';
                UPDATE surveys SET group_code = '64qn', sort_order = 1 WHERE code = '2540-10-64QN';
                """.trimIndent(),
            )
        }
    }
}
