package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "skyblock")
class SkyblockCategory {
    companion object {
        @ConfigOption.Separator(
            value = "Scoreboard",
            description = "Modify the SkyBlock sidebar scoreboard"
        )

        @ConfigEntry(id = "cleanScoreboard", translation = "Clean Scoreboard")
        @Comment("Remove unnecessary lines from the scoreboard")
        @JvmField var cleanScoreboard: Boolean = false

        @ConfigEntry(id = "hideScoreboardNumbers", translation = "Hide Score Numbers")
        @Comment("Hide the red score numbers on the sidebar")
        @JvmField var hideScoreboardNumbers: Boolean = false

        @ConfigOption.Separator(
            value = "Chat",
            description = "Track information from chat messages"
        )

        @ConfigEntry(id = "trackSkillXp", translation = "Track Skill XP")
        @Comment("Track skill XP gains from chat messages")
        @JvmField var trackSkillXp: Boolean = true

        @ConfigEntry(id = "trackRareDrops", translation = "Track Rare Drops")
        @Comment("Log rare drops to the debug log")
        @JvmField var trackRareDrops: Boolean = false

        @ConfigEntry(id = "trackCoinChanges", translation = "Track Coin Changes")
        @Comment("Track coin gain/loss from chat messages")
        @JvmField var trackCoinChanges: Boolean = false

        @ConfigOption.Separator(
            value = "Slayer",
            description = "Slayer quest tracking and alerts"
        )

        @ConfigEntry(id = "slayerTracker", translation = "Slayer Tracker")
        @Comment("Track slayer quest progress and kill times")
        @JvmField var slayerTracker: Boolean = false

        @ConfigEntry(id = "slayerBossAlert", translation = "Slayer Boss Alert")
        @Comment("Alert when your slayer boss spawns")
        @JvmField var slayerBossAlert: Boolean = false

        @ConfigOption.Separator(
            value = "Tab List",
            description = "Data parsing from the player tab list"
        )

        @ConfigEntry(id = "parseTabWidgets", translation = "Parse Tab Widgets")
        @Comment("Parse tab list widget data for HUD elements")
        @JvmField var parseTabWidgets: Boolean = true
    }
}
