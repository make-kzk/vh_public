package jobs.vibehunt.llm

object LlmResponseParser {
    fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        val fenceStart = trimmed.indexOf("```")
        if (fenceStart >= 0) {
            val afterFence = trimmed.substring(fenceStart + 3)
            val langEnd = afterFence.indexOf('\n')
            val contentStart = if (langEnd >= 0) langEnd + 1 else 0
            val fenceEnd = afterFence.indexOf("```", contentStart)
            if (fenceEnd >= 0) {
                return afterFence.substring(contentStart, fenceEnd).trim()
            }
        }
        val objectStart = trimmed.indexOf('{')
        val objectEnd = trimmed.lastIndexOf('}')
        if (objectStart >= 0 && objectEnd > objectStart) {
            return trimmed.substring(objectStart, objectEnd + 1)
        }
        return trimmed
    }
}
