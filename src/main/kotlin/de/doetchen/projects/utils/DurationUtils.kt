/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.utils

object DurationUtils {

    private const val PARSE_CEILING_SECONDS = 3_153_600_000L

    private val secondsPerUnit = mapOf('s' to 1L, 'm' to 60L, 'h' to 3_600L, 'd' to 86_400L)
    private val fullInputPattern = Regex("^(\\d{1,9}[smhd])+$")
    private val segmentPattern = Regex("(\\d{1,9})([smhd])")

    fun parseDurationSeconds(input: String): Long? {
        val text = input.trim().lowercase()
        if (!fullInputPattern.matches(text)) return null

        val total = segmentPattern.findAll(text).fold(0L) { sum, segment ->
            val segmentSeconds = segment.groupValues[1].toLong() * secondsPerUnit.getValue(segment.groupValues[2].single())
            (sum + segmentSeconds).coerceAtMost(PARSE_CEILING_SECONDS)
        }
        return total.takeIf { it > 0 }
    }

    fun formatDuration(totalSeconds: Long): String {
        val seconds = totalSeconds.coerceAtLeast(0)
        if (seconds == 0L) return "0s"

        val parts = listOf(
            seconds / 86_400 to "d",
            seconds % 86_400 / 3_600 to "h",
            seconds % 3_600 / 60 to "m",
            seconds % 60 to "s"
        )
        return parts.filter { it.first > 0 }.joinToString(" ") { "${it.first}${it.second}" }
    }
}
