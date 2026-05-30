package jobs.vibehunt.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val role: UserRole? = null,
)
