package jobs.vibehunt.domain

import jobs.vibehunt.db.SeekerRepository
import jobs.vibehunt.db.SurveyRepository
import jobs.vibehunt.fixtures.StubData
import jobs.vibehunt.models.PersonalityPreviewDto
import jobs.vibehunt.survey.SaveSurveyAnswersRequest
import jobs.vibehunt.survey.SurveyDefinitionDto
import jobs.vibehunt.survey.SurveyDetailDto
import jobs.vibehunt.survey.SurveyGroupDto
import jobs.vibehunt.survey.SurveyGroupsResponseDto
import jobs.vibehunt.survey.SurveyListItemDto
import jobs.vibehunt.survey.SurveyLlmContextDto
import jobs.vibehunt.survey.SurveyLlmContextItemDto
import jobs.vibehunt.survey.SurveyResultDto
import jobs.vibehunt.survey.SurveyStatus
import jobs.vibehunt.survey.scoring.SurveyAnswerValidator
import jobs.vibehunt.survey.scoring.SurveyScoringService
import kotlinx.serialization.json.Json
import java.util.UUID

class SurveyService(
    private val seekerRepository: SeekerRepository,
    private val surveyRepository: SurveyRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val groupNames =
        mapOf(
            "disc" to "DISC-профиль",
            "motivation" to "Мотивация и ценности",
            "team" to "Команда и самопредставление",
        )

    private val groupOrder = listOf("disc", "motivation", "team")

    fun listGroups(userId: UUID): SurveyGroupsResponseDto {
        val seeker = seekerRepository.findByUserId(userId) ?: error("Соискатель не найден")
        val surveys = surveyRepository.listSurveys()
        val results = surveyRepository.listResultsForSeeker(seeker.id)
        val statusBySurvey = buildStatusMap(results)

        val groups =
            groupOrder.map { groupCode ->
                val groupSurveys = surveys.filter { it.groupCode == groupCode }
                val items =
                    groupSurveys.map { survey ->
                        SurveyListItemDto(
                            id = survey.id,
                            code = survey.code,
                            name = survey.name,
                            description = survey.description,
                            status = statusBySurvey[survey.id] ?: SurveyStatus.NOT_STARTED,
                            sortOrder = survey.sortOrder,
                        )
                    }
                val completed = items.count { it.status == SurveyStatus.COMPLETED }
                SurveyGroupDto(
                    code = groupCode,
                    name = groupNames[groupCode] ?: groupCode,
                    surveys = items,
                    completedCount = completed,
                    totalCount = items.size,
                )
            }

        val testsCompleted = groups.count { it.completedCount == it.totalCount && it.totalCount > 0 }
        return SurveyGroupsResponseDto(
            groups = groups,
            testsCompleted = testsCompleted,
            testsTotal = groups.size,
        )
    }

    fun getSurvey(userId: UUID, surveyId: Long): SurveyDetailDto {
        val seeker = seekerRepository.findByUserId(userId) ?: error("Соискатель не найден")
        val survey = surveyRepository.findSurveyById(surveyId) ?: error("Опрос не найден")
        val status = resolveStatus(seeker.id, surveyId)
        val inProgress = surveyRepository.findInProgressResult(seeker.id, surveyId)
        return SurveyDetailDto(
            id = survey.id,
            code = survey.code,
            name = survey.name,
            description = survey.description,
            groupCode = survey.groupCode,
            questionsJson = survey.questionsJson,
            status = status,
            answersJson = inProgress?.answersJson,
            resultId = inProgress?.id,
        )
    }

    fun startSurvey(userId: UUID, surveyId: Long): SurveyDetailDto {
        val seeker = seekerRepository.findByUserId(userId) ?: error("Соискатель не найден")
        surveyRepository.findSurveyById(surveyId) ?: error("Опрос не найден")
        if (surveyRepository.findCompletedResult(seeker.id, surveyId) != null) {
            error("Опрос уже пройден")
        }
        if (surveyRepository.findInProgressResult(seeker.id, surveyId) == null) {
            surveyRepository.createInProgressResult(seeker.id, surveyId)
        }
        return getSurvey(userId, surveyId)
    }

    fun saveAnswers(userId: UUID, surveyId: Long, request: SaveSurveyAnswersRequest): SurveyDetailDto {
        val seeker = seekerRepository.findByUserId(userId) ?: error("Соискатель не найден")
        val survey = surveyRepository.findSurveyById(surveyId) ?: error("Опрос не найден")
        val result =
            surveyRepository.findInProgressResult(seeker.id, surveyId)
                ?: error("Сначала начните опрос")
        val answersJson = json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), request.answers)
        surveyRepository.updateAnswers(result.id, answersJson)
            ?: error("Не удалось сохранить ответы")
        return getSurvey(userId, surveyId)
    }

    fun completeSurvey(userId: UUID, surveyId: Long, request: SaveSurveyAnswersRequest): SurveyResultDto {
        val seeker = seekerRepository.findByUserId(userId) ?: error("Соискатель не найден")
        val survey = surveyRepository.findSurveyById(surveyId) ?: error("Опрос не найден")
        if (surveyRepository.findCompletedResult(seeker.id, surveyId) != null) {
            error("Опрос уже пройден")
        }
        val result =
            surveyRepository.findInProgressResult(seeker.id, surveyId)
                ?: error("Сначала начните опрос")

        SurveyAnswerValidator.validate(survey.code, survey.questionsJson, request.answers)
        val answersJson = json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), request.answers)
        val key = surveyRepository.findSurveyKey(surveyId) ?: error("Ключи подсчёта не найдены")
        val calculated =
            SurveyScoringService.calculate(
                surveyCode = survey.code,
                scoringLogic = key.scoringLogic,
                keysDataJson = key.keysDataJson,
                questionsJson = survey.questionsJson,
                answersJson = answersJson,
            )
        return surveyRepository.completeResult(result.id, answersJson, calculated)
            ?: error("Не удалось завершить опрос")
    }

    fun buildLlmContext(userId: UUID): SurveyLlmContextDto {
        val seeker = seekerRepository.findByUserId(userId) ?: error("Соискатель не найден")
        val surveys = surveyRepository.listSurveys().associateBy { it.id }
        val completed =
            surveyRepository.listResultsForSeeker(seeker.id)
                .filter { it.completedAt != null && it.calculatedResultsJson != null }
        val items =
            completed.mapNotNull { result ->
                val survey = surveys[result.surveyId] ?: return@mapNotNull null
                SurveyLlmContextItemDto(
                    surveyCode = survey.code,
                    answersJson = result.answersJson,
                    calculatedResultsJson = result.calculatedResultsJson!!,
                )
            }
        return SurveyLlmContextDto(
            surveys = items,
            glossaryTerms = surveyRepository.listGlossaryTerms(),
        )
    }

    fun personalityPreview(userId: UUID): PersonalityPreviewDto {
        val groups = listGroups(userId)
        val stub = StubData.personalityPreview()
        return stub.copy(
            testsCompleted = groups.testsCompleted,
            testsTotal = groups.testsTotal,
        )
    }

    private fun buildStatusMap(results: List<SurveyResultDto>): Map<Long, SurveyStatus> {
        val map = mutableMapOf<Long, SurveyStatus>()
        results.forEach { result ->
            map[result.surveyId] =
                when {
                    result.completedAt != null -> SurveyStatus.COMPLETED
                    else -> SurveyStatus.IN_PROGRESS
                }
        }
        return map
    }

    private fun resolveStatus(seekerId: Long, surveyId: Long): SurveyStatus {
        val completed = surveyRepository.findCompletedResult(seekerId, surveyId)
        if (completed != null) return SurveyStatus.COMPLETED
        val inProgress = surveyRepository.findInProgressResult(seekerId, surveyId)
        if (inProgress != null) return SurveyStatus.IN_PROGRESS
        return SurveyStatus.NOT_STARTED
    }
}
