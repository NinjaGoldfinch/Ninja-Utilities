package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.config.SkyblockCategory
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.ChatUtils

object SlayerTracker {
    private val logger = ModLogger.category("Slayer")

    /** Time source, overridable for testing. */
    var timeProvider: () -> Long = { timeProvider() }

    var activeQuest: String? = null
        private set
    var questStartTime: Long = 0
        private set
    var bossSpawned: Boolean = false
        private set
    var bossSpawnTime: Long = 0
        private set
    var lastCompletionTime: Long = 0
        private set
    var completionsThisSession: Int = 0
        private set

    fun onQuestStarted() {
        if (!SkyblockCategory.slayerTracker) return
        questStartTime = timeProvider()
        bossSpawned = false
        logger.info("Slayer quest started")

        if (SkyblockCategory.slayerBossAlert) {
            sendAlert("\u00a7e\u00a7lSLAYER QUEST STARTED!")
        }
    }

    fun onBossSpawned() {
        bossSpawned = true
        bossSpawnTime = timeProvider()
        logger.info("Slayer boss spawned!")

        if (SkyblockCategory.slayerBossAlert) {
            sendAlert("\u00a7c\u00a7lSLAYER BOSS SPAWNED!")
        }
    }

    fun onBossSlain() {
        val duration = if (bossSpawnTime > 0) {
            (timeProvider() - bossSpawnTime) / 1000.0
        } else null

        completionsThisSession++
        lastCompletionTime = timeProvider()
        bossSpawned = false

        val durationStr = if (duration != null) " in ${"%.1f".format(duration)}s" else ""
        logger.info("Slayer boss slain$durationStr (session total: $completionsThisSession)")

        if (SkyblockCategory.slayerBossAlert) {
            sendAlert("\u00a7a\u00a7lSLAYER BOSS SLAIN!$durationStr")
        }
    }

    fun updateFromScoreboard(questName: String?) {
        activeQuest = questName
    }

    fun reset() {
        activeQuest = null
        questStartTime = 0
        bossSpawned = false
        bossSpawnTime = 0
        completionsThisSession = 0
    }

    private fun sendAlert(message: String) {
        ChatUtils.sendModMessage(message)
    }
}
