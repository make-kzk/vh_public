package jobs.vibehunt.auth.oauth

import com.auth0.jwk.JwkProviderBuilder
import java.net.URL
import java.util.concurrent.TimeUnit

object JwtProviders {
    fun applePublicKeyProvider() =
        JwkProviderBuilder(URL("https://appleid.apple.com/auth/keys"))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
}
