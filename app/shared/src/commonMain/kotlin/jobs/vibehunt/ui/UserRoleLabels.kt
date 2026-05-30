package jobs.vibehunt.ui

import jobs.vibehunt.auth.UserRole

fun UserRole.displayLabel(): String =
    when (this) {
        UserRole.SEEKER -> "Соискатель"
        UserRole.EMPLOYER -> "Работодатель"
    }
