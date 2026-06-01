package jobs.vibehunt

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jobs.vibehunt.auth.RoleGuard
import jobs.vibehunt.auth.SessionService
import jobs.vibehunt.auth.UserAuthService
import jobs.vibehunt.config.AppConfig
import jobs.vibehunt.db.DatabaseFactory
import jobs.vibehunt.db.EmployerRepository
import jobs.vibehunt.db.ReferenceRepository
import jobs.vibehunt.db.SeekerRepository
import jobs.vibehunt.db.SurveyRepository
import jobs.vibehunt.db.SessionRepository
import jobs.vibehunt.db.UserRepository
import jobs.vibehunt.domain.EmployerProfileService
import jobs.vibehunt.domain.ProfileProvisioningService
import jobs.vibehunt.domain.SeekerProfileService
import jobs.vibehunt.domain.SurveyService
import jobs.vibehunt.plugins.configureCors
import jobs.vibehunt.plugins.configureSerialization
import jobs.vibehunt.routes.authRoutes
import jobs.vibehunt.routes.employerRoutes
import jobs.vibehunt.routes.referenceRoutes
import jobs.vibehunt.routes.seekerRoutes

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val config = AppConfig.fromEnvironment()
    DatabaseFactory.init(config)

    val userRepository = UserRepository()
    val sessionRepository = SessionRepository()
    val seekerRepository = SeekerRepository()
    val employerRepository = EmployerRepository()
    val referenceRepository = ReferenceRepository()
    val profileProvisioningService = ProfileProvisioningService(seekerRepository, employerRepository)
    val userAuthService =
        UserAuthService(userRepository, profileProvisioningService, seekerRepository, employerRepository)
    val sessionService = SessionService(config, sessionRepository, userRepository, userAuthService::withProfileName)
    val roleGuard = RoleGuard(config, sessionService)
    val surveyRepository = SurveyRepository()
    val surveyService = SurveyService(seekerRepository, surveyRepository)
    val seekerProfileService = SeekerProfileService(seekerRepository, referenceRepository, surveyService)
    val employerProfileService = EmployerProfileService(employerRepository, referenceRepository)

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
        referenceRoutes(roleGuard, referenceRepository)
        seekerRoutes(roleGuard, seekerProfileService, surveyService)
        employerRoutes(roleGuard, employerProfileService)
    }
}
