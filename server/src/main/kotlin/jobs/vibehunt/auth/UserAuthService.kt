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

    fun completeRegistration(userId: UUID, request: CompleteRegistrationRequest): AuthUserDto {
        val user =
            userRepository.findById(userId)
                ?: error("Пользователь не найден")
        if (user.role != null) {
            error("Роль уже выбрана и не может быть изменена")
        }
        val profileData = validateRegistrationProfile(request)
        return userRepository.setRole(userId, request.role)
            ?.also {
                profileProvisioningService.provisionForRole(
                    userId = userId,
                    role = request.role,
                    firstName = profileData.firstName,
                    lastName = profileData.lastName,
                    middleName = profileData.middleName,
                    companyName = profileData.companyName,
                )
            }
            ?: error("Не удалось установить роль")
    }

    private data class RegistrationProfileData(
        val firstName: String = "",
        val lastName: String = "",
        val middleName: String? = null,
        val companyName: String = "",
    )

    private fun validateRegistrationProfile(request: CompleteRegistrationRequest): RegistrationProfileData =
        when (request.role) {
            UserRole.SEEKER -> {
                val firstName = request.firstName?.trim().orEmpty()
                val lastName = request.lastName?.trim().orEmpty()
                require(firstName.isNotBlank()) { "Укажите имя" }
                require(lastName.isNotBlank()) { "Укажите фамилию" }
                RegistrationProfileData(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = request.middleName?.trim()?.ifBlank { null },
                )
            }
            UserRole.EMPLOYER -> {
                val companyName = request.companyName?.trim().orEmpty()
                require(companyName.isNotBlank()) { "Укажите название компании" }
                RegistrationProfileData(companyName = companyName)
            }
        }

    private companion object {
        const val DEV_PROVIDER = "dev"
    }
}
