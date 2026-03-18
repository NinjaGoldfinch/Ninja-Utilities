package com.ninjagoldfinch.nz.ninja_utils.core

import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger

/**
 * Lightweight internal event bus for mod-specific events.
 */
object EventBus {
    @PublishedApi
    internal val listeners = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> subscribe(noinline handler: (T) -> Unit) {
        val list = listeners.getOrPut(T::class.java) { mutableListOf() }
        list.add(handler as (Any) -> Unit)
    }

    fun post(event: Any) {
        val handlers = listeners[event::class.java] ?: return
        for (handler in handlers) {
            try {
                handler(event)
            } catch (e: Exception) {
                ModLogger.error("EventBus", "Error handling ${event::class.simpleName}", e)
            }
        }
    }
}

// Events
data class IslandChangeEvent(
    val previous: SkyBlockIsland?,
    val current: SkyBlockIsland?
)

data class SkillXpGainEvent(
    val skill: String,
    val xpGain: Double,
    val current: Long,
    val required: Long
)

data class CoinChangeEvent(
    val amount: Long,
    val source: String
)

data class RareDropEvent(
    val itemName: String
)

data class SlayerSpawnedEvent(val questName: String?)

data class SlayerCompleteEvent(val questName: String?)
