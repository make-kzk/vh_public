package db.migration

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
import jobs.vibehunt.db.tables.SkillsTable
import jobs.vibehunt.db.tables.SuperpowersAndTalentsTable
import jobs.vibehunt.db.tables.SurveyKeysTable
import jobs.vibehunt.db.tables.SurveyResultsTable
import jobs.vibehunt.db.tables.SurveysTable
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.OffsetDateTime

class V2__Domain_schema : ExposedMigration() {
    override fun migrate(context: Context) {
        exposedTransaction {
            SchemaUtils.create(
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
            exec("CREATE INDEX IF NOT EXISTS idx_occupations_parent ON occupations (parent_id)")
            seedReferenceData()
        }
    }

    private fun seedReferenceData() {
        val now = OffsetDateTime.now()

        val skillNames =
            listOf(
                "Kotlin", "Java", "Python", "JavaScript", "TypeScript", "React", "Vue.js",
                "PostgreSQL", "MongoDB", "Docker", "Kubernetes", "AWS", "Git", "CI/CD",
                "REST API", "GraphQL", "Microservices", "Agile", "Scrum", "Project Management",
                "UI/UX Design", "Figma", "SQL", "Redis", "Spring Boot", "Ktor", "Node.js",
                "Machine Learning", "Data Analysis", "Technical Writing",
            )
        skillNames.forEach { name ->
            SkillsTable.insert {
                it[SkillsTable.name] = name
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        val itCategoryId =
            OccupationsTable.insert {
                it[parentId] = null
                it[OccupationsTable.name] = "IT и разработка"
                it[isLeaf] = false
                it[createdAt] = now
                it[updatedAt] = now
            }[OccupationsTable.id].value

        val mgmtCategoryId =
            OccupationsTable.insert {
                it[parentId] = null
                it[OccupationsTable.name] = "Менеджмент"
                it[isLeaf] = false
                it[createdAt] = now
                it[updatedAt] = now
            }[OccupationsTable.id].value

        val designCategoryId =
            OccupationsTable.insert {
                it[parentId] = null
                it[OccupationsTable.name] = "Дизайн"
                it[isLeaf] = false
                it[createdAt] = now
                it[updatedAt] = now
            }[OccupationsTable.id].value

        listOf(
            "Backend-разработчик" to itCategoryId,
            "Frontend-разработчик" to itCategoryId,
            "Fullstack-разработчик" to itCategoryId,
            "DevOps-инженер" to itCategoryId,
            "QA-инженер" to itCategoryId,
            "Data Engineer" to itCategoryId,
            "Mobile-разработчик" to itCategoryId,
            "Product Manager" to mgmtCategoryId,
            "Project Manager" to mgmtCategoryId,
            "Team Lead" to mgmtCategoryId,
            "UX/UI дизайнер" to designCategoryId,
            "Product Designer" to designCategoryId,
        ).forEach { (name, parentId) ->
            OccupationsTable.insert {
                it[OccupationsTable.parentId] = parentId
                it[OccupationsTable.name] = name
                it[isLeaf] = true
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        listOf(
            "Аналитическое мышление",
            "Командная работа",
            "Лидерство",
            "Креативность",
            "Стрессоустойчивость",
        ).forEach { name ->
            SuperpowersAndTalentsTable.insert {
                it[SuperpowersAndTalentsTable.name] = name
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        val openQuestionsJson =
            """
            {"type":"open_questions","items":[
              {"id":"1","text":"Карьера мечты — это когда..."},
              {"id":"2","text":"Мой жизненный девиз — это..."},
              {"id":"3","text":"Я по-настоящему счастлив в работе, когда..."},
              {"id":"4","text":"Идеальный руководитель для меня..."},
              {"id":"5","text":"Идеальный коллектив для меня..."}
            ]}
            """.trimIndent()

        val multiSelectJson =
            """
            {"type":"multi_select","required_count":12,"options":[
              {"id":1,"text":"Оптимистичный"},{"id":2,"text":"Дисциплинированный"},
              {"id":3,"text":"Независимый"},{"id":4,"text":"Стабильный"}
            ]}
            """.trimIndent()

        val discJson =
            """
            {"type":"allocate_points","total_points":10,"max_per_option":5,"questions":[
              {"id":1,"text":"Как вы обычно принимаете решения:","options":[
                {"id":1,"text":"Подробно анализируя все факты"},{"id":2,"text":"Быстро и решительно"}
              ]}
            ]}
            """.trimIndent()

        listOf(
            Triple("2501-10-5KEY", "Продолжите фразу", openQuestionsJson),
            Triple("2520-10-ETYA", "Мои качества", multiSelectJson),
            Triple("2530-10-12F4", "Распределение 10 баллов (DISC)", discJson),
        ).forEach { (code, name, questions) ->
            SurveysTable.insert {
                it[SurveysTable.code] = code
                it[SurveysTable.name] = name
                it[description] = "Инструкция: пройдите опросник."
                it[SurveysTable.questions] = questions
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        val surveyId =
            SurveysTable
                .selectAll()
                .where { SurveysTable.code eq "2520-10-ETYA" }
                .first()[SurveysTable.id].value

        SurveyKeysTable.insert {
            it[SurveyKeysTable.surveyId] = surveyId
            it[scoringLogic] = "matrix"
            it[keysData] = """{"qualities_weights":{"1":{"A3":1,"B1":2}}}"""
            it[createdAt] = now
            it[updatedAt] = now
        }
    }
}
