package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Config
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo
import com.teamresourceful.resourcefulconfig.api.loader.Configurator

@Config(
    value = "ninja_utils",
    categories = [
        GeneralCategory::class,
        HudCategory::class,
        SkyblockCategory::class,
        ApiCategory::class,
        DebugCategory::class
    ]
)
@ConfigInfo(
    titleTranslation = "ninja_utils.config.title",
    descriptionTranslation = "ninja_utils.config.description"
)
class ModConfig {
    companion object {
        @JvmField
        val CONFIGURATOR = Configurator("ninja_utils")
    }
}
