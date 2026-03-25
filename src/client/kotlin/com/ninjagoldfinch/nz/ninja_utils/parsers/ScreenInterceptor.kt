package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillXpChecker
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType

/**
 * Intercepts container screens (chests) to read SkyBlock menu data.
 * Currently supports:
 * - "Your Skills" menu: reads skill XP values for SkillXpChecker
 */
object ScreenInterceptor {
    private val logger = ModLogger.category("ScreenInterceptor")

    /** Number of ticks to wait for items to load after screen opens. */
    private const val ITEM_LOAD_DELAY_TICKS = 3

    fun initialize() {
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            if (screen is GenericContainerScreen) {
                onContainerScreenInit(screen)
            }
        }
    }

    private fun onContainerScreenInit(screen: GenericContainerScreen) {
        val title = TextUtils.stripFormattingAndInvisible(screen.title.string)
        logger.trace("Container opened: \"$title\"")

        when {
            title.equals("Your Skills", ignoreCase = true) -> {
                waitForItemsAndRead(screen) { readSkillsMenu(it) }
            }
        }
    }

    /**
     * Waits a few ticks for the server to send items, then reads the container.
     */
    private fun waitForItemsAndRead(screen: GenericContainerScreen, reader: (GenericContainerScreen) -> Unit) {
        var ticksWaited = 0
        ScreenEvents.afterTick(screen).register {
            ticksWaited++
            if (ticksWaited >= ITEM_LOAD_DELAY_TICKS) {
                reader(screen)
                // Unregister by doing nothing further — tick listener is per-screen
                ticksWaited = Int.MAX_VALUE
            }
        }
    }

    private fun readSkillsMenu(screen: GenericContainerScreen) {
        val handler = screen.screenHandler
        val items = mutableListOf<Pair<String, List<String>>>()

        for (slot in handler.slots) {
            // Only read the container slots, not the player inventory
            if (slot.inventory == handler.inventory) {
                val stack = slot.stack
                if (stack.isEmpty || stack.item == Items.AIR) continue

                val name = TextUtils.stripFormattingAndInvisible(stack.name.string)
                val player = MinecraftClient.getInstance().player
                val lore = stack.getTooltip(Item.TooltipContext.DEFAULT, player, TooltipType.BASIC)
                    .drop(1) // First line is the item name
                    .map { TextUtils.stripFormattingAndInvisible(it.string) }
                    .filter { it.isNotBlank() }

                items.add(name to lore)
            }
        }

        logger.info("Read ${items.size} items from Skills menu")
        SkillXpChecker.onSkillMenuOpened(items)
    }
}
