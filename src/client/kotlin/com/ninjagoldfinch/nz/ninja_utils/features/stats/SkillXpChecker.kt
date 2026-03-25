package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger

/**
 * Reads skill XP values from the "Your Skills" menu and compares
 * them against SkillTracker's recorded gains.
 *
 * Flow:
 * 1. User opens /skills → ScreenInterceptor detects "Your Skills" title
 * 2. After items load, ScreenInterceptor calls [onSkillMenuOpened] with item lore
 * 3. This class parses the lore and stores baseline + snapshot data
 * 4. Dev command displays comparison between tracked gains and actual menu values
 */
object SkillXpChecker {
    private val logger = ModLogger.category("SkillXpChecker")

    /** Regex to match "Level XX" from item name, e.g. "Mining XX" or "Mining L" */
    private val SKILL_NAME_PATTERN = Regex("""^(\w[\w ]*?) \w+$""")

    /** Regex to match XP progress in lore: "1,234,567 / 2,000,000" or "1,234,567/2,000,000" */
    private val XP_PROGRESS_PATTERN = Regex("""([\d,]+)\s*/\s*([\d,]+)""")

    /** Regex to match percentage in lore: "Progress: 61.7%" */
    private val PERCENT_PATTERN = Regex("""(\d+\.?\d*)%""")

    data class SkillSnapshot(
        val skill: String,
        val currentXp: Long,
        val requiredXp: Long,
        val percent: Double,
        val timestamp: Long
    )

    /** Last snapshot from the Skills menu, keyed by skill name (lowercase). */
    private val snapshots = mutableMapOf<String, SkillSnapshot>()

    /** Timestamp of the last menu read. */
    var lastMenuReadTime: Long = 0
        private set

    /**
     * Called by ScreenInterceptor when the "Your Skills" menu items are available.
     * Each entry is a pair of (item display name, list of lore lines), all with formatting stripped.
     */
    fun onSkillMenuOpened(items: List<Pair<String, List<String>>>) {
        val now = System.currentTimeMillis()
        var parsed = 0

        for ((name, lore) in items) {
            val skillName = extractSkillName(name) ?: continue
            val (currentXp, requiredXp) = extractXpProgress(lore) ?: continue
            val percent = extractPercent(lore) ?: (if (requiredXp > 0) currentXp.toDouble() / requiredXp * 100 else 0.0)

            snapshots[skillName.lowercase()] = SkillSnapshot(skillName, currentXp, requiredXp, percent, now)
            parsed++
        }

        lastMenuReadTime = now
        logger.info("Parsed $parsed skills from menu")
    }

    /** Returns all stored snapshots. */
    fun getSnapshots(): Map<String, SkillSnapshot> = snapshots.toMap()

    /**
     * Compares SkillTracker's recorded gains against the menu snapshots.
     * Returns a list of comparison results for each tracked skill.
     */
    fun compare(): List<SkillComparison> {
        val results = mutableListOf<SkillComparison>()
        val allRates = SkillTracker.getAllRates()

        for ((key, snapshot) in snapshots) {
            val rate = allRates[snapshot.skill]
            results.add(
                SkillComparison(
                    skill = snapshot.skill,
                    menuXp = snapshot.currentXp,
                    menuRequired = snapshot.requiredXp,
                    menuPercent = snapshot.percent,
                    trackedGain = rate?.totalGained ?: 0.0,
                    trackedRate = rate?.xpPerMinute ?: 0.0,
                    snapshotAge = System.currentTimeMillis() - snapshot.timestamp
                )
            )
        }

        // Also include skills tracked but not in menu
        for ((skill, rate) in allRates) {
            if (snapshots.containsKey(skill.lowercase())) continue
            results.add(
                SkillComparison(
                    skill = skill,
                    menuXp = null,
                    menuRequired = null,
                    menuPercent = null,
                    trackedGain = rate.totalGained,
                    trackedRate = rate.xpPerMinute,
                    snapshotAge = null
                )
            )
        }

        return results
    }

    fun reset() {
        snapshots.clear()
        lastMenuReadTime = 0
    }

    private fun extractSkillName(itemName: String): String? {
        // SkyBlock skill items are named like "Mining", "Farming", "Combat", etc.
        // Some may have level info appended. Try exact single-word match first.
        val cleaned = itemName.trim()
        if (cleaned.isEmpty()) return null

        // If it's a single word, it's the skill name
        if (!cleaned.contains(' ')) return cleaned

        // Otherwise try to extract the first word(s) before a level indicator
        return SKILL_NAME_PATTERN.find(cleaned)?.groupValues?.get(1)
    }

    private fun extractXpProgress(lore: List<String>): Pair<Long, Long>? {
        for (line in lore) {
            val match = XP_PROGRESS_PATTERN.find(line) ?: continue
            val current = match.groupValues[1].replace(",", "").toLongOrNull() ?: continue
            val required = match.groupValues[2].replace(",", "").toLongOrNull() ?: continue
            return current to required
        }
        return null
    }

    private fun extractPercent(lore: List<String>): Double? {
        for (line in lore) {
            if (!line.contains("Progress", ignoreCase = true) && !line.contains("%")) continue
            val match = PERCENT_PATTERN.find(line) ?: continue
            return match.groupValues[1].toDoubleOrNull()
        }
        return null
    }

    data class SkillComparison(
        val skill: String,
        val menuXp: Long?,
        val menuRequired: Long?,
        val menuPercent: Double?,
        val trackedGain: Double,
        val trackedRate: Double,
        val snapshotAge: Long?
    )
}
