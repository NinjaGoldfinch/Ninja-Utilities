package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.core.CoinChangeEvent
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainSource
import com.ninjagoldfinch.nz.ninja_utils.core.RareDropEvent
import com.ninjagoldfinch.nz.ninja_utils.core.SkillXpGainEvent
import com.ninjagoldfinch.nz.ninja_utils.config.SkyblockCategory
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.core.SlayerCompleteEvent
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.RegexPatterns
import com.ninjagoldfinch.nz.ninja_utils.util.TextUtils
import net.minecraft.text.HoverEvent
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

    /** Ring buffer of recent chat messages with formatting codes intact. */
    private const val MAX_RECENT = 50
    private val recentMessages = ArrayDeque<String>(MAX_RECENT)
    private val recentLock = Any()

    /** Returns recent chat messages with raw formatting codes (§ notation). */
    fun getRecentFormatted(): List<String> = synchronized(recentLock) { recentMessages.toList() }

    fun register(handler: ChatHandler) {
        handlers.add(handler)
    }

    fun reset() {
        handlers.clear()
        suppressed = false
        synchronized(recentLock) { recentMessages.clear() }
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

        // Store raw formatted version for dev inspection
        val formatted = message.string.trim()
        if (formatted.isNotBlank()) {
            synchronized(recentLock) {
                if (recentMessages.size >= MAX_RECENT) recentMessages.removeFirst()
                recentMessages.addLast(formatted)
            }
        }

        val raw = TextUtils.stripFormattingAndInvisible(message.string).trim()
        if (raw.isBlank()) return

        if (DebugCategory.logChatMessages) {
            logger.debug("Chat: $raw")
        }

        // Handle sack summary messages with hover text (e.g. "[Sacks] +4 items. (Last 5s.)")
        if (raw.contains("[Sacks]")) {
            if (!SkyblockCategory.trackItemGains || !SkyblockCategory.trackSackGains) {
                logger.debug("Sack message ignored (trackItemGains=${SkyblockCategory.trackItemGains}, trackSackGains=${SkyblockCategory.trackSackGains})")
            } else {
                val summaryMatch = RegexPatterns.SACK_SUMMARY.containsMatchIn(raw)
                logger.debug("Sack message detected, summary regex match=$summaryMatch, raw='$raw'")
                if (summaryMatch) {
                    parseSackHoverText(message)
                }
            }
        }

        processMessage(raw)
    }

    /**
     * Extracts item gain data from sack summary hover text.
     * Hypixel sends "[Sacks] +N items." with hover text containing individual items
     * like "+4x Enchanted Diamond" on separate lines.
     */
    private fun parseSackHoverText(message: Text) {
        // Walk all text components looking for hover events, deduplicate
        val hoverTexts = mutableSetOf<String>()
        collectHoverTexts(message, hoverTexts)

        logger.debug("Sack hover: found ${hoverTexts.size} unique hover text(s)")
        for (hoverText in hoverTexts) {
            logger.debug("Sack hover raw: '$hoverText'")
            val lines = hoverText.split("\n")
            for (line in lines) {
                val stripped = TextUtils.stripFormattingAndInvisible(line).trim()
                val match = RegexPatterns.SACK_HOVER_ITEM.find(stripped) ?: continue
                val amount = match.groupValues[1].replace(",", "").toIntOrNull() ?: continue
                if (amount <= 0) continue
                val itemName = match.groupValues[2].trim()
                logger.debug("Sack (hover): +$amount $itemName")
                EventBus.post(ItemGainEvent(
                    itemId = itemName.uppercase().replace(" ", "_"),
                    displayName = itemName,
                    amount = amount,
                    source = ItemGainSource.SACK
                ))
            }
        }
    }

    private fun collectHoverTexts(text: Text, result: MutableSet<String>) {
        val hover = text.style.hoverEvent
        if (hover is HoverEvent.ShowText) {
            result.add(hover.value.string)
        }
        for (sibling in text.siblings) {
            collectHoverTexts(sibling, result)
        }
    }

    fun processMessage(raw: String) {
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

        register(ChatHandler("PestSpawned", RegexPatterns.PEST_SPAWNED) { match ->
            if (HypixelState.currentIsland == SkyBlockIsland.GARDEN) {
                val plot = match.groupValues[1]
                logger.info("Pest spawned in Plot $plot (count updated via scoreboard)")
            }
        })

        register(ChatHandler("PestCleared", RegexPatterns.PEST_CLEARED) {
            if (HypixelState.currentIsland == SkyBlockIsland.GARDEN) {
                logger.info("Pest cleared (count updated via scoreboard)")
            }
        })

        // Note: Sack messages are handled via hover text parsing in onChatMessage(),
        // not here, because Hypixel sends "[Sacks] +N items." with item details
        // only in the hover tooltip.
    }
}
