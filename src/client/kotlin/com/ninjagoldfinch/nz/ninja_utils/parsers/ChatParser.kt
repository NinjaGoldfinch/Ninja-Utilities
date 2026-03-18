package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.core.CoinChangeEvent
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.RareDropEvent
import com.ninjagoldfinch.nz.ninja_utils.core.SkillXpGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.SlayerCompleteEvent
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.RegexPatterns
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils
import net.minecraft.text.Text

data class ChatHandler(
    val name: String,
    val pattern: Regex,
    val handler: (MatchResult) -> Unit
)

object ChatParser {
    private val logger = ModLogger.category("Chat")
    private val handlers = mutableListOf<ChatHandler>()
    @PublishedApi internal var suppressed = false

    fun register(handler: ChatHandler) {
        handlers.add(handler)
    }

    /**
     * Runs [block] with chat parsing suppressed, so that any client-side
     * messages sent inside (e.g. mod alerts) are not picked up by handlers.
     */
    inline fun <T> suppressParsing(block: () -> T): T {
        suppressed = true
        try {
            return block()
        } finally {
            suppressed = false
        }
    }

    fun onChatMessage(message: Text, overlay: Boolean) {
        if (suppressed) return

        val raw = TextUtils.stripFormatting(message.string).trim()
        if (raw.isBlank()) return

        if (DebugCategory.logChatMessages) {
            logger.debug("Chat: $raw")
        }

        for (handler in handlers) {
            val match = handler.pattern.find(raw) ?: continue
            try {
                handler.handler(match)
            } catch (e: Exception) {
                logger.error("Error in handler '${handler.name}'", e)
            }
        }
    }

    fun registerDefaultHandlers() {
        register(ChatHandler("SkillXP", RegexPatterns.SKILL_XP) { match ->
            val xpGain = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return@ChatHandler
            val skill = match.groupValues[2].trim()
            val current = match.groupValues[3].replace(",", "").toLongOrNull() ?: return@ChatHandler
            val required = match.groupValues[4].replace(",", "").toLongOrNull() ?: return@ChatHandler
            logger.debug("Skill XP: +$xpGain $skill ($current/$required)")
            EventBus.post(SkillXpGainEvent(skill, xpGain, current, required))
        })

        register(ChatHandler("CoinChange", RegexPatterns.COIN_CHANGE) { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: return@ChatHandler
            val source = match.groupValues[2]
            logger.debug("Coins: $amount ($source)")
            EventBus.post(CoinChangeEvent(amount, source))
        })

        register(ChatHandler("RareDrop", RegexPatterns.RARE_DROP) { match ->
            val itemName = match.groupValues[1].trim()
            logger.info("RARE DROP: $itemName")
            EventBus.post(RareDropEvent(itemName))
        })

        register(ChatHandler("SlayerStarted", RegexPatterns.SLAYER_SPAWNED) {
            logger.info("Slayer quest started!")
            SlayerTracker.onQuestStarted()
        })

        register(ChatHandler("SlayerComplete", RegexPatterns.SLAYER_COMPLETE) {
            logger.info("Slayer boss defeated!")
            SlayerTracker.onBossSlain()
            EventBus.post(SlayerCompleteEvent(SlayerTracker.activeQuest))
        })

        register(ChatHandler("SkyBlockJoin", RegexPatterns.SKYBLOCK_JOIN) {
            logger.debug("Joined SkyBlock")
        })
    }
}
