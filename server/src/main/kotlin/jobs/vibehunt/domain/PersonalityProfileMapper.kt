package jobs.vibehunt.domain

import jobs.vibehunt.models.PersonalityCategoryDto
import jobs.vibehunt.models.PersonalityPreviewDto
import jobs.vibehunt.models.PersonalityProfileStatus
import jobs.vibehunt.models.PersonalitySectionDto
import jobs.vibehunt.models.PersonalitySectionJson
import jobs.vibehunt.models.PersonalityTraitCategoryJson
import jobs.vibehunt.models.PersonalityTraitDto
import jobs.vibehunt.models.SeekerPersonalProfileLlmOutput
import jobs.vibehunt.models.SeekerPersonalProfileRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PersonalityProfileMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toPreview(
        record: SeekerPersonalProfileRecord,
        testsCompleted: Int,
        testsTotal: Int,
    ): PersonalityPreviewDto {
        val categories = buildCategories(record)
        val energySources = record.energySources?.let { parseSection(it) }
        val stopFactors = record.stopFactors?.let { parseSection(it) }
        return PersonalityPreviewDto(
            status = PersonalityProfileStatus.READY,
            testsCompleted = testsCompleted,
            testsTotal = testsTotal,
            title = record.title,
            description = record.description,
            profile = record.profile,
            axisDominance = record.axisDominance,
            axisInfluence = record.axisInfluence,
            axisStability = record.axisStability,
            axisIntegrity = record.axisIntegrity,
            axisAutonomy = record.axisAutonomy,
            axisPace = record.axisPace,
            categories = categories,
            energySources = energySources,
            stopFactors = stopFactors,
        )
    }

    fun fromLlmOutput(seekerId: Long, output: SeekerPersonalProfileLlmOutput): SeekerPersonalProfileRecord =
        SeekerPersonalProfileRecord(
            seekerId = seekerId,
            title = output.title,
            description = output.description,
            profile = output.profile,
            autonomy = output.autonomy,
            thinkingStyle = output.thinkingStyle,
            burnoutRisk = output.burnoutRisk,
            connections = json.encodeToString(output.connections),
            creativity = json.encodeToString(output.creativity),
            drive = json.encodeToString(output.drive),
            thinking = json.encodeToString(output.thinking),
            axisDominance = output.axisDominance,
            axisInfluence = output.axisInfluence,
            axisStability = output.axisStability,
            axisIntegrity = output.axisIntegrity,
            axisAutonomy = output.axisAutonomy,
            axisPace = output.axisPace,
            burnoutRiskOverload = output.burnoutRiskOverload,
            burnoutRiskConflicts = output.burnoutRiskConflicts,
            burnoutRiskDemotivation = output.burnoutRiskDemotivation,
            burnoutRiskStress = output.burnoutRiskStress,
            energySources = json.encodeToString(output.energySources),
            stopFactors = json.encodeToString(output.stopFactors),
            generationStatus = PersonalityProfileStatus.READY,
            generationError = null,
        )

    private fun buildCategories(record: SeekerPersonalProfileRecord): List<PersonalityCategoryDto> {
        val keys =
            listOf(
                "connections" to record.connections,
                "creativity" to record.creativity,
                "drive" to record.drive,
                "thinking" to record.thinking,
            )
        return keys.mapNotNull { (key, raw) ->
            raw?.let { categoryJson ->
                val parsed = json.decodeFromString<PersonalityTraitCategoryJson>(categoryJson)
                PersonalityCategoryDto(
                    key = key,
                    description = parsed.description,
                    traits =
                        parsed.traits.map { (traitKey, trait) ->
                            val details = trait.details
                            PersonalityTraitDto(
                                key = traitKey,
                                label = trait.label,
                                scalePosition = trait.scalePosition,
                                leftPole = trait.leftPole,
                                rightPole = trait.rightPole,
                                description = details?.description ?: trait.label,
                                goodDay = details?.goodDay ?: "",
                                badDay = details?.badDay ?: "",
                                succeedThrough = details?.succeedThrough ?: emptyList(),
                                isTopStrength = trait.isTopStrength,
                            )
                        },
                )
            }
        }
    }

    private fun parseSection(raw: String): PersonalitySectionDto {
        val section = json.decodeFromString<PersonalitySectionJson>(raw)
        return PersonalitySectionDto(title = section.title, items = section.items)
    }
}
