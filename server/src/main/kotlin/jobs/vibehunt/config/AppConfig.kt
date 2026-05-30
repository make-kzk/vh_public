package jobs.vibehunt.config

data class AppConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val webOrigin: String,
    val frontendUrl: String,
    val sessionCookieName: String,
    val sessionDays: Long,
    val googleClientId: String?,
    val googleClientSecret: String?,
    val googleRedirectUri: String?,
    val appleClientId: String?,
    val appleTeamId: String?,
    val appleKeyId: String?,
    val applePrivateKey: String?,
    val appleRedirectUri: String?,
    val oauthDevMock: Boolean,
    val serverPublicUrl: String,
) {
    val googleEnabled: Boolean =
        !googleClientId.isNullOrBlank() &&
            !googleClientSecret.isNullOrBlank() &&
            !googleRedirectUri.isNullOrBlank()

    val appleEnabled: Boolean =
        !appleClientId.isNullOrBlank() &&
            !appleTeamId.isNullOrBlank() &&
            !appleKeyId.isNullOrBlank() &&
            !applePrivateKey.isNullOrBlank() &&
            !appleRedirectUri.isNullOrBlank()

    val googleDevMockEnabled: Boolean = oauthDevMock && !googleEnabled

    val appleDevMockEnabled: Boolean = oauthDevMock && !appleEnabled

    companion object {
        fun fromEnvironment(): AppConfig {
            val dotEnv = DotEnv.load()
            return AppConfig(
                databaseUrl = env("DATABASE_URL", "jdbc:postgresql://localhost:5432/vibehunt", dotEnv),
                databaseUser = env("DATABASE_USER", "vibehunt", dotEnv),
                databasePassword = env("DATABASE_PASSWORD", "vibehunt", dotEnv),
                webOrigin = env("WEB_ORIGIN", "http://localhost:8081", dotEnv),
                frontendUrl = env("FRONTEND_URL", "http://localhost:8081", dotEnv),
                sessionCookieName = env("SESSION_COOKIE_NAME", "vibehunt_session", dotEnv),
                sessionDays = env("SESSION_DAYS", "30", dotEnv).toLong(),
                googleClientId = envOrNull("GOOGLE_CLIENT_ID", dotEnv),
                googleClientSecret = envOrNull("GOOGLE_CLIENT_SECRET", dotEnv),
                googleRedirectUri = envOrNull("GOOGLE_REDIRECT_URI", dotEnv),
                appleClientId = envOrNull("APPLE_CLIENT_ID", dotEnv),
                appleTeamId = envOrNull("APPLE_TEAM_ID", dotEnv),
                appleKeyId = envOrNull("APPLE_KEY_ID", dotEnv),
                applePrivateKey = envOrNull("APPLE_PRIVATE_KEY", dotEnv)?.replace("\\n", "\n"),
                appleRedirectUri = envOrNull("APPLE_REDIRECT_URI", dotEnv),
                oauthDevMock = env("OAUTH_DEV_MOCK", "false", dotEnv).equals("true", ignoreCase = true),
                serverPublicUrl = env("SERVER_PUBLIC_URL", "http://localhost:8080", dotEnv),
            )
        }

        private fun env(name: String, default: String, dotEnv: Map<String, String>): String =
            resolve(name, dotEnv) ?: default

        private fun envOrNull(name: String, dotEnv: Map<String, String>): String? =
            resolve(name, dotEnv)

        private fun resolve(name: String, dotEnv: Map<String, String>): String? =
            System.getenv(name)?.takeIf { it.isNotBlank() }
                ?: dotEnv[name]?.takeIf { it.isNotBlank() }
    }
}
