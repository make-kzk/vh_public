package jobs.vibehunt.models

import kotlinx.serialization.Serializable

@Serializable
data class OccupationDto(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val isLeaf: Boolean,
)

@Serializable
data class SkillDto(
    val id: Long,
    val name: String,
)

@Serializable
data class SeekerProfileDto(
    val id: Long,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val phone: String?,
    val telegram: String?,
    val linkedin: String?,
)

@Serializable
data class UpdateSeekerProfileRequest(
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val phone: String? = null,
    val telegram: String? = null,
    val linkedin: String? = null,
)

@Serializable
data class SeekerExperienceDto(
    val id: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: String,
    val endDate: String?,
)

@Serializable
data class CreateSeekerExperienceRequest(
    val companyName: String,
    val position: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String? = null,
)

@Serializable
data class UpdateSeekerExperienceRequest(
    val companyName: String,
    val position: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String? = null,
)

@Serializable
data class SeekerEducationDto(
    val id: Long,
    val institution: String,
    val degree: String?,
    val specialization: String,
    val endYear: Int,
)

@Serializable
data class CreateSeekerEducationRequest(
    val institution: String,
    val degree: String? = null,
    val specialization: String,
    val endYear: Int,
)

@Serializable
data class UpdateSeekerEducationRequest(
    val institution: String,
    val degree: String? = null,
    val specialization: String,
    val endYear: Int,
)

@Serializable
data class SeekerSkillsResponse(
    val skillIds: List<Long>,
    val skills: List<SkillDto>,
)

@Serializable
data class UpdateSeekerSkillsRequest(
    val skillIds: List<Long>,
)

@Serializable
data class SeekerDesiredPositionsResponse(
    val occupationIds: List<Long>,
    val occupations: List<OccupationDto>,
)

@Serializable
data class UpdateSeekerDesiredPositionsRequest(
    val occupationIds: List<Long>,
)

@Serializable
data class EmployerProfileDto(
    val id: Long,
    val name: String,
    val description: String?,
    val website: String?,
    val phone: String?,
    val emailContact: String?,
)

@Serializable
data class UpdateEmployerProfileRequest(
    val name: String,
    val description: String? = null,
    val website: String? = null,
    val phone: String? = null,
    val emailContact: String? = null,
)

@Serializable
data class JobProfileDto(
    val id: Long,
    val occupationId: Long,
    val occupationName: String,
    val description: String?,
    val isActive: Boolean,
    val skillIds: List<Long>,
    val skills: List<SkillDto>,
)

@Serializable
data class CreateJobProfileRequest(
    val occupationId: Long,
    val description: String? = null,
    val isActive: Boolean = true,
    val skillIds: List<Long> = emptyList(),
)

@Serializable
data class UpdateJobProfileRequest(
    val occupationId: Long,
    val description: String? = null,
    val isActive: Boolean = true,
    val skillIds: List<Long> = emptyList(),
)

@Serializable
enum class PersonalityProfileStatus {
    NOT_READY,
    PROCESSING,
    READY,
    FAILED,
}

object PersonalityTraitDetailsRules {
    const val SUCCEED_THROUGH_SIZE = 3
}

@Serializable
data class PersonalityTraitDetailsJson(
    val description: String,
    @kotlinx.serialization.SerialName("good_day") val goodDay: String? = null,
    @kotlinx.serialization.SerialName("bad_day") val badDay: String? = null,
    @kotlinx.serialization.SerialName("succeed_through") val succeedThrough: List<String>? = null,
) {
    fun validateSucceedThrough(path: String) {
        require(succeedThrough != null && succeedThrough.size == PersonalityTraitDetailsRules.SUCCEED_THROUGH_SIZE) {
            "$path.details.succeed_through должен содержать ровно ${PersonalityTraitDetailsRules.SUCCEED_THROUGH_SIZE} пункта"
        }
        succeedThrough.forEachIndexed { index, item ->
            require(item.isNotBlank()) { "$path.details.succeed_through[$index] не может быть пустым" }
        }
    }
}

@Serializable
data class PersonalityTraitJson(
    val label: String,
    @kotlinx.serialization.SerialName("scale_position") val scalePosition: Double,
    @kotlinx.serialization.SerialName("left_pole") val leftPole: String,
    @kotlinx.serialization.SerialName("right_pole") val rightPole: String,
    val details: PersonalityTraitDetailsJson? = null,
)

/** Фиксированный набор черт: ровно [expectedTraitCount] элементов и ровно одна «главная сила» по индексу. */
@Serializable
data class PersonalityTraitCategoryJson(
    val description: String,
    @kotlinx.serialization.SerialName("top_strength_index") val topStrengthIndex: Int,
    val traits: List<PersonalityTraitJson>,
) {
    fun expectedTraitCount(categoryKey: String): Int =
        when (categoryKey) {
            "connections" -> 4
            "creativity" -> 3
            "drive" -> 4
            "thinking" -> 1
            else -> error("Неизвестная категория: $categoryKey")
        }

    fun validateStructure(categoryKey: String) {
        val expected = expectedTraitCount(categoryKey)
        require(traits.size == expected) {
            "$categoryKey.traits должен содержать ровно $expected элемент(ов), получено ${traits.size}"
        }
        require(topStrengthIndex in traits.indices) {
            "$categoryKey.top_strength_index должен быть от 0 до ${traits.lastIndex}, получено $topStrengthIndex"
        }
    }
}

