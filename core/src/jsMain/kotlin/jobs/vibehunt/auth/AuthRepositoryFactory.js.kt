package jobs.vibehunt.auth

actual fun createAuthRepository(): AuthRepository = JsAuthRepository()
