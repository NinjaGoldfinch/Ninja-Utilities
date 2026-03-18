package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.SlayerSpawnedEvent
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils
import net.minecraft.client.MinecraftClient
object TabListParser {
    private val logger = ModLogger.category("TabList")

    var lastData: TabListData? = null
        private set

    fun parse(): TabListData? {
        val networkHandler = MinecraftClient.getInstance().networkHandler ?: return null
        val entries = networkHandler.playerList
            .sortedBy { it.listOrder }

        val lines = entries.mapNotNull { entry ->
            entry.displayName?.string?.let { TextUtils.stripFormatting(it).trim() }
        }.filter { it.isNotBlank() }

        if (DebugCategory.logTabList) {
            logger.debug("Raw lines (${lines.size}): $lines")
        }

        var skills: String? = null
        var profile: String? = null
        var pet: String? = null
        var cookie: String? = null
        var slayerBossSpawned = false
        val stats = mutableListOf<String>()
        var currentSection: String? = null

        for (line in lines) {
            when {
                line.startsWith("Skills:") -> {
                    currentSection = "skills"
                    skills = line.removePrefix("Skills:").trim().ifBlank { null }
                }
                line.startsWith("Stats:") -> currentSection = "stats"
                line.startsWith("Profile:") -> {
                    currentSection = null
                    profile = line.removePrefix("Profile:").trim().ifBlank { null }
                }
                line.startsWith("Pet:") -> {
                    currentSection = null
                    pet = line.removePrefix("Pet:").trim().ifBlank { null }
                }
                line.startsWith("Cookie Buff:") -> {
                    currentSection = null
                    cookie = line.removePrefix("Cookie Buff:").trim().ifBlank { null }
                }
                line.startsWith("Slayer:") -> currentSection = "slayer"
                currentSection == "slayer" && line == "Slay the boss!" -> slayerBossSpawned = true
                currentSection == "stats" -> stats.add(line)
                currentSection == "skills" && skills == null -> skills = line.trim()
            }
        }

        // Update slayer boss state from tab list
        if (slayerBossSpawned && !SlayerTracker.bossSpawned) {
            logger.info("Slayer boss spawned (detected from tab list)")
            SlayerTracker.onBossSpawned()
            EventBus.post(SlayerSpawnedEvent(SlayerTracker.activeQuest))
        }

        val data = TabListData(
            skills = skills,
            stats = stats,
            profile = profile,
            pet = pet,
            cookie = cookie,
            slayerBossSpawned = slayerBossSpawned,
            rawLines = lines
        )
        lastData = data
        return data
    }
}
