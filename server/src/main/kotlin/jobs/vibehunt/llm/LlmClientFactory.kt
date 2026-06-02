package jobs.vibehunt.llm

import jobs.vibehunt.config.LlmConfig

object LlmClientFactory {
    fun create(config: LlmConfig): LlmClient {
        if (config.useStub) {
            return StubLlmClient()
        }
        return if (config.isOllama) {
            OllamaLlmClient(config)
        } else {
            OpenAiCompatibleLlmClient(config)
        }
    }
}