@Serializable
data class PersonalitySectionJson(
    val title: String,
    val items: List<PersonalityItemDto>,
)

@Serializable
data class SeekerPersonalProfileRecord(
    val seekerId: Long,
    val title: String?,
    val description: String?,
    val profile: String?,
    val autonomy: String?,
    val thinkingStyle: String?,
    val burnoutRisk: String?,
    val connections: String?,
    val creativity: String?,
    val drive: String?,
    val thinking: String?,
    val axisDominance: Double?,
    val axisInfluence: Double?,
    val axisStability: Double?,
    val axisIntegrity: Double?,
    val axisAutonomy: Double?,
    val axisPace: Double?,
    val burnoutRiskOverload: Double?,
    val burnoutRiskConflicts: Double?,
    val burnoutRiskDemotivation: Double?,
    val burnoutRiskStress: Double?,
    val energySources: String?,
    val stopFactors: String?,
    val generationStatus: PersonalityProfileStatus,
    val generationError: String?,
)

@Serializable
data class SeekerPersonalProfileLlmOutput(
    val title: String,
    val description: String,
    val profile: String,
    val autonomy: String? = null,
    @kotlinx.serialization.SerialName("thinking_style") val thinkingStyle: String? = null,
    @kotlinx.serialization.SerialName("burnout_risk") val burnoutRisk: String? = null,
    val connections: PersonalityTraitCategoryJson,
    val creativity: PersonalityTraitCategoryJson,
    val drive: PersonalityTraitCategoryJson,
    val thinking: PersonalityTraitCategoryJson,
    @kotlinx.serialization.SerialName("axis_dominance") val axisDominance: Double,
    @kotlinx.serialization.SerialName("axis_influence") val axisInfluence: Double,
    @kotlinx.serialization.SerialName("axis_stability") val axisStability: Double,
    @kotlinx.serialization.SerialName("axis_integrity") val axisIntegrity: Double,
    @kotlinx.serialization.SerialName("axis_autonomy") val axisAutonomy: Double,
    @kotlinx.serialization.SerialName("axis_pace") val axisPace: Double,
    @kotlinx.serialization.SerialName("burnout_risk_overload") val burnoutRiskOverload: Double? = null,
    @kotlinx.serialization.SerialName("burnout_risk_conflicts") val burnoutRiskConflicts: Double? = null,
    @kotlinx.serialization.SerialName("burnout_risk_demotivation") val burnoutRiskDemotivation: Double? = null,
    @kotlinx.serialization.SerialName("burnout_risk_stress") val burnoutRiskStress: Double? = null,
    @kotlinx.serialization.SerialName("energy_sources") val energySources: PersonalitySectionJson,
    @kotlinx.serialization.SerialName("stop_factors") val stopFactors: PersonalitySectionJson,
)

@Serializable
data class PersonalityTraitDto(
    val key: String,
    val label: String,
    val scalePosition: Double,
    val leftPole: String,
    val rightPole: String,
    val description: String,
    val goodDay: String,
    val badDay: String,
    val succeedThrough: List<String>,
    val isTopStrength: Boolean = false,
)

@Serializable
data class PersonalityCategoryDto(
    val key: String,
    val description: String,
    @kotlinx.serialization.SerialName("top_strength_index") val topStrengthIndex: Int,
    val traits: List<PersonalityTraitDto>,
)

@Serializable
data class PersonalityItemDto(
    val title: String,
    val description: String,
)

@Serializable
data class PersonalitySectionDto(
    val title: String,
    val items: List<PersonalityItemDto>,
)

@Serializable
data class PersonalityPreviewDto(
    val status: PersonalityProfileStatus,
    val generationError: String? = null,
    val testsCompleted: Int,
    val testsTotal: Int,
    val title: String? = null,
    val description: String? = null,
    val profile: String? = null,
    val axisDominance: Double? = null,
    val axisInfluence: Double? = null,
    val axisStability: Double? = null,
    val axisIntegrity: Double? = null,
    val axisAutonomy: Double? = null,
    val axisPace: Double? = null,
    val categories: List<PersonalityCategoryDto>? = null,
    val energySources: PersonalitySectionDto? = null,
    val stopFactors: PersonalitySectionDto? = null,
)

@Serializable
data class JobRecommendationDto(
    val id: Long,
    val companyName: String,
    val positionName: String,
    val description: String,
    val matchScore: Double,
    val matchScoreDisplay: Int,
    val testsCompleted: Int,
    val isScoreReduced: Boolean,
)

@Serializable
data class CandidateRecommendationDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val positionName: String,
    val skills: List<String>,
    val matchScore: Double,
    val matchScoreDisplay: Int,
    val testsCompleted: Int,
    val isScoreReduced: Boolean,
)

@Serializable
data class SeekerDashboardDto(
    val profileCompletionPercent: Int,
    val desiredPositionsCount: Int,
    val experienceCount: Int,
    val recommendationsPreview: List<JobRecommendationDto>,
)

@Serializable
data class EmployerDashboardDto(
    val companyName: String,
    val jobProfilesCount: Int,
    val activeJobProfilesCount: Int,
    val totalCandidatesStub: Int,
)
