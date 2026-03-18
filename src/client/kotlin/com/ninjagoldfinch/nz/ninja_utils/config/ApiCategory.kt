package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "api")
class ApiCategory {
    companion object {
        @ConfigOption.Separator(value = "ninja_utils.config.api.backend")

        @ConfigEntry(id = "enabled", translation = "ninja_utils.config.api.enabled")
        @Comment(value = "Enable fetching data from the backend API")
        @JvmField var enabled: Boolean = false

        @ConfigEntry(id = "backendUrl", translation = "ninja_utils.config.api.backendUrl")
        @Comment(value = "URL of your backend API server")
        @JvmField var backendUrl: String = ""

        @ConfigOption.Separator(value = "ninja_utils.config.api.cache")

        @ConfigEntry(id = "profileCacheTtl", translation = "ninja_utils.config.api.profileCacheTtl")
        @ConfigOption.Range(min = 60.0, max = 3600.0)
        @Comment(value = "Profile cache TTL in seconds")
        @JvmField var profileCacheTtl: Int = 300

        @ConfigEntry(id = "bazaarCacheTtl", translation = "ninja_utils.config.api.bazaarCacheTtl")
        @ConfigOption.Range(min = 30.0, max = 600.0)
        @Comment(value = "Bazaar price cache TTL in seconds")
        @JvmField var bazaarCacheTtl: Int = 60
    }
}
