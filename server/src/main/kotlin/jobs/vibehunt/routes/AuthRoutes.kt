package jobs.vibehunt.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jobs.vibehunt.auth.CompleteRegistrationRequest
import jobs.vibehunt.auth.DevLoginRequest
import jobs.vibehunt.auth.MeResponse
import jobs.vibehunt.auth.SessionService
import jobs.vibehunt.auth.UserAuthService
import jobs.vibehunt.config.AppConfig
import java.util.UUID

fun Route.authRoutes(
    config: AppConfig,
    userAuthService: UserAuthService,
    sessionService: SessionService,
) {
    route("/api/auth") {
        post("/dev/login") {
            if (!config.authDevMode) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf(
                        "error" to "dev_auth_disabled",
                        "message" to "Set AUTH_DEV_MODE=true in .env to enable dev sign-in.",
                    ),
                )
                return@post
            }
            val body = call.receive<DevLoginRequest>()
            val user =
                try {
                    userAuthService.findOrCreateDevUser(body.email)
                } catch (e: IllegalArgumentException) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.message ?: "Invalid email")))
                }
            val sessionToken = sessionService.createSession(UUID.fromString(user.id))
            call.setSessionCookie(config, sessionToken)
            call.respond(user)
        }

        get("/me") {
            val token = call.request.cookies[config.sessionCookieName]
            val user = sessionService.resolveUser(token)
            call.respond(MeResponse(user = user))
        }

        post("/logout") {
            val token = call.request.cookies[config.sessionCookieName]
            sessionService.invalidate(token)
            call.clearSessionCookie(config)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/complete-registration") {
            val token =
                call.request.cookies[config.sessionCookieName]
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
            val userId =
                sessionService.resolveUser(token)?.id?.let(UUID::fromString)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
            val existing = sessionService.resolveUser(token)!!
            if (existing.role != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Role already set")
            }
            val body = call.receive<CompleteRegistrationRequest>()
            val updated = userAuthService.completeRegistration(userId, body.role)
            call.respond(updated)
        }
    }
}

private fun ApplicationCall.setSessionCookie(config: AppConfig, token: String) {
    response.cookies.append(
        Cookie(
            name = config.sessionCookieName,
            value = token,
            httpOnly = true,
            path = "/",
            maxAge = (config.sessionDays * 24 * 60 * 60).toInt(),
            extensions = mapOf("SameSite" to "Lax"),
        ),
    )
}

private fun ApplicationCall.clearSessionCookie(config: AppConfig) {
    response.cookies.append(
        Cookie(
            name = config.sessionCookieName,
            value = "",
            httpOnly = true,
            path = "/",
            maxAge = 0,
            extensions = mapOf("SameSite" to "Lax"),
        ),
    )
}
