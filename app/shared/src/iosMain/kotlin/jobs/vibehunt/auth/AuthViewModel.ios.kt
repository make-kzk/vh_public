package jobs.vibehunt.auth

actual fun openOAuthAuthorizationUrl(url: String) {
    error("OAuth is only supported on web in MVP")
}

actual fun defaultAuthRedirectUri(): String = ""
