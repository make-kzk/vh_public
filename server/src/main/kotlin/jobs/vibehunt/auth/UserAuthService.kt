package jobs.vibehunt.auth

import jobs.vibehunt.db.UserRepository
import java.util.UUID

class UserAuthService(
    private val userRepository: UserRepository,
) {
    fun findOrCreateDevUser(email: String): AuthUserDto {
        val normalizedEmail = email.trim().lowercase()
        require(normalizedEmail.contains('@')) { "Invalid email address" }
        userRepository.findByEmail(normalizedEmail)?.let { return it }
        return userRepository.create(
            email = normalizedEmail,
            displayName = normalizedEmail.substringBefore('@'),
            authProvider = DEV_PROVIDER,
            authSubject = normalizedEmail,
        )
    }

    fun completeRegistration(userId: UUID, role: UserRole): AuthUserDto {
        val user =
            userRepository.findById(userId)
                ?: error("User not found")
        if (user.role != null) {
            error("Role is already set and cannot be changed")
        }
        return userRepository.setRole(userId, role)
            ?: error("Failed to set role")
    }

    private companion object {
        const val DEV_PROVIDER = "dev"
    }
}
