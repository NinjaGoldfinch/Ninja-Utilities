package com.ninjagoldfinch.nz.ninja_utils.features.stats

/**
 * Estimates server TPS using the same approach as SkyHanni:
 * - Any incoming packet sets a flag
 * - Each client tick, if the flag is set, increment a per-second counter
 * - Every second, save the counter to a rolling 10-second average
 *
 * This works because at 20 TPS, the server sends game-state packets each tick.
 * When the server lags, fewer ticks produce packets → counter drops → TPS drops.
 */
object TPSTracker {
    private const val MAX_SAMPLES = 10
    private const val IGNORE_SECONDS_AFTER_JOIN = 5

    @Volatile
    private var hasReceivedPacket = false

    private var packetsThisSecond = 0
    private var ticksInCurrentSecond = 0
    private val tpsSamples = ArrayDeque<Int>()
    private var ignoredSeconds = 0
    private var hasDiscardedFirstSecond = false

    /** Called from mixin on every incoming packet. */
    fun onPacketReceived() {
        hasReceivedPacket = true
    }

    /** Called every client tick (20 times/second). */
    fun onClientTick() {
        if (hasReceivedPacket) {
            hasReceivedPacket = false
            packetsThisSecond++
        }

        ticksInCurrentSecond++
        if (ticksInCurrentSecond >= 20) {
            ticksInCurrentSecond = 0
            onSecondPassed()
        }
    }

    private fun onSecondPassed() {
        val count = packetsThisSecond
        packetsThisSecond = 0

        // Ignore the first few seconds after joining to let things stabilize
        if (ignoredSeconds < IGNORE_SECONDS_AFTER_JOIN) {
            ignoredSeconds++
            return
        }

        // Discard the first real second (likely partial)
        if (!hasDiscardedFirstSecond) {
            hasDiscardedFirstSecond = true
            return
        }

        tpsSamples.addLast(count)
        while (tpsSamples.size > MAX_SAMPLES) {
            tpsSamples.removeFirst()
        }
    }

    /** Estimated TPS as rolling average. Null if insufficient data. */
    val tps: Double?
        get() {
            if (tpsSamples.isEmpty()) return null
            return tpsSamples.average().coerceIn(0.0, 20.0)
        }

    fun reset() {
        hasReceivedPacket = false
        packetsThisSecond = 0
        ticksInCurrentSecond = 0
        tpsSamples.clear()
        ignoredSeconds = 0
        hasDiscardedFirstSecond = false
    }
}
