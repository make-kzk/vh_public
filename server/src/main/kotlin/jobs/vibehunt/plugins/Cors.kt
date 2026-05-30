package jobs.vibehunt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import jobs.vibehunt.config.AppConfig

fun Application.configureCors(config: AppConfig) {
    val origin = config.webOrigin
    val hostWithPort = origin.removePrefix("http://").removePrefix("https://")
    val scheme = if (origin.startsWith("https")) "https" else "http"

    install(CORS) {
        allowHost(hostWithPort, schemes = listOf(scheme))
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
    }
}
