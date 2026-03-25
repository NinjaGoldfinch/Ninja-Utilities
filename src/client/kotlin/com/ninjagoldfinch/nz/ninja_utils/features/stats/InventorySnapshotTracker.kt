package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.IslandChangeEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainSource
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.SkyBlockItemUtils
import net.minecraft.client.MinecraftClient

object InventorySnapshotTracker {
    private val logger = ModLogger.category("InvSnapshot")

    private var previousSnapshot: Map<String, Int>? = null

    /** Slot range for player inventory (hotbar + main inventory), excluding armor. */
    private val INVENTORY_SLOTS = 0..35

    fun initialize() {
        EventBus.subscribe<IslandChangeEvent> {
            logger.debug("Island changed, clearing inventory snapshot baseline")
            previousSnapshot = null
        }
    }

    fun captureSnapshot(): Map<String, Int> {
        val player = MinecraftClient.getInstance().player ?: return emptyMap()
        val inventory = player.inventory
        val counts = mutableMapOf<String, Int>()

        for (i in INVENTORY_SLOTS) {
            val stack = inventory.getStack(i)
            val itemId = SkyBlockItemUtils.getItemId(stack) ?: continue
            counts[itemId] = (counts[itemId] ?: 0) + stack.count
        }

        return counts
    }

    fun tick() {
        val current = captureSnapshot()
        val previous = previousSnapshot
        previousSnapshot = current

        if (previous == null) return

        for ((itemId, newCount) in current) {
            val oldCount = previous[itemId] ?: 0
            val diff = newCount - oldCount
            if (diff > 0) {
                logger.debug("Inventory gain: +$diff $itemId")
                EventBus.post(
                    ItemGainEvent(
                        itemId = itemId,
                        displayName = itemId.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        amount = diff,
                        source = ItemGainSource.INVENTORY
                    )
                )
            }
        }
    }

    fun reset() {
        previousSnapshot = null
    }
}
