package com.ninjagoldfinch.nz.ninja_utils.config

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption

@Category(value = "api")
class ApiCategory {
    companion object {
        @ConfigOption.Separator(
            value = "Backend",
            description = "Connection settings for the backend API"
        )

        @ConfigEntry(id = "enabled", translation = "Enable API")
        @Comment("Enable fetching data from the backend API")
        @JvmField var enabled: Boolean = false

        @ConfigEntry(id = "backendUrl", translation = "Backend URL")
        @Comment("URL of your backend API server")
        @JvmField var backendUrl: String = ""

        @ConfigOption.Separator(
            value = "Cache",
            description = "Control how long API responses are cached"
        )

        @ConfigEntry(id = "profileCacheTtl", translation = "Profile Cache TTL (s)")
        @Comment("How long to cache profile data in seconds")
        @ConfigOption.Range(min = 60.0, max = 3600.0)
        @JvmField var profileCacheTtl: Int = 300

        @ConfigEntry(id = "bazaarCacheTtl", translation = "Bazaar Cache TTL (s)")
        @Comment("How long to cache Bazaar prices in seconds")
        @ConfigOption.Range(min = 30.0, max = 600.0)
        @JvmField var bazaarCacheTtl: Int = 60
    }
}
