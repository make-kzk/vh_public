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

    companion object {
        fun fromEnvironment(): AppConfig =
            AppConfig(
                databaseUrl = env("DATABASE_URL", "jdbc:postgresql://localhost:5432/vibehunt"),
                databaseUser = env("DATABASE_USER", "vibehunt"),
                databasePassword = env("DATABASE_PASSWORD", "vibehunt"),
                webOrigin = env("WEB_ORIGIN", "http://localhost:8081"),
                frontendUrl = env("FRONTEND_URL", "http://localhost:8081"),
                sessionCookieName = env("SESSION_COOKIE_NAME", "vibehunt_session"),
                sessionDays = env("SESSION_DAYS", "30").toLong(),
                googleClientId = envOrNull("GOOGLE_CLIENT_ID"),
                googleClientSecret = envOrNull("GOOGLE_CLIENT_SECRET"),
                googleRedirectUri = envOrNull("GOOGLE_REDIRECT_URI"),
                appleClientId = envOrNull("APPLE_CLIENT_ID"),
                appleTeamId = envOrNull("APPLE_TEAM_ID"),
                appleKeyId = envOrNull("APPLE_KEY_ID"),
                applePrivateKey = envOrNull("APPLE_PRIVATE_KEY")?.replace("\\n", "\n"),
                appleRedirectUri = envOrNull("APPLE_REDIRECT_URI"),
            )

        private fun env(name: String, default: String): String =
            System.getenv(name)?.takeIf { it.isNotBlank() } ?: default

        private fun envOrNull(name: String): String? =
            System.getenv(name)?.takeIf { it.isNotBlank() }
    }
}
