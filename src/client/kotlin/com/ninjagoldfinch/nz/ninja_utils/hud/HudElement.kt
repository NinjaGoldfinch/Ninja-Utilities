package com.ninjagoldfinch.nz.ninja_utils.hud

import net.minecraft.client.MinecraftClient

/**
 * A HUD element that provides labeled data for the HUD.
 * Each element is independently positioned and toggled.
 *
 * @param id         unique identifier, used as key for position storage
 * @param displayName  human-readable name shown in the config screen
 */
abstract class HudElement(val id: String, val displayName: String) {
    /** Whether this element is toggled on in config. */
    abstract fun isEnabled(): Boolean

    /**
     * Returns the current data to display, or null if there's nothing to show.
     * Returning null hides this element from the HUD.
     */
    abstract fun getData(): HudLine?

    /**
     * Returns all lines for this element. Override for multi-line elements.
     * Default implementation wraps getData() in a single-element list.
     */
    open fun getLines(): List<HudLine> = listOfNotNull(getData())

    /**
     * Returns sample data for the config screen preview when no live data is available.
     */
    open fun getSampleData(): List<HudLine> = listOf(HudLine(displayName, "---"))

    protected val textRenderer get() = MinecraftClient.getInstance().textRenderer
}

/**
 * A single line in the HUD.
 * @param label  e.g. "Location", "Purse"
 * @param value  the data text
 * @param labelColor  ARGB color for the label
 * @param valueColor  ARGB color for the value
 */
data class HudLine(
    val label: String,
    val value: String,
    val labelColor: Int = 0xFFAAAAAA.toInt(),
    val valueColor: Int = 0xFFFFFFFF.toInt()
)
