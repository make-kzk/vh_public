package jobs.vibehunt

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jobs.vibehunt.auth.OAuthService
import jobs.vibehunt.auth.SessionService
import jobs.vibehunt.auth.oauth.AppleOAuthClient
import jobs.vibehunt.auth.oauth.GoogleOAuthClient
import jobs.vibehunt.config.AppConfig
import jobs.vibehunt.db.DatabaseFactory
import jobs.vibehunt.db.OAuthStateRepository
import jobs.vibehunt.db.SessionRepository
import jobs.vibehunt.db.UserRepository
import jobs.vibehunt.plugins.configureCors
import jobs.vibehunt.plugins.configureSerialization
import jobs.vibehunt.routes.authRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val config = AppConfig.fromEnvironment()
    DatabaseFactory.init(config)

    val userRepository = UserRepository()
    val sessionRepository = SessionRepository()
    val oauthStateRepository = OAuthStateRepository()
    val sessionService = SessionService(config, sessionRepository, userRepository)
    val googleClient = if (config.googleEnabled) GoogleOAuthClient(config) else null
    val appleClient = if (config.appleEnabled) AppleOAuthClient(config) else null
    val oauthService =
        OAuthService(
            config = config,
            oauthStateRepository = oauthStateRepository,
            userRepository = userRepository,
            googleOAuthClient = googleClient,
            appleOAuthClient = appleClient,
        )

    configureSerialization()
    configureCors(config)

    routing {
        get("/") {
            call.respondText("VibeHunt API")
        }
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
        authRoutes(config, oauthService, sessionService)
    }
}
