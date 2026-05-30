package jobs.vibehunt.auth

actual fun openOAuthAuthorizationUrl(url: String) {
    navigateToOAuthAuthorizationUrl(url)
}

actual fun defaultAuthRedirectUri(): String = authCallbackPath()
