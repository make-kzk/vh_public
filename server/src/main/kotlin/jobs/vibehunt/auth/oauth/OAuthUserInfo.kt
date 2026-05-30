package jobs.vibehunt.auth.oauth

data class OAuthUserInfo(
    val subject: String,
    val email: String,
    val displayName: String?,
)
