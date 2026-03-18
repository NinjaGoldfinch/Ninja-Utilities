package com.ninjagoldfinch.nz.ninja_utils.config

import com.ninjagoldfinch.nz.ninja_utils.logging.LogLevel
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "debug")
class DebugCategory {
    companion object {
        @ConfigOption.Separator(
            value = "Logging",
            description = "Control what gets logged for debugging"
        )

        @ConfigEntry(id = "logLevel", translation = "Log Level")
        @Comment("Logging verbosity (INFO for normal use, DEBUG for development)")
        @JvmField var logLevel: LogLevel = LogLevel.INFO

        @ConfigEntry(id = "logScoreboard", translation = "Log Scoreboard")
        @Comment("Log raw scoreboard data every update tick")
        @JvmField var logScoreboard: Boolean = false

        @ConfigEntry(id = "logTabList", translation = "Log Tab List")
        @Comment("Log raw tab list data every update tick")
        @JvmField var logTabList: Boolean = false

        @ConfigEntry(id = "logChatMessages", translation = "Log Chat Messages")
        @Comment("Log all incoming chat messages")
        @JvmField var logChatMessages: Boolean = false

        @ConfigEntry(id = "logModApiPackets", translation = "Log Mod API Packets")
        @Comment("Log all Hypixel Mod API packets")
        @JvmField var logModApiPackets: Boolean = false

        @ConfigEntry(id = "logApiResponses", translation = "Log API Responses")
        @Comment("Log backend API responses")
        @JvmField var logApiResponses: Boolean = false

        @ConfigEntry(id = "debugOverlay", translation = "Debug Overlay")
        @Comment("Show debug info overlay in-game")
        @JvmField var debugOverlay: Boolean = false
    }
}
