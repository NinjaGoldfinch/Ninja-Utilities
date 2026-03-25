package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainSource
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger

object ItemTracker {
    private val logger = ModLogger.category("ItemTracker")

    private val gainHistory = mutableMapOf<String, MutableList<ItemGainEntry>>()
    private const val RATE_WINDOW_MS = 60_000L

    /** Time source, overridable for testing. */
    var timeProvider: () -> Long = { System.currentTimeMillis() }

    data class ItemGainEntry(
        val itemId: String,
        val displayName: String,
        val amount: Int,
        val source: ItemGainSource,
        val timestamp: Long
    )

    data class ItemRate(
        val itemId: String,
        val displayName: String,
        val totalGained: Int,
        val perMinute: Double,
        val entryCount: Int
    )

    fun recordGain(event: ItemGainEvent) {
        val entries = gainHistory.getOrPut(event.itemId) { mutableListOf() }
        entries.add(ItemGainEntry(event.itemId, event.displayName, event.amount, event.source, timeProvider()))
        pruneOldEntries(entries)
        logger.trace("Recorded +${event.amount} ${event.displayName} from ${event.source} (${entries.size} entries in window)")
    }

    fun getRate(itemId: String): ItemRate? {
        val entries = gainHistory[itemId] ?: return null
        pruneOldEntries(entries)
        if (entries.isEmpty()) return null

        val totalGained = entries.sumOf { it.amount }
        val displayName = entries.last().displayName
        val windowMs = if (entries.size >= 2) {
            entries.last().timestamp - entries.first().timestamp
        } else {
            RATE_WINDOW_MS
        }
        val perMinute = if (windowMs > 0) {
            totalGained.toDouble() / windowMs * 60_000.0
        } else {
            totalGained.toDouble()
        }

        return ItemRate(itemId, displayName, totalGained, perMinute, entries.size)
    }

    fun getAllRates(): Map<String, ItemRate> {
        return gainHistory.keys.mapNotNull { itemId ->
            getRate(itemId)?.let { itemId to it }
        }.toMap()
    }

    fun getRecentGains(limit: Int = 10): List<ItemGainEntry> {
        return gainHistory.values
            .flatten()
            .sortedByDescending { it.timestamp }
            .take(limit)
    }

    fun reset() {
        gainHistory.clear()
    }

    private fun pruneOldEntries(entries: MutableList<ItemGainEntry>) {
        val cutoff = timeProvider() - RATE_WINDOW_MS
        entries.removeAll { it.timestamp < cutoff }
    }
}
