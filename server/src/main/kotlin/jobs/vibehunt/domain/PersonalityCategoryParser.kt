package jobs.vibehunt.domain

import jobs.vibehunt.models.PersonalityTraitCategoryJson
import jobs.vibehunt.models.PersonalityTraitJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PersonalityCategoryParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String, categoryKey: String): PersonalityTraitCategoryJson {
        val parsed =
            try {
                json.decodeFromString<PersonalityTraitCategoryJson>(raw)
            } catch (_: Exception) {
                migrateLegacy(raw)
            }
        parsed.validateStructure(categoryKey)
        return parsed
    }

    private fun migrateLegacy(raw: String): PersonalityTraitCategoryJson {
        val legacy = json.decodeFromString<LegacyPersonalityTraitCategoryJson>(raw)
        val traits = legacy.traits.values.toList()
        val topIndex =
            legacy.traits.entries.indexOfFirst { it.value.isTopStrength }.let { idx ->
                if (idx >= 0) idx else 0
            }
        return PersonalityTraitCategoryJson(
            description = legacy.description,
            topStrengthIndex = topIndex,
            traits =
                traits.map { trait ->
                    PersonalityTraitJson(
                        label = trait.label,
                        scalePosition = trait.scalePosition,
                        leftPole = trait.leftPole,
                        rightPole = trait.rightPole,
                        details = trait.details,
                    )
                },
        )
    }

    @Serializable
    private data class LegacyPersonalityTraitCategoryJson(
        val description: String,
        val traits: Map<String, LegacyPersonalityTraitJson>,
    )

    @Serializable
    private data class LegacyPersonalityTraitJson(
        val label: String,
        @SerialName("scale_position") val scalePosition: Double,
        @SerialName("left_pole") val leftPole: String,
        @SerialName("right_pole") val rightPole: String,
        @SerialName("is_top_strength") val isTopStrength: Boolean = false,
        val details: jobs.vibehunt.models.PersonalityTraitDetailsJson? = null,
    )
}
