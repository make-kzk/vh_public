package jobs.vibehunt.auth

import kotlinx.browser.window

fun openOAuthAuthorizationUrl(url: String) {
    window.location.href = url
}

fun currentOrigin(): String = window.location.origin

fun authCallbackPath(): String = "${currentOrigin()}/auth/callback"
