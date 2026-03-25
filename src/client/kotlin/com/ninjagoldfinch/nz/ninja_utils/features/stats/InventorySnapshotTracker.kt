package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.IslandChangeEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainSource
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.SkyBlockItemUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries

object InventorySnapshotTracker {
    private val logger = ModLogger.category("InvSnapshot")

    private var previousSnapshot: Map<String, SnapshotEntry>? = null

    /** Slot range for player inventory (hotbar + main inventory), excluding armor. */
    private val INVENTORY_SLOTS = 0..35

    fun initialize() {
        EventBus.subscribe<IslandChangeEvent> {
            logger.debug("Island changed, clearing inventory snapshot baseline")
            previousSnapshot = null
        }
    }

    /**
     * Returns an item identifier for the stack.
     * Prefers the Hypixel SkyBlock item ID, falls back to the Minecraft registry ID.
     */
    private fun getItemKey(stack: ItemStack): String? {
        if (stack.isEmpty) return null
        return SkyBlockItemUtils.getItemId(stack)
            ?: Registries.ITEM.getId(stack.item).toString()
    }

    /**
     * Returns a human-readable display name for an item key.
     */
    private fun displayNameFor(key: String, stack: ItemStack): String {
        // If it's a SkyBlock ID (no colon), format it nicely
        if (!key.contains(":")) {
            return key.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
        // For Minecraft registry IDs, use the item's actual name
        return stack.name.string
    }

    fun captureSnapshot(): Map<String, SnapshotEntry> {
        val player = MinecraftClient.getInstance().player ?: return emptyMap()
        val inventory = player.inventory
        val counts = mutableMapOf<String, SnapshotEntry>()

        for (i in INVENTORY_SLOTS) {
            val stack = inventory.getStack(i)
            val key = getItemKey(stack) ?: continue
            val existing = counts[key]
            if (existing != null) {
                counts[key] = existing.copy(count = existing.count + stack.count)
            } else {
                counts[key] = SnapshotEntry(stack.count, displayNameFor(key, stack))
            }
        }

        return counts
    }

    data class SnapshotEntry(val count: Int, val displayName: String)

    fun tick() {
        val current = captureSnapshot()
        val previous = previousSnapshot
        previousSnapshot = current

        if (previous == null) return

        for ((itemId, entry) in current) {
            val oldCount = previous[itemId]?.count ?: 0
            val diff = entry.count - oldCount
            if (diff > 0) {
                logger.debug("Inventory gain: +$diff $itemId")
                EventBus.post(
                    ItemGainEvent(
                        itemId = itemId,
                        displayName = entry.displayName,
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
