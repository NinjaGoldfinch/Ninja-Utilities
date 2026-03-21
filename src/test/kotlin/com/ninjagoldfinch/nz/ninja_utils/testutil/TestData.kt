package com.ninjagoldfinch.nz.ninja_utils.testutil

/**
 * Shared test data representing real Hypixel SkyBlock messages and scoreboard lines.
 *
 * When Hypixel changes a format, update the relevant constant here and all
 * tests using it will either pass (format-compatible change) or fail clearly
 * (breaking change that needs code updates).
 *
 * To add new test data: add a constant with a comment noting where the data
 * came from and what it represents.
 */
object TestData {

    /** Scoreboard lines as they appear after stripping formatting codes and invisible chars. */
    object Scoreboard {
        // Currency lines (from real SkyBlock scoreboard sidebar)
        const val PURSE_SIMPLE = "Purse: 500"
        const val PURSE_COMMAS = "Purse: 1,234,567"
        const val PIGGY_BANK = "Piggy: 10,000"
        const val BITS_SIMPLE = "Bits: 500"
        const val BITS_COMMAS = "Bits: 12,345"

        // Location lines (from real SkyBlock scoreboard sidebar — icon prefix varies by area type)
        const val LOCATION_PARK = "⏣ The Park"
        const val LOCATION_GARDEN = "Ꮬ Garden"
        const val LOCATION_DWARVEN = "⏣ Dwarven Mines"
        const val LOCATION_HUB = "⏣ Village"
        const val LOCATION_PLOT = "⏣ Plot - 3"
        const val LOCATION_COPPERHEAD = "Ⓒ Copper Mining"

        // Garden-specific lines
        const val COPPER_SIMPLE = "Copper: 500"
        const val COPPER_COMMAS = "Copper: 1,234"
        const val SOWDUST_SIMPLE = "Sowdust: 100"
        const val SOWDUST_COMMAS = "Sowdust: 5,678"
        const val PESTS_SINGLE = "Pest: 1"
        const val PESTS_MULTIPLE = "Pests: 3"
        const val PESTS_MAX = "MAX PESTS"

        // Slayer lines
        const val SLAYER_QUEST_HEADER = "Slayer Quest"
        const val SLAYER_REVENANT_IV = "Revenant Horror IV"
        const val SLAYER_TARANTULA_III = "Tarantula Broodfather III"
        const val SLAYER_SLAY_BOSS = "Slay the boss!"

        // Date lines (from SkyBlock calendar on scoreboard)
        const val DATE_SIMPLE = "Spring 5th"
        const val DATE_EARLY = "Early Spring 1st"
        const val DATE_LATE = "Late Winter 22nd"
        const val DATE_SECOND = "Summer 2nd"
        const val DATE_THIRD = "Autumn 3rd"

        // Objective line
        const val OBJECTIVE = "Objective: Talk to the Farmer"

        // Lines that should NOT match any pattern
        const val IRRELEVANT_EMPTY = ""
        const val IRRELEVANT_SEPARATOR = "---------------"
        const val IRRELEVANT_WEBSITE = "www.hypixel.net"
    }

    /** Chat messages as they appear after stripping formatting codes. */
    object Chat {
        // Skill XP messages (from real SkyBlock chat)
        const val SKILL_XP_MINING = "+1,234.5 Mining (50,000/100,000)"
        const val SKILL_XP_FARMING = "+500 Farming (1,000/5,000)"
        const val SKILL_XP_COMBAT = "+100 Combat (200/1,000)"

        // Coin change messages
        const val COIN_GAIN = "+500 coins (Auction)"
        const val COIN_LOSS = "-1,000 coins (Shop)"
        const val COIN_LARGE = "+1,234,567 coins (Bazaar)"

        // Rare drop messages
        const val RARE_DROP = "RARE DROP! Enchanted Diamond"
        const val VERY_RARE_DROP = "VERY RARE DROP! Enchanted Emerald Block"
        const val CRAZY_RARE_WITH_MF = "CRAZY RARE DROP! (+420% Magic Find) Wither Catalyst"

        // Slayer messages
        const val SLAYER_STARTED = "SLAYER QUEST STARTED!"
        const val SLAYER_COMPLETE = "NICE! SLAYER BOSS SLAIN!"

        // SkyBlock join
        const val SKYBLOCK_JOIN = "Welcome to Hypixel SkyBlock!"

        // Pest messages
        const val PEST_SPAWNED = "Pest has appeared in Plot - 5!"
        const val PEST_CLEARED = "pest has been killed"
    }

    /** Full scoreboard line lists representing complete scoreboard states. */
    object ScoreboardScenarios {
        val GARDEN_WITH_PESTS = listOf(
            "Early Spring 1st",
            "Purse: 1,234,567",
            "⏣ Plot - 3",
            "Copper: 1,234",
            "Pests: 2"
        )

        val GARDEN_NO_PESTS = listOf(
            "Spring 5th",
            "Purse: 500",
            "Ꮬ Garden",
            "Copper: 100",
            "Sowdust: 5,678"
        )

        val SLAYER_ACTIVE = listOf(
            "Purse: 500",
            "⏣ Spider's Den",
            "Slayer Quest",
            "Tarantula Broodfather III",
            "Slay the boss!"
        )

        val SLAYER_NO_BOSS = listOf(
            "Purse: 500",
            "⏣ Spider's Den",
            "Slayer Quest",
            "Revenant Horror IV"
        )

        val HUB_BASIC = listOf(
            "Summer 2nd",
            "Purse: 10,000",
            "⏣ Village",
            "Bits: 500"
        )
    }
}
