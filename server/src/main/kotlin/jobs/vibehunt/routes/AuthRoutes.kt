package jobs.vibehunt.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jobs.vibehunt.auth.OAuthProvider
import jobs.vibehunt.auth.OAuthService
import jobs.vibehunt.auth.OAuthStartRequest
import jobs.vibehunt.auth.CompleteRegistrationRequest
import jobs.vibehunt.auth.MeResponse
import jobs.vibehunt.auth.SessionService
import jobs.vibehunt.config.AppConfig
import java.util.UUID

fun Route.authRoutes(
    config: AppConfig,
    oauthService: OAuthService,
    sessionService: SessionService,
) {
    route("/api/auth") {
        post("/oauth/start") {
            val request = call.receive<OAuthStartRequest>()
            when (request.provider) {
                OAuthProvider.GOOGLE ->
                    if (!config.googleEnabled && !config.googleDevMockEnabled) {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf(
                                "error" to "oauth_not_configured",
                                "message" to "Set GOOGLE_* in .env or OAUTH_DEV_MOCK=true for local dev.",
                            ),
                        )
                        return@post
                    }
                OAuthProvider.APPLE ->
                    if (!config.appleEnabled && !config.appleDevMockEnabled) {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            mapOf(
                                "error" to "oauth_not_configured",
                                "message" to "Set Apple OAuth in .env or OAUTH_DEV_MOCK=true for local dev.",
                            ),
                        )
                        return@post
                    }
            }
            val response = oauthService.startOAuth(request.provider, request.redirectUri)
            call.respond(response)
        }

        get("/dev/oauth/google") {
            if (!config.googleDevMockEnabled) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val state =
                call.request.queryParameters["state"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing state")
            finishDevMockOAuth(call, config, oauthService, sessionService, OAuthProvider.GOOGLE, state)
        }

        get("/dev/oauth/apple") {
            if (!config.appleDevMockEnabled) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val state =
                call.request.queryParameters["state"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing state")
            finishDevMockOAuth(call, config, oauthService, sessionService, OAuthProvider.APPLE, state)
        }

        get("/oauth/callback/google") {
            handleOAuthCallback(call, config, oauthService, sessionService, OAuthProvider.GOOGLE)
        }

        post("/oauth/callback/apple") {
            val parameters = call.receiveParameters()
            val code = parameters["code"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing code")
            val state = parameters["state"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing state")
            finishOAuth(call, config, oauthService, sessionService, OAuthProvider.APPLE, code, state)
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
            val updated = oauthService.completeRegistration(userId, body.role)
            call.respond(updated)
        }
    }
}

private suspend fun finishDevMockOAuth(
    call: ApplicationCall,
    config: AppConfig,
    oauthService: OAuthService,
    sessionService: SessionService,
    provider: OAuthProvider,
    state: String,
) {
    try {
        val user = oauthService.completeDevMockOAuth(provider, state)
        val sessionToken = sessionService.createSession(UUID.fromString(user.id))
        call.setSessionCookie(config, sessionToken)
        val path =
            if (user.role == null) "/auth/role" else "/auth/callback?success=true"
        call.respondRedirect("${config.frontendUrl}$path")
    } catch (e: Exception) {
        call.application.environment.log.error("Dev mock OAuth failed", e)
        call.respondRedirect("${config.frontendUrl}/auth/callback?error=oauth_failed")
    }
}

private suspend fun handleOAuthCallback(
    call: ApplicationCall,
    config: AppConfig,
    oauthService: OAuthService,
    sessionService: SessionService,
    provider: OAuthProvider,
) {
    val code = call.request.queryParameters["code"]
    val state = call.request.queryParameters["state"]
    if (code == null || state == null) {
        call.respondRedirect("${config.frontendUrl}/auth/callback?error=missing_params")
        return
    }
    finishOAuth(call, config, oauthService, sessionService, provider, code, state)
}

private suspend fun finishOAuth(
    call: ApplicationCall,
    config: AppConfig,
    oauthService: OAuthService,
    sessionService: SessionService,
    provider: OAuthProvider,
    code: String,
    state: String,
) {
    try {
        val user = oauthService.completeOAuth(provider, code, state)
        val sessionToken = sessionService.createSession(UUID.fromString(user.id))
        call.setSessionCookie(config, sessionToken)
        val needsRegistration = user.role == null
        val path =
            if (needsRegistration) {
                "/auth/role"
            } else {
                "/auth/callback?success=true"
            }
        call.respondRedirect("${config.frontendUrl}$path")
    } catch (e: Exception) {
        call.application.environment.log.error("OAuth callback failed", e)
        call.respondRedirect("${config.frontendUrl}/auth/callback?error=oauth_failed")
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
