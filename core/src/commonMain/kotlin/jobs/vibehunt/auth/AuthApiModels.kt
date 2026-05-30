package jobs.vibehunt.auth

import kotlinx.serialization.Serializable

@Serializable
data class CompleteRegistrationRequest(
    val role: UserRole,
)

@Serializable
data class MeResponse(
    val user: AuthUserDto?,
)
