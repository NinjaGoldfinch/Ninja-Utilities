package com.ninjagoldfinch.nz.ninja_utils.core

enum class SkyBlockIsland(val mode: String, val displayName: String) {
    PRIVATE_ISLAND("dynamic", "Private Island"),
    HUB("hub", "Hub"),
    FARMING_ISLANDS("farming_1", "The Farming Islands"),
    THE_PARK("foraging_1", "The Park"),
    GOLD_MINE("mining_1", "Gold Mine"),
    DEEP_CAVERNS("mining_2", "Deep Caverns"),
    DWARVEN_MINES("mining_3", "Dwarven Mines"),
    CRYSTAL_HOLLOWS("crystal_hollows", "Crystal Hollows"),
    SPIDERS_DEN("combat_1", "Spider's Den"),
    BLAZING_FORTRESS("combat_2", "Blazing Fortress"),
    THE_END("combat_3", "The End"),
    CRIMSON_ISLE("crimson_isle", "Crimson Isle"),
    DUNGEON("dungeon", "Dungeons"),
    DUNGEON_HUB("dungeon_hub", "Dungeon Hub"),
    GARDEN("garden", "Garden"),
    THE_RIFT("rift", "The Rift"),
    KUUDRA("kuudra", "Kuudra"),
    INSTANCED("instanced", "Instanced"),
    JERRYS_WORKSHOP("winter", "Jerry's Workshop");

    companion object {
        private val BY_MODE = entries.associateBy { it.mode }

        fun fromMode(mode: String): SkyBlockIsland? = BY_MODE[mode]
    }
}
