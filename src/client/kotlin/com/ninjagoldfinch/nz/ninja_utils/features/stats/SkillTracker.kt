package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger

object SkillTracker {
    private val logger = ModLogger.category("SkillTracker")

    private val xpHistory = mutableMapOf<String, MutableList<XpEntry>>()
    private const val RATE_WINDOW_MS = 60_000L

    data class XpEntry(val xpGain: Double, val timestamp: Long)

    data class SkillRate(
        val skill: String,
        val xpPerMinute: Double,
        val totalGained: Double,
        val entryCount: Int
    )

    fun recordXpGain(skill: String, xpGain: Double) {
        val entries = xpHistory.getOrPut(skill) { mutableListOf() }
        entries.add(XpEntry(xpGain, System.currentTimeMillis()))
        pruneOldEntries(entries)
        logger.trace("Recorded +$xpGain $skill XP (${entries.size} entries in window)")
    }

    fun getRate(skill: String): SkillRate? {
        val entries = xpHistory[skill] ?: return null
        pruneOldEntries(entries)
        if (entries.isEmpty()) return null

        val totalGained = entries.sumOf { it.xpGain }
        val windowMs = if (entries.size >= 2) {
            entries.last().timestamp - entries.first().timestamp
        } else {
            RATE_WINDOW_MS
        }
        val xpPerMinute = if (windowMs > 0) {
            totalGained / windowMs * 60_000.0
        } else {
            totalGained
        }

        return SkillRate(skill, xpPerMinute, totalGained, entries.size)
    }

    fun getAllRates(): Map<String, SkillRate> {
        return xpHistory.keys.mapNotNull { skill ->
            getRate(skill)?.let { skill to it }
        }.toMap()
    }

    fun reset() {
        xpHistory.clear()
    }

    private fun pruneOldEntries(entries: MutableList<XpEntry>) {
        val cutoff = System.currentTimeMillis() - RATE_WINDOW_MS
        entries.removeAll { it.timestamp < cutoff }
    }
}
