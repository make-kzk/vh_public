package jobs.vibehunt.domain

import jobs.vibehunt.llm.LlmResponseParser
import jobs.vibehunt.models.SeekerPersonalProfileLlmOutput
import kotlinx.serialization.json.Json

class PersonalityProfileValidator {
    private val json = Json { ignoreUnknownKeys = true }

    fun validateAndParse(rawResponse: String): SeekerPersonalProfileLlmOutput {
        val jsonText = LlmResponseParser.extractJson(rawResponse)
        val output =
            try {
                json.decodeFromString<SeekerPersonalProfileLlmOutput>(jsonText)
            } catch (e: Exception) {
                throw IllegalArgumentException("Не удалось разобрать JSON ответа LLM: ${e.message}")
            }

        require(output.title.isNotBlank()) { "Поле title обязательно" }
        require(output.description.isNotBlank()) { "Поле description обязательно" }
        require(output.profile.isNotBlank()) { "Поле profile обязательно" }

        listOf(
            output.axisDominance,
            output.axisInfluence,
            output.axisStability,
            output.axisIntegrity,
            output.axisAutonomy,
            output.axisPace,
        ).forEach { requireScore(it, "axis") }

        output.burnoutRiskOverload?.let { requireScore(it, "burnout_risk_overload") }
        output.burnoutRiskConflicts?.let { requireScore(it, "burnout_risk_conflicts") }
        output.burnoutRiskDemotivation?.let { requireScore(it, "burnout_risk_demotivation") }
        output.burnoutRiskStress?.let { requireScore(it, "burnout_risk_stress") }

        validateCategory(output.connections, "connections")
        validateCategory(output.creativity, "creativity")
        validateCategory(output.drive, "drive")
        validateCategory(output.thinking, "thinking")
        validateSection(output.energySources, "energy_sources")
        validateSection(output.stopFactors, "stop_factors")

        return output
    }

    private fun requireScore(value: Double, field: String) {
        require(value in 0.0..1.0) { "$field должно быть в диапазоне 0.0–1.0, получено $value" }
    }

    private fun validateCategory(
        category: jobs.vibehunt.models.PersonalityTraitCategoryJson,
        name: String,
    ) {
        require(category.description.isNotBlank()) { "$name.description обязательно" }
        require(category.traits.isNotEmpty()) { "$name.traits не может быть пустым" }
        category.traits.forEach { (key, trait) ->
            require(key.isNotBlank()) { "$name: пустой ключ trait" }
            require(trait.label.isNotBlank()) { "$name.$key.label обязательно" }
            requireScore(trait.scalePosition, "$name.$key.scale_position")
        }
    }

    private fun validateSection(
        section: jobs.vibehunt.models.PersonalitySectionJson,
        name: String,
    ) {
        require(section.title.isNotBlank()) { "$name.title обязательно" }
        require(section.items.isNotEmpty()) { "$name.items не может быть пустым" }
        section.items.forEach { item ->
            require(item.title.isNotBlank()) { "$name: пустой title элемента" }
            require(item.description.isNotBlank()) { "$name: пустой description элемента" }
        }
    }
}
