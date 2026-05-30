package jobs.vibehunt.auth

import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

actual fun currentAuthPath(): String = window.location.pathname

actual fun readAuthQueryParam(name: String): String? {
    val params = URLSearchParams(window.location.search)
    return params.get(name)
}
