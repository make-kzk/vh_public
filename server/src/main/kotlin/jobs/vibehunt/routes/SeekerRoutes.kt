package jobs.vibehunt.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jobs.vibehunt.auth.RoleGuard
import jobs.vibehunt.auth.UserRole
import jobs.vibehunt.db.ReferenceRepository
import jobs.vibehunt.domain.SeekerProfileService
import jobs.vibehunt.models.CreateSeekerEducationRequest
import jobs.vibehunt.models.CreateSeekerExperienceRequest
import jobs.vibehunt.models.UpdateSeekerDesiredPositionsRequest
import jobs.vibehunt.models.UpdateSeekerEducationRequest
import jobs.vibehunt.models.UpdateSeekerExperienceRequest
import jobs.vibehunt.models.UpdateSeekerProfileRequest
import jobs.vibehunt.models.UpdateSeekerSkillsRequest

fun Route.referenceRoutes(
    roleGuard: RoleGuard,
    referenceRepository: ReferenceRepository,
) {
    route("/api") {
        get("/occupations") {
            roleGuard.requireAuth(call) ?: return@get
            val leafOnly = call.request.queryParameters["leafOnly"]?.toBooleanStrictOrNull() ?: false
            call.respond(referenceRepository.listOccupations(leafOnly))
        }
        get("/skills") {
            roleGuard.requireAuth(call) ?: return@get
            val query = call.request.queryParameters["q"]
            call.respond(referenceRepository.searchSkills(query))
        }
    }
}

fun Route.seekerRoutes(
    roleGuard: RoleGuard,
    seekerProfileService: SeekerProfileService,
) {
    route("/api/seeker") {
        get("/dashboard") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.dashboard(user.id))
        }
        get("/me") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.getOrCreateSeeker(user.id))
        }
        patch("/me") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@patch
            val body = call.receive<UpdateSeekerProfileRequest>()
            seekerProfileService.updateProfile(user.id, body)
            call.respond(seekerProfileService.getOrCreateSeeker(user.id))
        }
        get("/experience") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.listExperience(user.id))
        }
        post("/experience") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@post
            val body = call.receive<CreateSeekerExperienceRequest>()
            call.respond(HttpStatusCode.Created, seekerProfileService.createExperience(user.id, body))
        }
        patch("/experience/{id}") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@patch
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Некорректный id"))
            val body = call.receive<UpdateSeekerExperienceRequest>()
            try {
                call.respond(seekerProfileService.updateExperience(user.id, id, body))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to (e.message ?: "Не найдено")))
            }
        }
        delete("/experience/{id}") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@delete
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Некорректный id"))
            try {
                seekerProfileService.deleteExperience(user.id, id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to (e.message ?: "Не найдено")))
            }
        }
        get("/education") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.listEducation(user.id))
        }
        post("/education") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@post
            val body = call.receive<CreateSeekerEducationRequest>()
            call.respond(HttpStatusCode.Created, seekerProfileService.createEducation(user.id, body))
        }
        patch("/education/{id}") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@patch
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Некорректный id"))
            val body = call.receive<UpdateSeekerEducationRequest>()
            try {
                call.respond(seekerProfileService.updateEducation(user.id, id, body))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to (e.message ?: "Не найдено")))
            }
        }
        delete("/education/{id}") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@delete
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Некорректный id"))
            try {
                seekerProfileService.deleteEducation(user.id, id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to (e.message ?: "Не найдено")))
            }
        }
        get("/skills") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.getSkills(user.id))
        }
        put("/skills") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@put
            val body = call.receive<UpdateSeekerSkillsRequest>()
            call.respond(seekerProfileService.setSkills(user.id, body.skillIds))
        }
        get("/desired-positions") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.getDesiredPositions(user.id))
        }
        put("/desired-positions") {
            val user = roleGuard.requireRole(call, UserRole.SEEKER) ?: return@put
            val body = call.receive<UpdateSeekerDesiredPositionsRequest>()
            try {
                call.respond(seekerProfileService.setDesiredPositions(user.id, body.occupationIds))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to (e.message ?: "Ошибка")))
            }
        }
        get("/personality-preview") {
            roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.personalityPreview())
        }
        get("/recommendations") {
            roleGuard.requireRole(call, UserRole.SEEKER) ?: return@get
            call.respond(seekerProfileService.recommendations())
        }
    }
}
