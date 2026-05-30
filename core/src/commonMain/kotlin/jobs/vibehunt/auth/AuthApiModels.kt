package jobs.vibehunt.auth

import kotlinx.serialization.Serializable

@Serializable
enum class OAuthProvider {
    GOOGLE,
    APPLE,
}

@Serializable
data class OAuthStartRequest(
    val provider: OAuthProvider,
    val redirectUri: String,
)

@Serializable
data class OAuthStartResponse(
    val authorizationUrl: String,
    val state: String,
)

@Serializable
data class CompleteRegistrationRequest(
    val role: UserRole,
)

@Serializable
data class MeResponse(
    val user: AuthUserDto?,
)
