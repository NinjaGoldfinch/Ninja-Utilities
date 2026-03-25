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

    /** Snapshot cycles to skip after a world change before diffing. Called every 10 game ticks (~0.5s each). */
    private const val GRACE_CYCLES = 4 // 4 × 0.5s = ~2 seconds
    private var gracePeriodRemaining = 0

    /** Slot range for player inventory (hotbar + main inventory), excluding armor. */
    private val INVENTORY_SLOTS = 0..35

    fun initialize() {
        EventBus.subscribe<IslandChangeEvent> {
            logger.debug("Island changed, suppressing inventory snapshot for ${GRACE_CYCLES} ticks")
            previousSnapshot = null
            gracePeriodRemaining = GRACE_CYCLES
        }
    }

    /** Called on server join to suppress false positives from inventory refill. */
    fun onJoinServer() {
        logger.debug("Server joined, suppressing inventory snapshot for ${GRACE_CYCLES} ticks")
        previousSnapshot = null
        gracePeriodRemaining = GRACE_CYCLES
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
        if (gracePeriodRemaining > 0) {
            logger.trace("Grace period: $gracePeriodRemaining cycles remaining, skipping snapshot")
            gracePeriodRemaining--
            previousSnapshot = null
            return
        }

        val current = captureSnapshot()
        val previous = previousSnapshot
        previousSnapshot = current

        if (previous == null) {
            logger.debug("First snapshot captured: ${current.size} unique items")
            return
        }

        for ((itemId, entry) in current) {
            val oldCount = previous[itemId]?.count ?: 0
            val diff = entry.count - oldCount
            if (diff > 0) {
                logger.debug("Inventory gain: +$diff ${entry.displayName} ($itemId) [was=$oldCount, now=${entry.count}]")
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
        gracePeriodRemaining = 0
    }
}
