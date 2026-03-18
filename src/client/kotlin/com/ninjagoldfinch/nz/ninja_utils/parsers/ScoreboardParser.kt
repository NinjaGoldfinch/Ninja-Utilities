package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.RegexPatterns
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot

object ScoreboardParser {
    private val logger = ModLogger.category("Scoreboard")

    var lastDate: String? = null
        private set

    fun parse(): ScoreboardData? {
        val scoreboard = MinecraftClient.getInstance().world?.scoreboard ?: return null
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return null

        // Fallback: detect SkyBlock from scoreboard title only when Mod API hasn't reported
        val title = TextUtils.stripFormatting(objective.displayName.string)
        if (!HypixelState.isInSkyBlock && title.contains("SkyBlock", ignoreCase = true)) {
            if (!HypixelState.isOnHypixel) {
                HypixelState.isOnHypixel = true
                logger.info("Fallback: detected Hypixel via scoreboard title: '$title'")
            }
            if (HypixelState.serverType == null) {
                HypixelState.serverType = "SKYBLOCK"
                HypixelState.skyblockDetectedViaScoreboard = true
                logger.info("Fallback: detected SkyBlock via scoreboard title: '$title'")
            }
        }

        val entries = scoreboard.getScoreboardEntries(objective)
        val lines = entries
            .sortedByDescending { it.value }
            .mapNotNull { entry ->
                // Prefer the display name if available (clean text)
                val display = entry.display
                if (display != null) {
                    TextUtils.stripFormatting(display.string).trim()
                } else {
                    val team = scoreboard.getScoreHolderTeam(entry.owner)
                    if (team != null) {
                        // Use Team.decorateName to properly combine prefix + name + suffix
                        TextUtils.stripFormatting(team.decorateName(net.minecraft.text.Text.literal(entry.owner)).string).trim()
                    } else {
                        TextUtils.stripFormatting(entry.owner).trim()
                    }
                }
            }
            .filter { it.isNotBlank() }

        if (DebugCategory.logScoreboard) {
            logger.debug("Title: $title | Raw lines: $lines")
        }

        var purse: Long? = null
        var bits: Int? = null
        var location: String? = null
        var slayerQuest: String? = null
        var slayerProgress: String? = null
        var parsedObjective: String? = null
        var sbDate: String? = null

        for (line in lines) {
            RegexPatterns.PURSE.find(line)?.let {
                purse = it.groupValues[1].replace(",", "").toLongOrNull()
            }
            RegexPatterns.BITS.find(line)?.let {
                bits = it.groupValues[1].replace(",", "").toIntOrNull()
            }
            RegexPatterns.LOCATION.find(line)?.let {
                location = it.groupValues[1].trim()
            }
            RegexPatterns.SLAYER_QUEST.find(line)?.let {
                slayerQuest = "${it.groupValues[1]} ${it.groupValues[2]}"
            }
            RegexPatterns.OBJECTIVE.find(line)?.let {
                parsedObjective = it.groupValues[1].trim()
            }
            RegexPatterns.SB_DATE.find(line)?.let {
                val prefix = it.groupValues[1].let { p -> if (p.isNotBlank()) "$p " else "" }
                sbDate = "$prefix${it.groupValues[2]} ${it.groupValues[3]}"
            }
        }

        lastDate = sbDate

        // Update shared state
        purse?.let { HypixelState.purse = it }
        bits?.let { HypixelState.bits = it }
        location?.let { HypixelState.currentArea = it }
        SlayerTracker.updateFromScoreboard(slayerQuest)

        return ScoreboardData(
            title = title,
            purse = purse,
            bits = bits,
            location = location,
            slayerQuest = slayerQuest,
            slayerProgress = slayerProgress,
            objective = parsedObjective,
            rawLines = lines
        )
    }
}
