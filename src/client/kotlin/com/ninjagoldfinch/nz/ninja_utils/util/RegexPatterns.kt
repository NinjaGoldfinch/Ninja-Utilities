package com.ninjagoldfinch.nz.ninja_utils.util

/**
 * Shared compiled regex patterns used across parsers.
 */
object RegexPatterns {
    // Scoreboard patterns
    val PURSE = Regex("""(?:Purse|Piggy): ([\d,]+)""")
    val BITS = Regex("""Bits: ([\d,]+)""")
    val LOCATION = Regex("""[⏣ᏜⒸ] (.+)""")
    val SLAYER_QUEST = Regex("""([\w ]+) (I{1,4}|V)""")
    val SB_DATE = Regex("""(Early|Late)? ?(\w+) (\d+)(?:st|nd|rd|th)""")
    val OBJECTIVE = Regex("""Objective: (.+)""")

    // Garden patterns
    val COPPER = Regex("""Copper: ([\d,]+)""")
    val COMPOST = Regex("""Compost: ([\d,]+)""")

    // Chat patterns
    val SKILL_XP = Regex("""\+?([\d,.]+) ([\w][\w ]*?) \(([\d,.]+)/([\d,.]+)\)""")
    val COIN_CHANGE = Regex("""([+-][\d,.]+) coins? \((.+)\)""")
    val RARE_DROP = Regex("""(?:RARE|VERY RARE|CRAZY RARE|INSANE|PRAY RNGESUS) DROP! (?:\(.+?\) )?(.+)""")

    // Slayer patterns
    val SLAYER_SPAWNED = Regex("""SLAYER QUEST STARTED!""")
    val SLAYER_COMPLETE = Regex("""NICE! SLAYER BOSS SLAIN!""")

    // Connection
    val SKYBLOCK_JOIN = Regex("""Welcome to Hypixel SkyBlock!""")
}
