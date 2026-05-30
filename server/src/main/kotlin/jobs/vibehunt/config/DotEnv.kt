package jobs.vibehunt.config

import java.nio.file.Files
import java.nio.file.Path

internal object DotEnv {
    fun load(envFile: Path = Path.of(System.getProperty("user.dir"), ".env")): Map<String, String> {
        if (!Files.exists(envFile)) return emptyMap()
        return Files.readAllLines(envFile).mapNotNull { parseLine(it) }.toMap()
    }

    private fun parseLine(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return null
        val separator = trimmed.indexOf('=')
        if (separator <= 0) return null
        val key = trimmed.substring(0, separator).trim()
        if (key.isEmpty()) return null
        var value = trimmed.substring(separator + 1).trim()
        if (
            value.length >= 2 &&
            ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'")))
        ) {
            value = value.substring(1, value.length - 1)
        }
        return key to value
    }
}
