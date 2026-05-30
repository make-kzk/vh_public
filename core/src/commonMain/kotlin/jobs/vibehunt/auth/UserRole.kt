package jobs.vibehunt.auth

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    SEEKER,
    EMPLOYER,
}
