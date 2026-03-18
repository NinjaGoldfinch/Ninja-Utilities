package com.ninjagoldfinch.nz.ninja_utils.logging

/**
 * Tracks tick processing time and memory usage for the mod's systems.
 * Uses a rolling window to compute averages.
 */
object PerformanceMonitor {
    private const val WINDOW_SIZE = 100 // ticks (~5 seconds)

    // Tick timing
    private val tickTimesNs = LongArray(WINDOW_SIZE)
    private var tickIndex = 0
    private var tickCount = 0
    private var currentTickStartNs = 0L

    // Render timing
    private val renderTimesNs = LongArray(WINDOW_SIZE)
    private var renderIndex = 0
    private var renderCount = 0

    fun beginTick() {
        currentTickStartNs = System.nanoTime()
    }

    fun endTick() {
        if (currentTickStartNs > 0) {
            tickTimesNs[tickIndex] = System.nanoTime() - currentTickStartNs
            tickIndex = (tickIndex + 1) % WINDOW_SIZE
            if (tickCount < WINDOW_SIZE) tickCount++
            currentTickStartNs = 0
        }
    }

    fun recordRenderTime(nanos: Long) {
        renderTimesNs[renderIndex] = nanos
        renderIndex = (renderIndex + 1) % WINDOW_SIZE
        if (renderCount < WINDOW_SIZE) renderCount++
    }

    /** Average tick processing time in microseconds. */
    val avgTickTimeUs: Double
        get() {
            if (tickCount == 0) return 0.0
            var sum = 0L
            for (i in 0 until tickCount) sum += tickTimesNs[i]
            return sum.toDouble() / tickCount / 1000.0
        }

    /** Max tick processing time in microseconds over the window. */
    val maxTickTimeUs: Double
        get() {
            if (tickCount == 0) return 0.0
            var max = 0L
            for (i in 0 until tickCount) if (tickTimesNs[i] > max) max = tickTimesNs[i]
            return max.toDouble() / 1000.0
        }

    /** Average HUD render time in microseconds. */
    val avgRenderTimeUs: Double
        get() {
            if (renderCount == 0) return 0.0
            var sum = 0L
            for (i in 0 until renderCount) sum += renderTimesNs[i]
            return sum.toDouble() / renderCount / 1000.0
        }

    /** JVM heap memory used in MB. */
    val usedMemoryMb: Long
        get() {
            val runtime = Runtime.getRuntime()
            return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        }

    /** JVM max heap memory in MB. */
    val maxMemoryMb: Long
        get() = Runtime.getRuntime().maxMemory() / (1024 * 1024)

    /** Memory usage as a percentage. */
    val memoryPercent: Int
        get() {
            val max = Runtime.getRuntime().maxMemory()
            if (max == 0L) return 0
            val used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            return (used * 100 / max).toInt()
        }

    fun reset() {
        tickIndex = 0
        tickCount = 0
        renderIndex = 0
        renderCount = 0
        currentTickStartNs = 0
    }
}
