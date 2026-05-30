package jobs.vibehunt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform