package com.ninjagoldfinch.nz.ninja_utils.util

object TextUtils {
    // Matches Minecraft/Hypixel formatting codes: § followed by any single character
    private val FORMATTING_CODE_REGEX = Regex("\u00a7.")

    // Invisible Unicode characters Hypixel uses to make scoreboard lines unique
    private val INVISIBLE_CHARS_REGEX = Regex("[\u200B\u200C\u200D\u200E\u200F\u00AD\uFEFF\u2060\u2061\u2062\u2063\u2064\u180E]")

    fun stripFormatting(text: String): String = FORMATTING_CODE_REGEX.replace(text, "")

    fun stripFormattingAndInvisible(text: String): String =
        INVISIBLE_CHARS_REGEX.replace(FORMATTING_CODE_REGEX.replace(text, ""), "")

    /**
     * Formats a duration in seconds. Under 60s: "42.5s". 60s+: "1m32s".
     */
    fun formatDuration(seconds: Double): String {
        if (seconds < 60.0) return "${"%.1f".format(seconds)}s"
        val mins = (seconds / 60).toInt()
        val secs = (seconds % 60).toInt()
        return "${mins}m${secs}s"
    }
}
