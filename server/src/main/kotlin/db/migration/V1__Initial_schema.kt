package db.migration

import jobs.vibehunt.auth.UserRole
import jobs.vibehunt.db.tables.EmployerJobProfilesTable
import jobs.vibehunt.db.tables.EmployersTable
import jobs.vibehunt.db.tables.GlossaryTermsTable
import jobs.vibehunt.db.tables.JobProfileSkillsTable
import jobs.vibehunt.db.tables.OccupationsTable
import jobs.vibehunt.db.tables.SeekerDesiredPositionsTable
import jobs.vibehunt.db.tables.SeekerEducationTable
import jobs.vibehunt.db.tables.SeekerExperienceTable
import jobs.vibehunt.db.tables.SeekerPersonalProfilesTable
import jobs.vibehunt.db.tables.SeekerSkillsTable
import jobs.vibehunt.db.tables.SeekerSuperpowersAndTalentsTable
import jobs.vibehunt.db.tables.SeekersTable
import jobs.vibehunt.db.tables.SessionsTable
import jobs.vibehunt.db.tables.SkillsTable
import jobs.vibehunt.db.tables.SuperpowersAndTalentsTable
import jobs.vibehunt.db.tables.SurveyKeysTable
import jobs.vibehunt.db.tables.SurveyResultsTable
import jobs.vibehunt.db.tables.SurveysTable
import jobs.vibehunt.db.tables.UsersTable
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import java.nio.file.Files
import java.nio.file.Path

class V1__Initial_schema : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            SchemaUtils.create(
                UsersTable,
                SessionsTable,
                SkillsTable,
                OccupationsTable,
                SeekersTable,
                SeekerExperienceTable,
                SeekerEducationTable,
                SeekerSkillsTable,
                SeekerDesiredPositionsTable,
                SeekerPersonalProfilesTable,
                SuperpowersAndTalentsTable,
                SeekerSuperpowersAndTalentsTable,
                EmployersTable,
                EmployerJobProfilesTable,
                JobProfileSkillsTable,
                SurveysTable,
                SurveyResultsTable,
                SurveyKeysTable,
                GlossaryTermsTable,
            )
            val roles = UserRole.entries.joinToString(", ") { "'${it.name}'" }
            exec(
                """
                ALTER TABLE users ADD CONSTRAINT users_role_check
                CHECK (role IN ($roles));
                """.trimIndent(),
            )
            exec("CREATE INDEX IF NOT EXISTS idx_occupations_parent ON occupations (parent_id)")
        }
    }
}
