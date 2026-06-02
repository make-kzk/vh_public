package jobs.vibehunt.domain

import jobs.vibehunt.survey.SurveyLlmContextDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersonalityPromptBuilder {
    private val json = Json { prettyPrint = true }

    fun build(context: SurveyLlmContextDto): Pair<String, String> {
        val systemPrompt =
            """
            Ты — опытный психолог-интерпретатор результатов личностных опросов.
            На основе предоставленных ответов, расчётов и глоссария сформируй целостный личностный профиль соискателя.

            ВАЖНО: верни ответ СТРОГО в формате JSON без markdown-обёртки и без пояснений.
            JSON должен соответствовать следующей структуре (все поля обязательны, кроме помеченных optional):

            {
              "title": "string — краткий тип личности",
              "description": "string — общее описание",
              "profile": "string — развёрнутый профиль",
              "autonomy": "string — описание автономности (optional)",
              "thinking_style": "string — стиль мышления (optional)",
              "burnout_risk": "string — общий риск выгорания (optional)",
              "connections": { "description": "string", "traits": { "<key>": { "label": "string", "scale_position": 0.0-1.0, "left_pole": "string", "right_pole": "string", "is_top_strength": false, "details": { "description": "string", "good_day": "string", "bad_day": "string", "succeed_through": ["string", "..."] } } } },
              "creativity": { ... same structure as connections ... },
              "drive": { ... same structure as connections ... },
              "thinking": { ... same structure as connections ... },
              "axis_dominance": 0.0-1.0,
              "axis_influence": 0.0-1.0,
              "axis_stability": 0.0-1.0,
              "axis_integrity": 0.0-1.0,
              "axis_autonomy": 0.0-1.0,
              "axis_pace": 0.0-1.0,
              "burnout_risk_overload": 0.0-1.0 (optional),
              "burnout_risk_conflicts": 0.0-1.0 (optional),
              "burnout_risk_demotivation": 0.0-1.0 (optional),
              "burnout_risk_stress": 0.0-1.0 (optional),
              "energy_sources": { "title": "string", "items": [{ "title": "string", "description": "string" }] },
              "stop_factors": { "title": "string", "items": [{ "title": "string", "description": "string" }] }
            }

            Используй термины из глоссария. Опирайся на calculated_results каждого опроса.
            Пиши на русском языке. Числовые значения осей — нормализованные от 0 до 1.

            Правила для категорий connections, creativity, drive, thinking:
            - description категории — вводный абзац по шаблону «Ваш раздел … показывает …» (что измеряет категория).
            - connections и drive: 3–5 черт (traits); creativity: около 3; thinking: 1–3.
            - label черты — заголовок в духе «Вы немного более {правый полюс}, чем {левый}» или «Вы {доминирующий полюс}» при сильном смещении (scale_position ближе к 0 или 1).
            - left_pole и right_pole — короткие русские названия противоположных полюсов шкалы.
            - scale_position: 0 = левый полюс, 1 = правый полюс.
            - is_top_strength: true у 0–2 самых выраженных черт внутри каждой категории (остальные false).
            - details обязательны для каждой черты: description (абзац под заголовком), good_day, bad_day, succeed_through (2–4 коротких пункта без нумерации в тексте).
            """.trimIndent()

        val userPrompt =
            """
            Данные для интерпретации:

            ${json.encodeToString(context)}
            """.trimIndent()

        return systemPrompt to userPrompt
    }

    fun retryPrompt(originalError: String): String =
        """
        Предыдущий ответ был некорректен: $originalError
        Верни ТОЛЬКО валидный JSON строго по указанной схеме, без markdown и комментариев.
        """.trimIndent()
}
