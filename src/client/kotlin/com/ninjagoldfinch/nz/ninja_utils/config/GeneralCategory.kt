package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "general")
class GeneralCategory {
    companion object {
        @ConfigOption.Separator(
            value = "Behavior",
            description = "Core mod behavior settings"
        )

        @ConfigEntry(id = "enabled", translation = "Enabled")
        @Comment("Master toggle for all mod features")
        @JvmField var enabled: Boolean = true

        @ConfigEntry(id = "skyblockOnly", translation = "SkyBlock Only")
        @Comment("Only activate features when in SkyBlock")
        @JvmField var skyblockOnly: Boolean = true

        @ConfigOption.Separator(
            value = "Notifications",
            description = "Control mod messages and alerts"
        )

        @ConfigEntry(id = "showWarnings", translation = "Show Warnings")
        @Comment("Show warning messages for unknown locations etc.")
        @JvmField var showWarnings: Boolean = true

        @ConfigEntry(id = "chatPrefix", translation = "Chat Prefix")
        @Comment("Show [NinjaUtils] prefix on mod messages")
        @JvmField var chatPrefix: Boolean = true
    }
}
