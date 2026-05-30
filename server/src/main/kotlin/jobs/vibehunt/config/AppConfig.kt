package jobs.vibehunt.config

data class AppConfig(
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val webOrigin: String,
    val frontendUrl: String,
    val sessionCookieName: String,
    val sessionDays: Long,
    val authDevMode: Boolean,
) {
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
                authDevMode = authDevModeEnabled(dotEnv),
            )
        }

        private fun authDevModeEnabled(dotEnv: Map<String, String>): Boolean {
            val raw =
                resolve("AUTH_DEV_MODE", dotEnv)
                    ?: resolve("OAUTH_DEV_MOCK", dotEnv)
                    ?: "true"
            return raw.equals("true", ignoreCase = true)
        }

        private fun env(name: String, default: String, dotEnv: Map<String, String>): String =
            resolve(name, dotEnv) ?: default

        private fun resolve(name: String, dotEnv: Map<String, String>): String? =
            System.getenv(name)?.takeIf { it.isNotBlank() }
                ?: dotEnv[name]?.takeIf { it.isNotBlank() }
    }
}
