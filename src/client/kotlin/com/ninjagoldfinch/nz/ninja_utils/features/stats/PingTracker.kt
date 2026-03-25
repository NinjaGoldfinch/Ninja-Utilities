package com.ninjagoldfinch.nz.ninja_utils.features.stats

import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket
import net.minecraft.util.Util

/**
 * Measures ping by sending QueryPingC2SPacket and measuring round-trip to PingResultS2CPacket.
 * Same approach as NobaAddons — we send our own ping packets on a timer since
 * MC's PingMeasurer only runs when the F3 network chart is visible.
 */
object PingTracker {
    private const val HISTORY_SIZE = 20
    private const val DISPLAY_UPDATE_MS = 2500L
    private const val SPIKE_THRESHOLD_MS = 100

    /** The ping value shown on the HUD (updates every 2.5s or on spikes). */
    var displayPing: Int = 0
        private set

    /** The most recent raw ping measurement. */
    var ping: Int = 0
        private set

    /** Rolling average, updated on every result. */
    var averagePing: Int = 0
        private set

    private val pingHistory = ArrayDeque<Int>(HISTORY_SIZE)
    private var lastDisplayUpdateTime: Long = 0

    /**
     * Send a ping packet. Called periodically from the tick handler.
     * Skips if F3 network charts are already sending pings.
     */
    fun sendPing() {
        val client = MinecraftClient.getInstance()
        if (client.debugHud.shouldShowPacketSizeAndPingCharts()) return
        client.networkHandler?.sendPacket(QueryPingC2SPacket(Util.getMeasuringTimeMs()))
    }

    /**
     * Called from mixin when PingResultS2CPacket is received.
     */
    fun onPingResult(startTime: Long) {
        val now = Util.getMeasuringTimeMs()
        ping = (now - startTime).toInt()

        // Always update average
        if (pingHistory.size >= HISTORY_SIZE) pingHistory.removeFirst()
        pingHistory.addLast(ping)
        averagePing = pingHistory.average().toInt()

        // Update display value on timer or if spike detected
        val timeSinceUpdate = now - lastDisplayUpdateTime
        val isSpike = averagePing > 0 && (ping - averagePing) >= SPIKE_THRESHOLD_MS
        if (timeSinceUpdate >= DISPLAY_UPDATE_MS || isSpike || displayPing == 0) {
            displayPing = ping
            lastDisplayUpdateTime = now
        }
    }

    fun reset() {
        ping = 0
        displayPing = 0
        averagePing = 0
        pingHistory.clear()
        lastDisplayUpdateTime = 0
    }
}
