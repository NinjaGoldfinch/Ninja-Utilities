package com.ninjagoldfinch.nz.ninja_utils.util

import com.ninjagoldfinch.nz.ninja_utils.config.GeneralCategory
import com.ninjagoldfinch.nz.ninja_utils.parsers.ChatParser
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object ChatUtils {
    /**
     * Sends a client-side message to the player with chat parsing suppressed,
     * optionally prefixed with [NinjaUtils] based on config.
     */
    fun sendModMessage(message: String) {
        val player = MinecraftClient.getInstance().player ?: return
        ChatParser.suppressParsing {
            if (GeneralCategory.chatPrefix) {
                player.sendMessage(Text.literal("\u00a78[\u00a76NinjaUtils\u00a78]\u00a7r $message"), false)
            } else {
                player.sendMessage(Text.literal(message), false)
            }
        }
    }
}
