package jobs.vibehunt.auth

import jobs.vibehunt.db.UserRepository
import jobs.vibehunt.domain.ProfileProvisioningService
import java.util.UUID

class UserAuthService(
    private val userRepository: UserRepository,
    private val profileProvisioningService: ProfileProvisioningService,
) {
    fun findOrCreateDevUser(email: String): AuthUserDto {
        val normalizedEmail = email.trim().lowercase()
        require(normalizedEmail.contains('@')) { "Некорректный адрес электронной почты" }
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
                ?: error("Пользователь не найден")
        if (user.role != null) {
            error("Роль уже выбрана и не может быть изменена")
        }
        return userRepository.setRole(userId, role)
            ?.also { profileProvisioningService.provisionForRole(userId, role) }
            ?: error("Не удалось установить роль")
    }

    private companion object {
        const val DEV_PROVIDER = "dev"
    }
}
