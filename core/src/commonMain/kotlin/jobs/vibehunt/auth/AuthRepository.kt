package jobs.vibehunt.auth

interface AuthRepository {
    suspend fun fetchMe(): AuthUserDto?

    suspend fun devLogin(): AuthUserDto

    suspend fun logout()

    suspend fun completeRegistration(role: UserRole): AuthUserDto
}
