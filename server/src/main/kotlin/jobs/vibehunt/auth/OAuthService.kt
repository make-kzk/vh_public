package jobs.vibehunt.auth

import jobs.vibehunt.auth.oauth.AppleOAuthClient
import jobs.vibehunt.auth.oauth.GoogleOAuthClient
import jobs.vibehunt.auth.oauth.OAuthUserInfo
import jobs.vibehunt.config.AppConfig
import jobs.vibehunt.db.OAuthStateRepository
import jobs.vibehunt.db.UserRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.UUID

class OAuthService(
    private val config: AppConfig,
    private val oauthStateRepository: OAuthStateRepository,
    private val userRepository: UserRepository,
    private val googleOAuthClient: GoogleOAuthClient?,
    private val appleOAuthClient: AppleOAuthClient?,
) {
    fun startOAuth(provider: OAuthProvider, redirectUri: String): OAuthStartResponse {
        when (provider) {
            OAuthProvider.GOOGLE ->
                require(config.googleEnabled || config.googleDevMockEnabled) {
                    "Google OAuth is not configured"
                }
            OAuthProvider.APPLE ->
                require(config.appleEnabled || config.appleDevMockEnabled) {
                    "Apple OAuth is not configured"
                }
        }
        val state = Pkce.generateState()
        val codeVerifier = Pkce.generateCodeVerifier()
        val codeChallenge = Pkce.codeChallengeS256(codeVerifier)
        oauthStateRepository.save(
            state = state,
            codeVerifier = codeVerifier,
            provider = provider,
            redirectUri = redirectUri,
            expiresAt = OffsetDateTime.now().plusMinutes(10),
        )
        val authorizationUrl =
            when (provider) {
                OAuthProvider.GOOGLE ->
                    when {
                        config.googleEnabled ->
                            googleOAuthClient!!.buildAuthorizationUrl(state, codeChallenge)
                        else -> devMockAuthorizationUrl(OAuthProvider.GOOGLE, state)
                    }
                OAuthProvider.APPLE ->
                    when {
                        config.appleEnabled ->
                            appleOAuthClient!!.buildAuthorizationUrl(state)
                        else -> devMockAuthorizationUrl(OAuthProvider.APPLE, state)
                    }
            }
        return OAuthStartResponse(authorizationUrl = authorizationUrl, state = state)
    }

    suspend fun completeDevMockOAuth(provider: OAuthProvider, state: String): AuthUserDto {
        require(
            when (provider) {
                OAuthProvider.GOOGLE -> config.googleDevMockEnabled
                OAuthProvider.APPLE -> config.appleDevMockEnabled
            },
        ) { "Dev OAuth mock is not enabled for $provider" }
        val stored =
            oauthStateRepository.consume(state)
                ?: error("Invalid or expired OAuth state")
        if (stored.provider != provider) {
            error("OAuth provider mismatch")
        }
        return upsertUser(
            provider,
            OAuthUserInfo(
                subject = "dev-${provider.name.lowercase()}",
                email = "dev-${provider.name.lowercase()}@localhost.vibehunt",
                displayName = "Dev User (${provider.name})",
            ),
        )
    }

    suspend fun completeOAuth(
        provider: OAuthProvider,
        code: String,
        state: String,
    ): AuthUserDto {
        val stored =
            oauthStateRepository.consume(state)
                ?: error("Invalid or expired OAuth state")
        if (stored.provider != provider) {
            error("OAuth provider mismatch")
        }
        val userInfo =
            when (provider) {
                OAuthProvider.GOOGLE ->
                    googleOAuthClient!!.exchangeCode(code, stored.codeVerifier)
                OAuthProvider.APPLE ->
                    appleOAuthClient!!.exchangeCode(code)
            }
        return upsertUser(provider, userInfo)
    }

    private fun devMockAuthorizationUrl(provider: OAuthProvider, state: String): String {
        val encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8)
        val segment =
            when (provider) {
                OAuthProvider.GOOGLE -> "google"
                OAuthProvider.APPLE -> "apple"
            }
        return "${config.serverPublicUrl}/api/auth/dev/oauth/$segment?state=$encodedState"
    }

    private fun upsertUser(provider: OAuthProvider, userInfo: OAuthUserInfo): AuthUserDto {
        val existing = userRepository.findByOAuth(provider, userInfo.subject)
        if (existing != null) {
            return existing
        }
        return userRepository.create(
            email = userInfo.email,
            displayName = userInfo.displayName,
            provider = provider,
            subject = userInfo.subject,
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
}
