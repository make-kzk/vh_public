package jobs.vibehunt.llm

interface LlmClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
}
