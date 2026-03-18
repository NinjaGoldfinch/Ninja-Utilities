package com.ninjagoldfinch.nz.ninja_utils.features.warnings

import com.ninjagoldfinch.nz.ninja_utils.config.GeneralCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.util.ChatUtils

object WarningManager {
    private val logger = ModLogger.category("Warnings")

    private var lastUnknownLocationWarning = 0L
    private var lastModApiWarning = 0L
    private var joinTime = 0L
    private var warnedModApiMissing = false

    private const val UNKNOWN_LOCATION_COOLDOWN_MS = 30_000L
    private const val MOD_API_GRACE_PERIOD_MS = 10_000L

    fun onJoinServer() {
        joinTime = System.currentTimeMillis()
        warnedModApiMissing = false
    }

    fun onDisconnect() {
        joinTime = 0
        warnedModApiMissing = false
    }

    fun tick() {
        if (!GeneralCategory.enabled || !GeneralCategory.showWarnings) return
        if (joinTime == 0L) return

        val now = System.currentTimeMillis()
        val timeSinceJoin = now - joinTime

        // Warning: Mod API not responding after grace period
        if (!warnedModApiMissing
            && timeSinceJoin > MOD_API_GRACE_PERIOD_MS
            && HypixelState.lastHelloPacketTime == 0L
            && HypixelState.isOnHypixel
        ) {
            // Only warn if scoreboard fallback detected Hypixel but Mod API never responded
            sendModMessage("\u00a7eHypixel Mod API not detected. Location features may be limited.")
            sendModMessage("\u00a77Ensure the Hypixel Mod API mod is installed.")
            logger.warn("Mod API not responding after ${timeSinceJoin / 1000}s")
            warnedModApiMissing = true
        }

        // Warning: Unknown location (on Hypixel but no server type known)
        // Skip warning if in limbo or lobby — these are expected states
        if (HypixelState.isLocationUnknown
            && !HypixelState.isInLimbo
            && !HypixelState.isInLobby
            && timeSinceJoin > MOD_API_GRACE_PERIOD_MS
            && now - lastUnknownLocationWarning > UNKNOWN_LOCATION_COOLDOWN_MS
        ) {
            sendModMessage("\u00a7eLocation detection unavailable. Some features may not work.")
            logger.warn("Unknown location — serverType is null")
            lastUnknownLocationWarning = now
        }

        // Info: Unknown island mode (on SkyBlock but mode not in our enum)
        if (HypixelState.isInSkyBlock
            && HypixelState.mode != null
            && HypixelState.currentIsland == null
        ) {
            val mode = HypixelState.mode
            logger.warn("Unknown SkyBlock island mode: '$mode' — consider adding to SkyBlockIsland enum")
        }
    }

    private fun sendModMessage(message: String) {
        ChatUtils.sendModMessage(message)
    }
}
