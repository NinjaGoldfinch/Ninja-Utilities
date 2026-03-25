package com.ninjagoldfinch.nz.ninja_utils.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack

data class SearchResult(
    val slotIndex: Int,
    val stack: ItemStack,
    val itemId: String,
    val location: String
)

object InventorySearcher {

    /**
     * Searches the player's inventory (main + armor + offhand) for items
     * matching the given Hypixel item ID. Case-insensitive.
     */
    fun searchPlayerInventory(targetId: String): List<SearchResult> {
        val player = MinecraftClient.getInstance().player ?: return emptyList()
        val inventory = player.inventory
        val results = mutableListOf<SearchResult>()
        val upper = targetId.uppercase()

        for (i in 0 until inventory.size()) {
            val stack = inventory.getStack(i)
            val itemId = SkyBlockItemUtils.getItemId(stack) ?: continue
            if (itemId.uppercase() == upper) {
                val location = when {
                    i < 9 -> "Hotbar slot $i"
                    i < 36 -> "Inventory slot $i"
                    i < 40 -> "Armor slot ${i - 36}"
                    else -> "Offhand"
                }
                results.add(SearchResult(i, stack, itemId, location))
            }
        }

        return results
    }

    /**
     * Searches the currently open container for items matching the given ID.
     * Returns null if no container is open. Only searches container slots,
     * not the player inventory portion of the screen handler.
     */
    fun searchOpenContainer(targetId: String): List<SearchResult>? {
        val screen = MinecraftClient.getInstance().currentScreen
        if (screen !is GenericContainerScreen) return null

        val handler = screen.screenHandler
        val containerSize = handler.rows * 9
        val title = TextUtils.stripFormattingAndInvisible(screen.title.string)
        val results = mutableListOf<SearchResult>()
        val upper = targetId.uppercase()

        for (i in 0 until containerSize) {
            val stack = handler.inventory.getStack(i)
            val itemId = SkyBlockItemUtils.getItemId(stack) ?: continue
            if (itemId.uppercase() == upper) {
                results.add(SearchResult(i, stack, itemId, "$title slot $i"))
            }
        }

        return results
    }

    /**
     * Searches both player inventory and any open container.
     */
    fun searchAll(targetId: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        results.addAll(searchPlayerInventory(targetId))
        searchOpenContainer(targetId)?.let { results.addAll(it) }
        return results
    }
}
