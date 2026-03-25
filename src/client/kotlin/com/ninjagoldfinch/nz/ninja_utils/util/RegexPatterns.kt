package com.ninjagoldfinch.nz.ninja_utils.util

/**
 * Shared compiled regex patterns used across parsers.
 */
object RegexPatterns {
    // Scoreboard patterns
    val PURSE = Regex("""(?:Purse|Piggy): ([\d,]+)""")
    val BITS = Regex("""Bits: ([\d,]+)""")
    val LOCATION = Regex("""[⏣ᏜⒸ] (.+)""")
    val SLAYER_QUEST = Regex("""([\w ]+) (IV|V|I{1,3})""")
    val SB_DATE = Regex("""(Early|Late)? ?(\w+) (\d+)(?:st|nd|rd|th)""")
    val OBJECTIVE = Regex("""Objective: (.+)""")

    // Garden patterns
    val COPPER = Regex("""Copper: ([\d,]+)""")
    val SOWDUST = Regex("""Sowdust: ([\d,]+)""")
    val PESTS = Regex("""Pests?: (\d+)""")
    val MAX_PESTS = Regex("""MAX PESTS""")
    val GARDEN_PLOT = Regex("""Plot - (\d+)""")

    // Chat: pest events
    val PEST_SPAWNED = Regex("""Pest has appeared in Plot - (\d+)!""", RegexOption.IGNORE_CASE)
    val PEST_CLEARED = Regex("""pest (?:has been )?(?:killed|removed)""", RegexOption.IGNORE_CASE)

    // Chat patterns
    val SKILL_XP = Regex("""\+?([\d,.]+) ([\w][\w ]*?) \(([\d,.]+)/([\d,.]+)\)""")
    val COIN_CHANGE = Regex("""([+-][\d,.]+) coins? \((.+)\)""")
    val RARE_DROP = Regex("""(?:RARE|VERY RARE|CRAZY RARE|INSANE|PRAY RNGESUS) DROP! (?:\(.+?\) )?(.+)""")

    // Slayer patterns
    val SLAYER_SPAWNED = Regex("""SLAYER QUEST STARTED!""")
    val SLAYER_COMPLETE = Regex("""NICE! SLAYER BOSS SLAIN!""")

    // Sack patterns
    val SACK_CHANGE = Regex("""\[Sacks] ([+-][\d,]+)x? (.+)""")
    val SACK_SUMMARY = Regex("""\[Sacks] \+(\d+) items?\.""")
    val SACK_HOVER_ITEM = Regex("""([+-][\d,]+)x? (.+)""")

    // Connection
    val SKYBLOCK_JOIN = Regex("""Welcome to Hypixel SkyBlock!""")
}
