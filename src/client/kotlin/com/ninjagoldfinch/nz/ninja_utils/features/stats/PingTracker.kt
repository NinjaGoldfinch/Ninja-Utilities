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
    var ping: Int = 0
        private set

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
        ping = (Util.getMeasuringTimeMs() - startTime).toInt()
    }

    fun reset() {
        ping = 0
    }
}
