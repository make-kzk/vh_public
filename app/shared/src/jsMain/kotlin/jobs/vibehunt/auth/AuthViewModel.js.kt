package jobs.vibehunt.auth

actual fun openOAuthAuthorizationUrl(url: String) {
    jobs.vibehunt.auth.openOAuthAuthorizationUrl(url)
}

actual fun defaultAuthRedirectUri(): String = authCallbackPath()
