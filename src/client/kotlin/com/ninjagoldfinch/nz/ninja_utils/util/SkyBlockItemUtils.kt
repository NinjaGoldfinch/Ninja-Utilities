package com.ninjagoldfinch.nz.ninja_utils.util

import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

/**
 * Utility for extracting Hypixel SkyBlock item data from ItemStacks.
 * Hypixel stores custom data in the CUSTOM_DATA component with fields
 * like id, modifier, enchantments, uuid, etc. at the top level.
 */
object SkyBlockItemUtils {

    /**
     * Extracts the Hypixel SkyBlock item ID from an ItemStack.
     * Returns null if the item has no SkyBlock ID.
     */
    fun getItemId(stack: ItemStack): String? {
        val nbt = getCustomData(stack) ?: return null
        val id = nbt.getString("id").orElse(null)
        return if (id.isNullOrBlank()) null else id
    }

    /**
     * Returns the full custom data NbtCompound for inspection.
     */
    fun getCustomData(stack: ItemStack): NbtCompound? {
        if (stack.isEmpty) return null
        val customData = stack.get(DataComponentTypes.CUSTOM_DATA) ?: return null
        val nbt = customData.copyNbt()
        return if (nbt.isEmpty) null else nbt
    }
}
