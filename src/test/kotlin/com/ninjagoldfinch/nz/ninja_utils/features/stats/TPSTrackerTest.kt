package com.ninjagoldfinch.nz.ninja_utils.features.stats

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests TPS estimation from packet counting.
 *
 * The tracker estimates server TPS by counting packets per second.
 * Tests simulate packet/tick sequences and verify the resulting TPS value.
 *
 * Key behaviors tested:
 * - Stabilization period (first seconds ignored after join)
 * - Rolling average calculation
 * - Boundary clamping (0-20 TPS)
 *
 * To simulate N seconds at X TPS: call [simulateSeconds].
 */
class TPSTrackerTest {

    @BeforeEach
    fun reset() {
        TPSTracker.reset()
    }

    /**
     * Simulates [seconds] seconds of gameplay at [tps] TPS.
     * Each second: triggers [tps] packet-received flags across 20 client ticks.
     */
    private fun simulateSeconds(tps: Int, seconds: Int) {
        for (s in 0 until seconds) {
            // Distribute tps packets across 20 ticks
            for (tick in 0 until 20) {
                if (tick < tps) {
                    TPSTracker.onPacketReceived()
                }
                TPSTracker.onClientTick()
            }
        }
    }

    @Nested
    inner class StabilizationPeriod {
        @Test
        fun `returns null before stabilization completes`() {
            // Only 3 seconds — within the 5-second ignore window
            simulateSeconds(20, 3)
            assertNull(TPSTracker.tps)
        }

        @Test
        fun `returns null during ignore period plus first discard`() {
            // 6 seconds = 5 ignored + 1 discarded, still no data
            simulateSeconds(20, 6)
            assertNull(TPSTracker.tps)
        }

        @Test
        fun `returns value after stabilization and first discard`() {
            // 7 seconds = 5 ignored + 1 discarded + 1 real sample
            simulateSeconds(20, 7)
            assertTrue(TPSTracker.tps != null)
        }
    }

    @Nested
    inner class TPSCalculation {
        @Test
        fun `full TPS produces value near 20`() {
            simulateSeconds(20, 17) // 5 ignored + 1 discarded + 11 samples (fills buffer + 1)
            val tps = TPSTracker.tps!!
            assertEquals(20.0, tps, 0.1)
        }

        @Test
        fun `half TPS produces value near 10`() {
            simulateSeconds(10, 17)
            val tps = TPSTracker.tps!!
            assertEquals(10.0, tps, 0.1)
        }

        @Test
        fun `zero packets produces 0 TPS`() {
            simulateSeconds(0, 17)
            val tps = TPSTracker.tps!!
            assertEquals(0.0, tps, 0.1)
        }
    }

    @Nested
    inner class BoundaryClamping {
        @Test
        fun `TPS is clamped to maximum of 20`() {
            // Even if somehow more packets come in, TPS shouldn't exceed 20
            // Simulate by sending more than 20 packets per second
            for (s in 0 until 17) {
                for (tick in 0 until 20) {
                    // Send multiple packets per tick
                    TPSTracker.onPacketReceived()
                    TPSTracker.onPacketReceived()
                    TPSTracker.onClientTick()
                }
            }
            val tps = TPSTracker.tps
            // Note: onPacketReceived sets a flag, not a counter — so multiple calls
            // before a tick only count as 1. Result is still capped at 20.
            assertTrue(tps != null && tps <= 20.0)
        }

        @Test
        fun `TPS is at least 0`() {
            simulateSeconds(0, 17)
            val tps = TPSTracker.tps!!
            assertTrue(tps >= 0.0)
        }
    }

    @Nested
    inner class ResetBehavior {
        @Test
        fun `reset clears all state`() {
            simulateSeconds(20, 10)
            TPSTracker.reset()
            assertNull(TPSTracker.tps)
        }
    }
}
