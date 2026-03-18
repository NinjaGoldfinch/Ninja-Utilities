package com.ninjagoldfinch.nz.ninja_utils.util

object TextUtils {
    // Matches Minecraft/Hypixel formatting codes: § followed by any single character
    private val FORMATTING_CODE_REGEX = Regex("\u00a7.")

    fun stripFormatting(text: String): String = FORMATTING_CODE_REGEX.replace(text, "")
}
