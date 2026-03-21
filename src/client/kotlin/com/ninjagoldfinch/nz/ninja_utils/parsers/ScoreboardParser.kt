package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.core.SlayerCompleteEvent
import com.ninjagoldfinch.nz.ninja_utils.core.SlayerSpawnedEvent
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.RegexPatterns
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot

data class ParsedScoreboardLines(
    val purse: Long?,
    val bits: Int?,
    val location: String?,
    val slayerQuest: String?,
    val slayerBossSpawned: Boolean,
    val copper: Int?,
    val sowdust: Int?,
    val pests: Int?,
    val sbDate: String?,
    val objective: String?
)

object ScoreboardParser {
    private val logger = ModLogger.category("Scoreboard")

    var lastDate: String? = null
        private set

    fun parseLines(lines: List<String>, currentIsland: SkyBlockIsland?): ParsedScoreboardLines {
        var purse: Long? = null
        var bits: Int? = null
        var location: String? = null
        var slayerQuest: String? = null
        var slayerBossSpawned = false
        var copper: Int? = null
        var sowdust: Int? = null
        var pests: Int? = null
        var parsedObjective: String? = null
        var sbDate: String? = null
        var inSlayerSection = false

        for (line in lines) {
            if (line == "Slayer Quest") {
                inSlayerSection = true
                continue
            }

            if (inSlayerSection) {
                if (line == "Slay the boss!") {
                    slayerBossSpawned = true
                    continue
                }
                RegexPatterns.SLAYER_QUEST.find(line)?.let {
                    slayerQuest = "${it.groupValues[1]} ${it.groupValues[2]}"
                    continue
                }
                inSlayerSection = false
            }

            RegexPatterns.PURSE.find(line)?.let {
                purse = it.groupValues[1].replace(",", "").toLongOrNull()
            }
            RegexPatterns.BITS.find(line)?.let {
                bits = it.groupValues[1].replace(",", "").toIntOrNull()
            }
            RegexPatterns.LOCATION.find(line)?.let {
                location = it.groupValues[1].trim()
            }
            RegexPatterns.OBJECTIVE.find(line)?.let {
                parsedObjective = it.groupValues[1].trim()
            }
            if (currentIsland == SkyBlockIsland.GARDEN) {
                RegexPatterns.COPPER.find(line)?.let {
                    copper = it.groupValues[1].replace(",", "").toIntOrNull()
                }
                RegexPatterns.SOWDUST.find(line)?.let {
                    sowdust = it.groupValues[1].replace(",", "").toIntOrNull()
                }
                if (RegexPatterns.MAX_PESTS.containsMatchIn(line)) {
                    pests = 8
                } else {
                    RegexPatterns.PESTS.find(line)?.let {
                        pests = it.groupValues[1].toIntOrNull()
                    }
                }
            }
            RegexPatterns.SB_DATE.find(line)?.let {
                val prefix = it.groupValues[1].let { p -> if (p.isNotBlank()) "$p " else "" }
                sbDate = "$prefix${it.groupValues[2]} ${it.groupValues[3]}"
            }
        }

        return ParsedScoreboardLines(
            purse = purse,
            bits = bits,
            location = location,
            slayerQuest = slayerQuest,
            slayerBossSpawned = slayerBossSpawned,
            copper = copper,
            sowdust = sowdust,
            pests = pests,
            sbDate = sbDate,
            objective = parsedObjective
        )
    }

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
                    TextUtils.stripFormattingAndInvisible(display.string).trim()
                } else {
                    val team = scoreboard.getScoreHolderTeam(entry.owner)
                    if (team != null) {
                        // Use Team.decorateName to properly combine prefix + name + suffix
                        TextUtils.stripFormattingAndInvisible(team.decorateName(net.minecraft.text.Text.literal(entry.owner)).string).trim()
                    } else {
                        TextUtils.stripFormattingAndInvisible(entry.owner).trim()
                    }
                }
            }
            .filter { it.isNotBlank() }

        if (DebugCategory.logScoreboard) {
            logger.debug("Title: $title | Raw lines: $lines")
        }

        val parsed = parseLines(lines, HypixelState.currentIsland)

        lastDate = parsed.sbDate

        // Update shared state
        parsed.purse?.let { HypixelState.purse = it }
        parsed.bits?.let { HypixelState.bits = it }
        parsed.location?.let { HypixelState.currentArea = it }
        parsed.copper?.let { HypixelState.copper = it }
        parsed.sowdust?.let { HypixelState.sowdust = it }
        if (HypixelState.currentIsland == SkyBlockIsland.GARDEN) {
            // No pest line on scoreboard means 0 pests
            HypixelState.gardenPests = parsed.pests ?: 0
            // Extract plot number from area name (e.g., "Plot - 5")
            val areaForPlot = parsed.location ?: HypixelState.currentArea
            val plot = areaForPlot?.let { RegexPatterns.GARDEN_PLOT.find(it)?.groupValues?.get(1) }
            HypixelState.currentPlot = plot
        }
        SlayerTracker.updateFromScoreboard(parsed.slayerQuest)

        // Detect boss spawn from scoreboard
        if (parsed.slayerBossSpawned && !SlayerTracker.bossSpawned) {
            logger.info("Slayer boss spawned (detected from scoreboard)")
            SlayerTracker.onBossSpawned()
            EventBus.post(SlayerSpawnedEvent(SlayerTracker.activeQuest))
        } else if (!parsed.slayerBossSpawned && SlayerTracker.bossSpawned) {
            // "Slay the boss!" disappeared — boss was killed
            logger.info("Slayer boss no longer on scoreboard, resetting boss state")
            SlayerTracker.onBossSlain()
            EventBus.post(SlayerCompleteEvent(SlayerTracker.activeQuest))
        }

        return ScoreboardData(
            title = title,
            purse = parsed.purse,
            bits = parsed.bits,
            location = parsed.location,
            slayerQuest = parsed.slayerQuest,
            slayerProgress = null,
            slayerBossSpawned = parsed.slayerBossSpawned,
            objective = parsed.objective,
            rawLines = lines
        )
    }
}
