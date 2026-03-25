package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.config.SkyblockCategory
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.ChatUtils
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils

object SlayerTracker {
    private val logger = ModLogger.category("Slayer")

    /** Time source, overridable for testing. */
    var timeProvider: () -> Long = { System.currentTimeMillis() }

    /** When true, scoreboard parser won't override slayer state (used by simulate commands). */
    var suppressScoreboardUpdates: Boolean = false

    var activeQuest: String? = null
        private set
    var questStartTime: Long = 0
        private set
    var bossSpawned: Boolean = false
        private set
    var bossSpawnTime: Long = 0
        private set
    /** Time in seconds it took to spawn the boss (quest start → boss spawn). */
    var spawnTime: Double? = null
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
        spawnTime = if (questStartTime > 0) (bossSpawnTime - questStartTime) / 1000.0 else null

        val spawnStr = spawnTime?.let { " (${TextUtils.formatDuration(it)})" } ?: ""
        logger.info("Slayer boss spawned!$spawnStr")

        if (SkyblockCategory.slayerBossAlert) {
            sendAlert("\u00a7c\u00a7lSLAYER BOSS SPAWNED!$spawnStr")
        }
    }

    fun onBossSlain() {
        val duration = if (bossSpawnTime > 0) {
            (timeProvider() - bossSpawnTime) / 1000.0
        } else null

        completionsThisSession++
        lastCompletionTime = timeProvider()
        bossSpawned = false

        val durationStr = if (duration != null) " in ${TextUtils.formatDuration(duration)}" else ""
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
        spawnTime = null
        completionsThisSession = 0
        suppressScoreboardUpdates = false
    }

    private fun sendAlert(message: String) {
        ChatUtils.sendModMessage(message)
    }
}
