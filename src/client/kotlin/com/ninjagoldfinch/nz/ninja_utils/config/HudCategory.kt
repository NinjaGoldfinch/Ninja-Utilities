package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "hud")
class HudCategory {
    companion object {
        @ConfigOption.Separator(value = "ninja_utils.config.hud.basic")

        @ConfigEntry(id = "showLocation", translation = "ninja_utils.config.hud.showLocation")
        @JvmField var showLocation: Boolean = true

        @ConfigEntry(id = "showPurse", translation = "ninja_utils.config.hud.showPurse")
        @JvmField var showPurse: Boolean = true

        @ConfigOption.Separator(value = "ninja_utils.config.hud.advanced")

        @ConfigEntry(id = "showBits", translation = "ninja_utils.config.hud.showBits")
        @JvmField var showBits: Boolean = false

        @ConfigEntry(id = "showSkillProgress", translation = "ninja_utils.config.hud.showSkillProgress")
        @JvmField var showSkillProgress: Boolean = false

        @ConfigEntry(id = "showSlayer", translation = "ninja_utils.config.hud.showSlayer")
        @JvmField var showSlayer: Boolean = false

        @ConfigEntry(id = "showStats", translation = "ninja_utils.config.hud.showStats")
        @JvmField var showStats: Boolean = false

        @ConfigEntry(id = "showMayor", translation = "ninja_utils.config.hud.showMayor")
        @JvmField var showMayor: Boolean = false

        @ConfigEntry(id = "showPet", translation = "ninja_utils.config.hud.showPet")
        @JvmField var showPet: Boolean = false

        @ConfigEntry(id = "showCookieTimer", translation = "ninja_utils.config.hud.showCookieTimer")
        @JvmField var showCookieTimer: Boolean = false

        @ConfigEntry(id = "showSbTime", translation = "ninja_utils.config.hud.showSbTime")
        @JvmField var showSbTime: Boolean = false

        @ConfigEntry(id = "showPing", translation = "ninja_utils.config.hud.showPing")
        @JvmField var showPing: Boolean = false

        @ConfigEntry(id = "showTps", translation = "ninja_utils.config.hud.showTps")
        @JvmField var showTps: Boolean = false

        @ConfigEntry(id = "showDebugOverlay", translation = "ninja_utils.config.hud.showDebugOverlay")
        @JvmField var showDebugOverlay: Boolean = false

        @ConfigOption.Separator(value = "ninja_utils.config.hud.appearance")

        @ConfigEntry(id = "hudScale", translation = "ninja_utils.config.hud.hudScale")
        @ConfigOption.Range(min = 0.5, max = 3.0)
        @ConfigOption.Slider
        @JvmField var hudScale: Float = 1.0f

        @ConfigEntry(id = "hudBackgroundOpacity", translation = "ninja_utils.config.hud.hudBackgroundOpacity")
        @ConfigOption.Range(min = 0.0, max = 1.0)
        @ConfigOption.Slider
        @JvmField var hudBackgroundOpacity: Float = 0.5f
    }
}
