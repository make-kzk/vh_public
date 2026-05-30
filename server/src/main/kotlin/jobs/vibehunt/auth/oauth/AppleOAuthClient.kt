package jobs.vibehunt.auth.oauth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

class AppleOAuthClient(private val config: AppConfig) {
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

    fun buildAuthorizationUrl(state: String): String {
        val params =
            listOf(
                "client_id" to config.appleClientId!!,
                "redirect_uri" to config.appleRedirectUri!!,
                "response_type" to "code",
                "response_mode" to "form_post",
                "scope" to "name email",
                "state" to state,
            )
        return "https://appleid.apple.com/auth/authorize?" + params.toQueryString()
    }

    suspend fun exchangeCode(code: String): OAuthUserInfo {
        val clientSecret = createClientSecret()
        val tokenResponse: AppleTokenResponse =
            http.submitForm(
                url = "https://appleid.apple.com/auth/token",
                formParameters =
                    Parameters.build {
                        append("client_id", config.appleClientId!!)
                        append("client_secret", clientSecret)
                        append("code", code)
                        append("grant_type", "authorization_code")
                        append("redirect_uri", config.appleRedirectUri!!)
                    },
            ).body()

        val idToken = tokenResponse.idToken
        val decodedJwt = JWT.decode(idToken)
        val jwk = JwtProviders.applePublicKeyProvider().get(decodedJwt.keyId)
        val algorithm = Algorithm.RSA256(jwk.publicKey as java.security.interfaces.RSAPublicKey, null)
        val decoded =
            JWT.require(algorithm)
                .withIssuer("https://appleid.apple.com")
                .withAudience(config.appleClientId)
                .build()
                .verify(idToken)
        val email = decoded.getClaim("email").asString()
            ?: error("Apple ID token missing email claim")
        val subject = decoded.subject
        return OAuthUserInfo(
            subject = subject,
            email = email,
            displayName = null,
        )
    }

    private fun createClientSecret(): String {
        val now = Instant.now()
        val key = loadPrivateKey(config.applePrivateKey!!)
        val algorithm = Algorithm.ECDSA256(null, key)
        return JWT.create()
            .withIssuer(config.appleTeamId)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(300)))
            .withAudience("https://appleid.apple.com")
            .withSubject(config.appleClientId)
            .withKeyId(config.appleKeyId)
            .sign(algorithm)
    }

    private fun loadPrivateKey(pem: String): ECPrivateKey {
        val normalized =
            pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")
        val decoded = Base64.getDecoder().decode(normalized)
        val spec = PKCS8EncodedKeySpec(decoded)
        return KeyFactory.getInstance("EC").generatePrivate(spec) as ECPrivateKey
    }

    @Serializable
    private data class AppleTokenResponse(
        @SerialName("id_token") val idToken: String,
    )

    private fun List<Pair<String, String>>.toQueryString(): String =
        joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, Charsets.UTF_8)}=${URLEncoder.encode(value, Charsets.UTF_8)}"
        }
}
