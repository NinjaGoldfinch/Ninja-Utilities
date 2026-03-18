package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.config.GeneralCategory
import com.ninjagoldfinch.nz.ninja_utils.config.SkyblockCategory
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object SlayerTracker {
    private val logger = ModLogger.category("Slayer")

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
        // Quest info comes from scoreboard parsing
        questStartTime = System.currentTimeMillis()
        bossSpawned = false
        logger.info("Slayer quest started")
    }

    fun onBossSpawned() {
        bossSpawned = true
        bossSpawnTime = System.currentTimeMillis()
        logger.info("Slayer boss spawned!")

        if (SkyblockCategory.slayerBossAlert) {
            sendAlert("\u00a7c\u00a7lSLAYER BOSS SPAWNED!")
        }
    }

    fun onBossSlain() {
        val duration = if (bossSpawnTime > 0) {
            (System.currentTimeMillis() - bossSpawnTime) / 1000
        } else null

        completionsThisSession++
        lastCompletionTime = System.currentTimeMillis()
        bossSpawned = false

        val durationStr = if (duration != null) " in ${duration}s" else ""
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
        val player = MinecraftClient.getInstance().player ?: return
        if (GeneralCategory.chatPrefix) {
            player.sendMessage(Text.literal("\u00a78[\u00a76NinjaUtils\u00a78]\u00a7r $message"), false)
        } else {
            player.sendMessage(Text.literal(message), false)
        }
    }
}
