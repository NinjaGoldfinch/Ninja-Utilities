package com.ninjagoldfinch.nz.ninja_utils.config

import com.ninjagoldfinch.nz.ninja_utils.logging.LogLevel
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "debug")
class DebugCategory {
    companion object {
        @ConfigOption.Separator(value = "ninja_utils.config.debug.logging")

        @ConfigEntry(id = "logLevel", translation = "ninja_utils.config.debug.logLevel")
        @Comment(value = "Logging verbosity (INFO for normal use, DEBUG for development)")
        @JvmField var logLevel: LogLevel = LogLevel.INFO

        @ConfigEntry(id = "logScoreboard", translation = "ninja_utils.config.debug.logScoreboard")
        @Comment(value = "Log raw scoreboard data every update")
        @JvmField var logScoreboard: Boolean = false

        @ConfigEntry(id = "logTabList", translation = "ninja_utils.config.debug.logTabList")
        @Comment(value = "Log raw tab list data every update")
        @JvmField var logTabList: Boolean = false

        @ConfigEntry(id = "logChatMessages", translation = "ninja_utils.config.debug.logChatMessages")
        @Comment(value = "Log all incoming chat messages")
        @JvmField var logChatMessages: Boolean = false

        @ConfigEntry(id = "logModApiPackets", translation = "ninja_utils.config.debug.logModApiPackets")
        @Comment(value = "Log all Hypixel Mod API packets")
        @JvmField var logModApiPackets: Boolean = false

        @ConfigEntry(id = "logApiResponses", translation = "ninja_utils.config.debug.logApiResponses")
        @Comment(value = "Log backend API responses")
        @JvmField var logApiResponses: Boolean = false

        @ConfigEntry(id = "debugOverlay", translation = "ninja_utils.config.debug.debugOverlay")
        @Comment(value = "Show debug info overlay in-game")
        @JvmField var debugOverlay: Boolean = false
    }
}
