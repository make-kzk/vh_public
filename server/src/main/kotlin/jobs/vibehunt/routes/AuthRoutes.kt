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
                        "message" to "Установите AUTH_DEV_MODE=true в .env, чтобы включить вход для разработки.",
                    ),
                )
                return@post
            }
            val body = call.receive<DevLoginRequest>()
            val user =
                try {
                    userAuthService.findOrCreateDevUser(body.email)
                } catch (e: IllegalArgumentException) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.message ?: "Некорректный email")))
                }
            val sessionToken = sessionService.createSession(UUID.fromString(user.id))
            call.setSessionCookie(config, sessionToken)
            call.respond(userAuthService.withProfileName(user))
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
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Не авторизован")
            val userId =
                sessionService.resolveUser(token)?.id?.let(UUID::fromString)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Не авторизован")
            val existing = sessionService.resolveUser(token)!!
            if (existing.role != null) {
                return@post call.respond(HttpStatusCode.Conflict, "Роль уже выбрана")
            }
            val body = call.receive<CompleteRegistrationRequest>()
            val updated =
                try {
                    userAuthService.completeRegistration(userId, body)
                } catch (e: IllegalArgumentException) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.message ?: "Некорректные данные")))
                }
            call.respond(userAuthService.withProfileName(updated))
        }

        delete("/account") {
            val token =
                call.request.cookies[config.sessionCookieName]
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Не авторизован")
            val user =
                sessionService.resolveUser(token)
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Не авторизован")
            val userId =
                try {
                    java.util.UUID.fromString(user.id)
                } catch (_: IllegalArgumentException) {
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Некорректный id пользователя"))
                }
            sessionService.invalidate(token)
            try {
                userAuthService.deleteAccount(userId)
            } catch (e: IllegalStateException) {
                return@delete call.respond(HttpStatusCode.NotFound, mapOf("message" to (e.message ?: "Пользователь не найден")))
            }
            call.clearSessionCookie(config)
            call.respond(HttpStatusCode.NoContent)
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
