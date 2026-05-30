package jobs.vibehunt.auth

interface AuthRepository {
    suspend fun fetchMe(): AuthUserDto?

    suspend fun startOAuth(provider: OAuthProvider, redirectUri: String): OAuthStartResponse

    suspend fun logout()

    suspend fun completeRegistration(role: UserRole): AuthUserDto
}
