package jobs.vibehunt.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*

class JsAuthRepository(
    private val httpClient: HttpClient = createAuthHttpClient(),
) : AuthRepository {
    override suspend fun fetchMe(): AuthUserDto? {
        val response = httpClient.authGet("/api/auth/me")
        if (!response.status.isSuccess()) return null
        return response.body<MeResponse>().user
    }

    override suspend fun devLogin(email: String): AuthUserDto {
        val response =
            httpClient.authPost(
                "/api/auth/dev/login",
                DevLoginRequest(email = email),
            )
        if (!response.status.isSuccess()) {
            throw AuthException("Failed to sign in: ${response.status}")
        }
        return response.body()
    }

    override suspend fun logout() {
        httpClient.authPost("/api/auth/logout")
    }

    override suspend fun completeRegistration(role: UserRole): AuthUserDto {
        val response =
            httpClient.authPost(
                "/api/auth/complete-registration",
                CompleteRegistrationRequest(role = role),
            )
        if (!response.status.isSuccess()) {
            throw AuthException("Failed to complete registration: ${response.status}")
        }
        return response.body()
    }
}

class AuthException(message: String) : Exception(message)
