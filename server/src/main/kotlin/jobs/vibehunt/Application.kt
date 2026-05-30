package jobs.vibehunt

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jobs.vibehunt.auth.SessionService
import jobs.vibehunt.auth.UserAuthService
import jobs.vibehunt.config.AppConfig
import jobs.vibehunt.db.DatabaseFactory
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
    val sessionService = SessionService(config, sessionRepository, userRepository)
    val userAuthService = UserAuthService(userRepository)

    configureSerialization()
    configureCors(config)

    routing {
        get("/") {
            call.respondText("VibeHunt API")
        }
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
        authRoutes(config, userAuthService, sessionService)
    }
}
