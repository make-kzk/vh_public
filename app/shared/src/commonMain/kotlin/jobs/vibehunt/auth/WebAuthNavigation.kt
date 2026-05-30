package jobs.vibehunt.auth

expect fun currentAuthPath(): String

expect fun readAuthQueryParam(name: String): String?
