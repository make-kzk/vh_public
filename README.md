This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM), Server.

* [/app/iosApp](./app/iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/app/shared](./app/shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./app/shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./app/shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./app/shared/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/core](./core/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./core/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

### Authentication (web MVP)

VibeHunt uses **OAuth 2.0 with PKCE** (Google and Apple), **JWT verification** for Apple ID tokens, and **httpOnly session cookies**. The web client talks to `/api/auth/*` with cookies (webpack dev server proxies `/api` to Ktor on port 8080).

User role (`SEEKER` or `EMPLOYER`) is chosen once via `POST /api/auth/complete-registration` and cannot be changed afterward.

1. Copy [`env.example`](./env.example) to `.env` and fill in OAuth credentials.
2. Start PostgreSQL: `docker compose up -d`
3. Start the API: `./gradlew :server:run` (migrations run automatically via Flyway)
4. Start the web app: `./gradlew :app:webApp:jsBrowserDevelopmentRun` → http://localhost:8081

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/oauth/start` | Start Google/Apple OAuth (PKCE) |
| `GET /api/auth/oauth/callback/google` | Google redirect handler |
| `POST /api/auth/oauth/callback/apple` | Apple form_post callback |
| `GET /api/auth/me` | Current user (cookie session) |
| `POST /api/auth/logout` | Clear session |
| `POST /api/auth/complete-registration` | Set immutable role |

**Google / Apple blockers:** OAuth does not work until client IDs, secrets, and redirect URIs in `.env` match your cloud console configuration. Apple also requires a Services ID, Sign in with Apple key (`.p8`), and the redirect URI registered for `form_post` to the server callback URL.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :app:androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
  - Standard run: `./gradlew :app:desktopApp:run`
- Server: `./gradlew :server:run`
- Web app (auth MVP, JS only): `./gradlew :app:webApp:jsBrowserDevelopmentRun` → http://localhost:8081
- iOS app: open the [/app/iosApp](./app/iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :app:shared:testAndroidHostTest`
- Desktop tests: `./gradlew :app:shared:jvmTest`
- Server tests: `./gradlew :server:test`
- Web tests:
  - Wasm target: `./gradlew :app:shared:wasmJsTest`
  - JS target: `./gradlew :app:shared:jsTest`
- iOS tests: `./gradlew :app:shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).