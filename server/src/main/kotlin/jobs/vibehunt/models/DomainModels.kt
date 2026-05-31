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
data class PersonalityTraitDto(
    val key: String,
    val label: String,
    val scalePosition: Double,
    val leftPole: String,
    val rightPole: String,
    val description: String,
)

@Serializable
data class PersonalityCategoryDto(
    val key: String,
    val description: String,
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
    val title: String,
    val description: String,
    val profile: String,
    val axisDominance: Double,
    val axisInfluence: Double,
    val axisStability: Double,
    val axisIntegrity: Double,
    val axisAutonomy: Double,
    val axisPace: Double,
    val categories: List<PersonalityCategoryDto>,
    val energySources: PersonalitySectionDto,
    val stopFactors: PersonalitySectionDto,
    val testsCompleted: Int,
    val testsTotal: Int,
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
