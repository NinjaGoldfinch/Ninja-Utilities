package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "skyblock")
class SkyblockCategory {
    companion object {
        @ConfigOption.Separator(value = "ninja_utils.config.skyblock.scoreboard")

        @ConfigEntry(id = "cleanScoreboard", translation = "ninja_utils.config.skyblock.cleanScoreboard")
        @Comment(value = "Remove unnecessary lines from scoreboard")
        @JvmField var cleanScoreboard: Boolean = false

        @ConfigEntry(id = "hideScoreboardNumbers", translation = "ninja_utils.config.skyblock.hideScoreboardNumbers")
        @Comment(value = "Hide the red score numbers on the sidebar")
        @JvmField var hideScoreboardNumbers: Boolean = false

        @ConfigOption.Separator(value = "ninja_utils.config.skyblock.chat")

        @ConfigEntry(id = "trackSkillXp", translation = "ninja_utils.config.skyblock.trackSkillXp")
        @Comment(value = "Track skill XP gains from chat messages")
        @JvmField var trackSkillXp: Boolean = true

        @ConfigEntry(id = "trackRareDrops", translation = "ninja_utils.config.skyblock.trackRareDrops")
        @Comment(value = "Log rare drops to debug log")
        @JvmField var trackRareDrops: Boolean = false

        @ConfigEntry(id = "trackCoinChanges", translation = "ninja_utils.config.skyblock.trackCoinChanges")
        @Comment(value = "Track coin gain/loss from chat messages")
        @JvmField var trackCoinChanges: Boolean = false

        @ConfigOption.Separator(value = "ninja_utils.config.skyblock.slayer")

        @ConfigEntry(id = "slayerTracker", translation = "ninja_utils.config.skyblock.slayerTracker")
        @Comment(value = "Track slayer quest progress")
        @JvmField var slayerTracker: Boolean = false

        @ConfigEntry(id = "slayerBossAlert", translation = "ninja_utils.config.skyblock.slayerBossAlert")
        @Comment(value = "Alert when slayer boss spawns")
        @JvmField var slayerBossAlert: Boolean = false

        @ConfigOption.Separator(value = "ninja_utils.config.skyblock.tablist")

        @ConfigEntry(id = "parseTabWidgets", translation = "ninja_utils.config.skyblock.parseTabWidgets")
        @Comment(value = "Parse tab list widget data for HUD use")
        @JvmField var parseTabWidgets: Boolean = true
    }
}
