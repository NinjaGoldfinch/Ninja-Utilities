package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "hud")
class HudCategory {
    companion object {
        @ConfigOption.Separator(
            value = "Basic Elements",
            description = "Common HUD elements for everyday use"
        )

        @ConfigEntry(id = "showLocation", translation = "Show Location")
        @Comment("Display your current area on the HUD")
        @JvmField var showLocation: Boolean = true

        @ConfigEntry(id = "showPurse", translation = "Show Purse")
        @Comment("Display your coin balance on the HUD")
        @JvmField var showPurse: Boolean = true

        @ConfigOption.Separator(
            value = "Advanced Elements",
            description = "Additional HUD elements for detailed information"
        )

        @ConfigEntry(id = "showBits", translation = "Show Bits")
        @Comment("Display your Bits balance on the HUD")
        @JvmField var showBits: Boolean = false

        @ConfigEntry(id = "showSkillProgress", translation = "Show Skill Progress")
        @Comment("Display active skill and XP progress from the tab list")
        @JvmField var showSkillProgress: Boolean = false

        @ConfigEntry(id = "showSlayer", translation = "Show Slayer")
        @Comment("Display active slayer quest info on the HUD")
        @JvmField var showSlayer: Boolean = false

        @ConfigEntry(id = "showStats", translation = "Show Stats")
        @Comment("Display player stats from the tab list")
        @JvmField var showStats: Boolean = false

        @ConfigEntry(id = "showMayor", translation = "Show Mayor")
        @Comment("Display the current SkyBlock mayor on the HUD")
        @JvmField var showMayor: Boolean = false

        @ConfigEntry(id = "showPet", translation = "Show Pet")
        @Comment("Display your active pet on the HUD")
        @JvmField var showPet: Boolean = false

        @ConfigEntry(id = "showCookieTimer", translation = "Show Cookie Timer")
        @Comment("Display remaining Booster Cookie time on the HUD")
        @JvmField var showCookieTimer: Boolean = false

        @ConfigEntry(id = "showSbTime", translation = "Show SkyBlock Time")
        @Comment("Display the current in-game SkyBlock date")
        @JvmField var showSbTime: Boolean = false

        @ConfigEntry(id = "showPing", translation = "Show Ping")
        @Comment("Display your latency to the server")
        @JvmField var showPing: Boolean = false

        @ConfigEntry(id = "showTps", translation = "Show TPS")
        @Comment("Display the server's ticks per second")
        @JvmField var showTps: Boolean = false

        @ConfigEntry(id = "showDebugOverlay", translation = "Show Debug Overlay")
        @Comment("Display a detailed debug info overlay")
        @JvmField var showDebugOverlay: Boolean = false

        @ConfigOption.Separator(
            value = "Garden",
            description = "HUD elements specific to the Garden island"
        )

        @ConfigEntry(id = "showCopper", translation = "Show Copper")
        @Comment("Display Copper count (Garden only)")
        @JvmField var showCopper: Boolean = false

        @ConfigEntry(id = "showSowdust", translation = "Show Sowdust")
        @Comment("Display Sowdust count (Garden only)")
        @JvmField var showSowdust: Boolean = false

        @ConfigOption.Separator(
            value = "Appearance",
            description = "Customize the look of the HUD"
        )

        @ConfigEntry(id = "hudScale", translation = "HUD Scale")
        @Comment("Scale factor for all HUD elements")
        @ConfigOption.Range(min = 0.5, max = 3.0)
        @ConfigOption.Slider
        @JvmField var hudScale: Float = 1.0f

        @ConfigEntry(id = "hudBackgroundOpacity", translation = "Background Opacity")
        @Comment("Opacity of the HUD background (0 = transparent, 1 = opaque)")
        @ConfigOption.Range(min = 0.0, max = 1.0)
        @ConfigOption.Slider
        @JvmField var hudBackgroundOpacity: Float = 0.5f
    }
}
