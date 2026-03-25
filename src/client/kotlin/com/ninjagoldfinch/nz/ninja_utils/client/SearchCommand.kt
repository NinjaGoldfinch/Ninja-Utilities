package com.ninjagoldfinch.nz.ninja_utils.client

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.ninjagoldfinch.nz.ninja_utils.util.InventorySearcher
import com.ninjagoldfinch.nz.ninja_utils.util.SkyBlockItemUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object SearchCommand {

    fun buildSearchSubtree(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("search").apply {
            // /ninja_utils search — show held item's SkyBlock ID
            executes { ctx ->
                showHeldItemId(ctx.source)
                1
            }

            // /ninja_utils search <item_id> — search inventories for item
            then(argument("item_id", StringArgumentType.greedyString())
                .executes { ctx ->
                    val itemId = StringArgumentType.getString(ctx, "item_id")
                    searchForItem(ctx.source, itemId)
                    1
                }
            )
        }
    }

    private fun showHeldItemId(source: FabricClientCommandSource) {
        val player = MinecraftClient.getInstance().player
        if (player == null) {
            msg(source, "Not in-game", Formatting.RED)
            return
        }

        val mainHand = player.mainHandStack
        val itemId = SkyBlockItemUtils.getItemId(mainHand)
        if (itemId != null) {
            DevCommand.header(source, "Held Item")
            msg(source, "Item ID: $itemId", Formatting.GREEN)
            msg(source, "Display: ${mainHand.name.string}", Formatting.GRAY)

            val ea = SkyBlockItemUtils.getCustomData(mainHand)
            if (ea != null) {
                msg(source, "Attributes: ${ea.keys.joinToString(", ")}", Formatting.DARK_GRAY)
            }
        } else {
            msg(source, "Held item has no SkyBlock ID", Formatting.YELLOW)
        }
    }

    private fun searchForItem(source: FabricClientCommandSource, targetId: String) {
        DevCommand.header(source, "Search: $targetId")

        val inventoryResults = InventorySearcher.searchPlayerInventory(targetId)
        val containerResults = InventorySearcher.searchOpenContainer(targetId)

        if (inventoryResults.isEmpty() && (containerResults == null || containerResults.isEmpty())) {
            msg(source, "No items found with ID: $targetId", Formatting.RED)
            if (containerResults == null) {
                msg(source, "Open a container to search it too", Formatting.GRAY)
            }
            return
        }

        if (inventoryResults.isNotEmpty()) {
            msg(source, "Inventory (${inventoryResults.size}):", Formatting.AQUA)
            for (r in inventoryResults) {
                msg(source, "  ${r.location}: ${r.stack.name.string} x${r.stack.count}", Formatting.GREEN)
            }
        }

        if (containerResults != null && containerResults.isNotEmpty()) {
            msg(source, "Container (${containerResults.size}):", Formatting.AQUA)
            for (r in containerResults) {
                msg(source, "  ${r.location}: ${r.stack.name.string} x${r.stack.count}", Formatting.GREEN)
            }
        }
    }

    private fun msg(source: FabricClientCommandSource, text: String, formatting: Formatting? = null) {
        val t = Text.literal("  $text")
        if (formatting != null) t.formatted(formatting)
        source.sendFeedback(t)
    }
}
