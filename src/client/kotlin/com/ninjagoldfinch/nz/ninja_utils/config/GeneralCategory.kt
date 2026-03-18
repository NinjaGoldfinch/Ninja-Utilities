package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "general")
class GeneralCategory {
    companion object {
        @ConfigOption.Separator(value = "ninja_utils.config.general.behavior")

        @ConfigEntry(id = "enabled", translation = "ninja_utils.config.general.enabled")
        @Comment(value = "Master toggle for all mod features")
        @JvmField var enabled: Boolean = true

        @ConfigEntry(id = "skyblockOnly", translation = "ninja_utils.config.general.skyblockOnly")
        @Comment(value = "Only activate features when in SkyBlock")
        @JvmField var skyblockOnly: Boolean = true

        @ConfigOption.Separator(value = "ninja_utils.config.general.notifications")

        @ConfigEntry(id = "showWarnings", translation = "ninja_utils.config.general.showWarnings")
        @Comment(value = "Show warning messages for unknown locations etc.")
        @JvmField var showWarnings: Boolean = true

        @ConfigEntry(id = "chatPrefix", translation = "ninja_utils.config.general.chatPrefix")
        @Comment(value = "Show [NinjaUtils] prefix on mod messages")
        @JvmField var chatPrefix: Boolean = true
    }
}
