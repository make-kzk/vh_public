package jobs.vibehunt.auth.oauth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import jobs.vibehunt.config.AppConfig
import java.net.URLEncoder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GoogleOAuthClient(private val config: AppConfig) {
    private val http =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    fun buildAuthorizationUrl(state: String, codeChallenge: String): String {
        val params =
            listOf(
                "client_id" to config.googleClientId!!,
                "redirect_uri" to config.googleRedirectUri!!,
                "response_type" to "code",
                "scope" to "openid email profile",
                "state" to state,
                "code_challenge" to codeChallenge,
                "code_challenge_method" to "S256",
                "access_type" to "offline",
                "prompt" to "consent",
            )
        return "https://accounts.google.com/o/oauth2/v2/auth?" + params.toQueryString()
    }

    suspend fun exchangeCode(code: String, codeVerifier: String): OAuthUserInfo {
        val tokenResponse: GoogleTokenResponse =
            http.submitForm(
                url = "https://oauth2.googleapis.com/token",
                formParameters =
                    Parameters.build {
                        append("client_id", config.googleClientId!!)
                        append("client_secret", config.googleClientSecret!!)
                        append("code", code)
                        append("code_verifier", codeVerifier)
                        append("grant_type", "authorization_code")
                        append("redirect_uri", config.googleRedirectUri!!)
                    },
            ).body()

        val userInfo: GoogleUserInfo =
            http.get("https://www.googleapis.com/oauth2/v3/userinfo") {
                header(HttpHeaders.Authorization, "Bearer ${tokenResponse.accessToken}")
            }.body()

        return OAuthUserInfo(
            subject = userInfo.sub,
            email = userInfo.email,
            displayName = userInfo.name,
        )
    }

    @Serializable
    private data class GoogleTokenResponse(
        @SerialName("access_token") val accessToken: String,
    )

    @Serializable
    private data class GoogleUserInfo(
        val sub: String,
        val email: String,
        val name: String? = null,
    )

    private fun List<Pair<String, String>>.toQueryString(): String =
        joinToString("&") { (key, value) ->
            "${urlEncode(key)}=${urlEncode(value)}"
        }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8)
}
