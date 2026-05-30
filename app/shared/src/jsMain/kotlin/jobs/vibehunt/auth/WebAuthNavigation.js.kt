package jobs.vibehunt.auth

import kotlinx.browser.window

actual fun currentAuthPath(): String = window.location.pathname
