package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainSource
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine

object ItemGainHud : HudElement("itemGains", "Item Gains") {
    private const val DISPLAY_DURATION_MS = 10_000L
    private const val MAX_DISPLAY_ITEMS = 5
    private const val MAX_BUFFER = 50

    private data class DisplayEntry(
        val displayName: String,
        val amount: Int,
        val source: ItemGainSource,
        val timestamp: Long
    )

    private val recentGains = mutableListOf<DisplayEntry>()

    override fun isEnabled(): Boolean = HudCategory.showItemGains

    override fun getData(): HudLine? = null

    override fun getLines(): List<HudLine> {
        val now = System.currentTimeMillis()
        recentGains.removeAll { now - it.timestamp > DISPLAY_DURATION_MS }

        return recentGains
            .groupBy { it.displayName }
            .map { (name, entries) ->
                val total = entries.sumOf { it.amount }
                val hasSack = entries.any { it.source == ItemGainSource.SACK }
                val suffix = if (hasSack) " [S]" else ""
                HudLine("Item", "+$total $name", valueColor = 0xFF55FF55.toInt(), suffix = suffix, suffixColor = 0xFF888888.toInt())
            }
            .take(MAX_DISPLAY_ITEMS)
    }

    override fun getSampleData(): List<HudLine> = listOf(
        HudLine("Item", "+64 Enchanted Diamond", valueColor = 0xFF55FF55.toInt()),
        HudLine("Item", "+128 Wheat", valueColor = 0xFF55FF55.toInt(), suffix = " [S]", suffixColor = 0xFF888888.toInt())
    )

    fun recordGain(event: ItemGainEvent) {
        recentGains.add(DisplayEntry(event.displayName, event.amount, event.source, System.currentTimeMillis()))
        if (recentGains.size > MAX_BUFFER) recentGains.removeFirst()
    }
}
