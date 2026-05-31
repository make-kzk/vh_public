package jobs.vibehunt.auth

import jobs.vibehunt.db.EmployerRepository
import jobs.vibehunt.db.SeekerRepository
import jobs.vibehunt.db.UserRepository
import jobs.vibehunt.domain.ProfileProvisioningService
import java.util.UUID

class UserAuthService(
    private val userRepository: UserRepository,
    private val profileProvisioningService: ProfileProvisioningService,
    private val seekerRepository: SeekerRepository,
    private val employerRepository: EmployerRepository,
) {
    fun findOrCreateDevUser(email: String): AuthUserDto {
        val normalizedEmail = email.trim().lowercase()
        require(normalizedEmail.contains('@')) { "Некорректный адрес электронной почты" }
        val user = userRepository.findByEmail(normalizedEmail) ?: userRepository.create(
            email = normalizedEmail,
            authProvider = DEV_PROVIDER,
            authSubject = normalizedEmail,
        )
        return withProfileName(user)
    }

    fun completeRegistration(userId: UUID, request: CompleteRegistrationRequest): AuthUserDto {
        val user =
            userRepository.findById(userId)
                ?: error("Пользователь не найден")
        if (user.role != null) {
            error("Роль уже выбрана и не может быть изменена")
        }
        val profileData = validateRegistrationProfile(request)
        val updated =
            userRepository.setRole(userId, request.role)
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
        return withProfileName(updated)
    }

    fun deleteAccount(userId: UUID) {
        if (!userRepository.deleteById(userId)) {
            error("Пользователь не найден")
        }
    }

    fun withProfileName(user: AuthUserDto): AuthUserDto {
        val role = user.role ?: return user.copy(profileName = null)
        val profileName =
            when (role) {
                UserRole.SEEKER ->
                    seekerRepository.findByUserId(UUID.fromString(user.id))?.let { seeker ->
                        listOf(seeker.firstName, seeker.lastName)
                            .joinToString(" ")
                            .trim()
                            .ifBlank { null }
                    }
                UserRole.EMPLOYER ->
                    employerRepository.findByUserId(UUID.fromString(user.id))?.name?.trim()?.ifBlank { null }
            }
        return user.copy(profileName = profileName)
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
