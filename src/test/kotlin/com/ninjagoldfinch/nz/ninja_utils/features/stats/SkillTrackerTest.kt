package com.ninjagoldfinch.nz.ninja_utils.features.stats

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests XP rate calculation with rolling time window.
 *
 * Key behaviors:
 * - Records XP gains and calculates XP/minute rate
 * - Prunes entries older than 60 seconds
 * - Handles single vs multiple entries differently for rate calculation
 *
 * Time is controlled via [SkillTracker.timeProvider] to test window expiry
 * without real delays.
 */
class SkillTrackerTest {

    private var mockTime = 100_000L

    @BeforeEach
    fun setup() {
        SkillTracker.reset()
        SkillTracker.timeProvider = { mockTime }
    }

    @AfterEach
    fun cleanup() {
        SkillTracker.timeProvider = { System.currentTimeMillis() }
    }

    private fun advanceTime(ms: Long) {
        mockTime += ms
    }

    @Nested
    inner class BasicRecording {
        @Test
        fun `returns null rate for untracked skill`() {
            assertNull(SkillTracker.getRate("Mining"))
        }

        @Test
        fun `records xp and returns rate`() {
            SkillTracker.recordXpGain("Mining", 100.0)
            val rate = SkillTracker.getRate("Mining")
            assertEquals("Mining", rate?.skill)
            assertEquals(100.0, rate?.totalGained)
            assertEquals(1, rate?.entryCount)
        }
    }

    @Nested
    inner class RateCalculation {
        @Test
        fun `single entry uses full window for rate calculation`() {
            SkillTracker.recordXpGain("Mining", 1000.0)
            val rate = SkillTracker.getRate("Mining")!!
            // Single entry: rate = totalGained / RATE_WINDOW_MS * 60000
            // = 1000 / 60000 * 60000 = 1000 XP/min
            assertEquals(1000.0, rate.xpPerMinute, 0.1)
        }

        @Test
        fun `multiple entries calculate rate from time span`() {
            SkillTracker.recordXpGain("Mining", 500.0)
            advanceTime(30_000) // 30 seconds later
            SkillTracker.recordXpGain("Mining", 500.0)

            val rate = SkillTracker.getRate("Mining")!!
            assertEquals(1000.0, rate.totalGained)
            // Rate = 1000 / 30000 * 60000 = 2000 XP/min
            assertEquals(2000.0, rate.xpPerMinute, 0.1)
        }

        @Test
        fun `tracks multiple skills independently`() {
            SkillTracker.recordXpGain("Mining", 100.0)
            SkillTracker.recordXpGain("Farming", 200.0)

            val miningRate = SkillTracker.getRate("Mining")!!
            val farmingRate = SkillTracker.getRate("Farming")!!
            assertEquals(100.0, miningRate.totalGained)
            assertEquals(200.0, farmingRate.totalGained)
        }
    }

    @Nested
    inner class WindowPruning {
        @Test
        fun `prunes entries older than 60 seconds`() {
            SkillTracker.recordXpGain("Mining", 100.0)
            advanceTime(61_000) // 61 seconds later
            SkillTracker.recordXpGain("Mining", 200.0)

            val rate = SkillTracker.getRate("Mining")!!
            assertEquals(200.0, rate.totalGained)
            assertEquals(1, rate.entryCount)
        }

        @Test
        fun `getRate prunes old entries and returns null when all expired`() {
            SkillTracker.recordXpGain("Mining", 100.0)
            advanceTime(61_000)
            assertNull(SkillTracker.getRate("Mining"))
        }
    }

    @Nested
    inner class AllRates {
        @Test
        fun `getAllRates returns all tracked skills`() {
            SkillTracker.recordXpGain("Mining", 100.0)
            SkillTracker.recordXpGain("Farming", 200.0)

            val rates = SkillTracker.getAllRates()
            assertEquals(2, rates.size)
            assertTrue(rates.containsKey("Mining"))
            assertTrue(rates.containsKey("Farming"))
        }
    }

    @Nested
    inner class ResetBehavior {
        @Test
        fun `reset clears all tracked data`() {
            SkillTracker.recordXpGain("Mining", 100.0)
            SkillTracker.reset()
            assertNull(SkillTracker.getRate("Mining"))
            assertTrue(SkillTracker.getAllRates().isEmpty())
        }
    }
}
