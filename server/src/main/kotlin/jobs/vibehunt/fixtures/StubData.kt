package jobs.vibehunt.fixtures

import jobs.vibehunt.models.CandidateRecommendationDto
import jobs.vibehunt.models.JobRecommendationDto
import jobs.vibehunt.models.PersonalityCategoryDto
import jobs.vibehunt.models.PersonalityItemDto
import jobs.vibehunt.models.PersonalityPreviewDto
import jobs.vibehunt.models.PersonalitySectionDto
import jobs.vibehunt.models.PersonalityTraitDto

object StubData {
    fun personalityPreview(): PersonalityPreviewDto =
        PersonalityPreviewDto(
            title = "Стратегический аналитик",
            description =
                "Вы сочетаете системное мышление с прагматичным подходом к решению задач. " +
                    "Цените автономность и работаете лучше всего в среде с чёткими целями.",
            profile =
                "Вы — человек, который предпочитает глубокий анализ быстрым решениям, " +
                    "но умеет действовать решительно, когда ситуация этого требует.",
            axisDominance = 0.62,
            axisInfluence = 0.45,
            axisStability = 0.71,
            axisIntegrity = 0.83,
            axisAutonomy = 0.78,
            axisPace = 0.55,
            categories =
                listOf(
                    PersonalityCategoryDto(
                        key = "connections",
                        description = "Как вы управляете отношениями и работаете в команде.",
                        traits =
                            listOf(
                                PersonalityTraitDto(
                                    key = "diplomatic_vs_direct",
                                    label = "Вы немного более дипломатичны, чем прямолинейны",
                                    scalePosition = 0.55,
                                    leftPole = "Прямолинейность",
                                    rightPole = "Дипломатичность",
                                    description = "Вы учитываете мнение других и стремитесь к справедливому разрешению конфликтов.",
                                ),
                                PersonalityTraitDto(
                                    key = "reserved_vs_sociable",
                                    label = "Вы сдержанны",
                                    scalePosition = 0.25,
                                    leftPole = "Сдержанность",
                                    rightPole = "Общительность",
                                    description = "Предпочитаете работать с знакомыми людьми и комфортно чувствуете себя в одиночной работе.",
                                ),
                            ),
                    ),
                    PersonalityCategoryDto(
                        key = "thinking",
                        description = "Способности, которые вы используете при решении задач.",
                        traits =
                            listOf(
                                PersonalityTraitDto(
                                    key = "intuitive_vs_agile",
                                    label = "Вы высоко гибки в мышлении",
                                    scalePosition = 0.85,
                                    leftPole = "Интуиция",
                                    rightPole = "Гибкость",
                                    description = "Быстро обучаетесь и решаете сложные задачи логически и аналитически.",
                                ),
                            ),
                    ),
                ),
            energySources =
                PersonalitySectionDto(
                    title = "Источники энергии",
                    items =
                        listOf(
                            PersonalityItemDto(
                                title = "Амбициозные цели и вызовы",
                                description = "Максимальная отдача при масштабных задачах и реальном влиянии.",
                            ),
                            PersonalityItemDto(
                                title = "Автономность и доверие",
                                description = "Свобода принятия решений — ключ к продуктивности.",
                            ),
                        ),
                ),
            stopFactors =
                PersonalitySectionDto(
                    title = "Стоп-факторы — красные флаги",
                    items =
                        listOf(
                            PersonalityItemDto(
                                title = "Микроменеджмент",
                                description = "Избыточный контроль резко снижает мотивацию.",
                            ),
                            PersonalityItemDto(
                                title = "Размытые цели",
                                description = "Отсутствие чётких приоритетов — главный источник стресса.",
                            ),
                        ),
                ),
            testsCompleted = 1,
            testsTotal = 3,
        )

    fun jobRecommendations(): List<JobRecommendationDto> =
        listOf(
            JobRecommendationDto(
                id = 1,
                companyName = "TechNova",
                positionName = "Backend-разработчик",
                description = "Разработка микросервисов на Kotlin, участие в архитектурных решениях.",
                matchScore = 0.82,
                matchScoreDisplay = 4,
                testsCompleted = 1,
                isScoreReduced = true,
            ),
            JobRecommendationDto(
                id = 2,
                companyName = "DataFlow",
                positionName = "Fullstack-разработчик",
                description = "React + Ktor, продуктовая команда, гибкий график.",
                matchScore = 0.76,
                matchScoreDisplay = 4,
                testsCompleted = 1,
                isScoreReduced = true,
            ),
            JobRecommendationDto(
                id = 3,
                companyName = "CloudScale",
                positionName = "DevOps-инженер",
                description = "Kubernetes, CI/CD, облачная инфраструктура.",
                matchScore = 0.68,
                matchScoreDisplay = 3,
                testsCompleted = 1,
                isScoreReduced = true,
            ),
            JobRecommendationDto(
                id = 4,
                companyName = "ProductLab",
                positionName = "Product Manager",
                description = "Управление продуктом в IT-команде, работа с метриками.",
                matchScore = 0.61,
                matchScoreDisplay = 3,
                testsCompleted = 1,
                isScoreReduced = true,
            ),
        )

    fun candidateRecommendations(jobProfileId: Long): List<CandidateRecommendationDto> =
        listOf(
            CandidateRecommendationDto(
                id = 101,
                firstName = "Алексей",
                lastName = "Петров",
                positionName = "Backend-разработчик",
                skills = listOf("Kotlin", "PostgreSQL", "Docker"),
                matchScore = 0.88,
                matchScoreDisplay = 4,
                testsCompleted = 3,
                isScoreReduced = false,
            ),
            CandidateRecommendationDto(
                id = 102,
                firstName = "Мария",
                lastName = "Сидорова",
                positionName = "Backend-разработчик",
                skills = listOf("Java", "Spring Boot", "Redis"),
                matchScore = 0.79,
                matchScoreDisplay = 4,
                testsCompleted = 2,
                isScoreReduced = true,
            ),
            CandidateRecommendationDto(
                id = 103,
                firstName = "Дмитрий",
                lastName = "Козлов",
                positionName = "Backend-разработчик",
                skills = listOf("Kotlin", "Ktor", "AWS"),
                matchScore = 0.72,
                matchScoreDisplay = 4,
                testsCompleted = 1,
                isScoreReduced = true,
            ),
        )
}
