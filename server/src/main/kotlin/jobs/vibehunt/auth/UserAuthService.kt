package jobs.vibehunt.auth

import jobs.vibehunt.db.UserRepository
import java.util.UUID

class UserAuthService(
    private val userRepository: UserRepository,
) {
    fun findOrCreateDevUser(): AuthUserDto {
        val existing = userRepository.findByAuthKey(DEV_PROVIDER, DEV_SUBJECT)
        if (existing != null) {
            return existing
        }
        return userRepository.create(
            email = DEV_EMAIL,
            displayName = DEV_DISPLAY_NAME,
            authProvider = DEV_PROVIDER,
            authSubject = DEV_SUBJECT,
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
        const val DEV_SUBJECT = "local"
        const val DEV_EMAIL = "dev@localhost.vibehunt"
        const val DEV_DISPLAY_NAME = "Dev User"
    }
}
