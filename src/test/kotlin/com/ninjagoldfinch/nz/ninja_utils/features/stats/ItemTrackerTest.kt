package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainEvent
import com.ninjagoldfinch.nz.ninja_utils.core.ItemGainSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests item gain tracking with rolling time window.
 *
 * Key behaviors:
 * - Records item gains and calculates items/minute rate
 * - Prunes entries older than 60 seconds
 * - Handles single vs multiple entries differently for rate calculation
 * - Tracks multiple items independently
 *
 * Time is controlled via [ItemTracker.timeProvider] to test window expiry
 * without real delays.
 */
class ItemTrackerTest {

    private var mockTime = 100_000L

    @BeforeEach
    fun setup() {
        ItemTracker.reset()
        ItemTracker.timeProvider = { mockTime }
    }

    @AfterEach
    fun cleanup() {
        ItemTracker.timeProvider = { System.currentTimeMillis() }
    }

    private fun advanceTime(ms: Long) {
        mockTime += ms
    }

    private fun gainEvent(
        itemId: String = "ENCHANTED_DIAMOND",
        displayName: String = "Enchanted Diamond",
        amount: Int = 1,
        source: ItemGainSource = ItemGainSource.INVENTORY
    ) = ItemGainEvent(itemId, displayName, amount, source)

    @Nested
    inner class BasicRecording {
        @Test
        fun `returns null rate for untracked item`() {
            assertNull(ItemTracker.getRate("ENCHANTED_DIAMOND"))
        }

        @Test
        fun `records gain and returns rate`() {
            ItemTracker.recordGain(gainEvent(amount = 64))
            val rate = ItemTracker.getRate("ENCHANTED_DIAMOND")
            assertEquals("ENCHANTED_DIAMOND", rate?.itemId)
            assertEquals("Enchanted Diamond", rate?.displayName)
            assertEquals(64, rate?.totalGained)
            assertEquals(1, rate?.entryCount)
        }
    }

    @Nested
    inner class RateCalculation {
        @Test
        fun `single entry uses full window for rate calculation`() {
            ItemTracker.recordGain(gainEvent(amount = 60))
            val rate = ItemTracker.getRate("ENCHANTED_DIAMOND")!!
            // Single entry: rate = 60 / 60000 * 60000 = 60 items/min
            assertEquals(60.0, rate.perMinute, 0.1)
        }

        @Test
        fun `multiple entries calculate rate from time span`() {
            ItemTracker.recordGain(gainEvent(amount = 30))
            advanceTime(30_000)
            ItemTracker.recordGain(gainEvent(amount = 30))

            val rate = ItemTracker.getRate("ENCHANTED_DIAMOND")!!
            assertEquals(60, rate.totalGained)
            // Rate = 60 / 30000 * 60000 = 120 items/min
            assertEquals(120.0, rate.perMinute, 0.1)
        }

        @Test
        fun `tracks multiple items independently`() {
            ItemTracker.recordGain(gainEvent(itemId = "ENCHANTED_DIAMOND", displayName = "Enchanted Diamond", amount = 64))
            ItemTracker.recordGain(gainEvent(itemId = "WHEAT", displayName = "Wheat", amount = 128))

            val diamondRate = ItemTracker.getRate("ENCHANTED_DIAMOND")!!
            val wheatRate = ItemTracker.getRate("WHEAT")!!
            assertEquals(64, diamondRate.totalGained)
            assertEquals(128, wheatRate.totalGained)
        }
    }

    @Nested
    inner class WindowPruning {
        @Test
        fun `prunes entries older than 60 seconds`() {
            ItemTracker.recordGain(gainEvent(amount = 32))
            advanceTime(61_000)
            ItemTracker.recordGain(gainEvent(amount = 64))

            val rate = ItemTracker.getRate("ENCHANTED_DIAMOND")!!
            assertEquals(64, rate.totalGained)
            assertEquals(1, rate.entryCount)
        }

        @Test
        fun `getRate prunes old entries and returns null when all expired`() {
            ItemTracker.recordGain(gainEvent(amount = 32))
            advanceTime(61_000)
            assertNull(ItemTracker.getRate("ENCHANTED_DIAMOND"))
        }
    }

    @Nested
    inner class AllRates {
        @Test
        fun `getAllRates returns all tracked items`() {
            ItemTracker.recordGain(gainEvent(itemId = "ENCHANTED_DIAMOND", displayName = "Enchanted Diamond", amount = 64))
            ItemTracker.recordGain(gainEvent(itemId = "WHEAT", displayName = "Wheat", amount = 128))

            val rates = ItemTracker.getAllRates()
            assertEquals(2, rates.size)
            assertTrue(rates.containsKey("ENCHANTED_DIAMOND"))
            assertTrue(rates.containsKey("WHEAT"))
        }
    }

    @Nested
    inner class RecentGains {
        @Test
        fun `getRecentGains returns entries in reverse chronological order`() {
            ItemTracker.recordGain(gainEvent(itemId = "WHEAT", displayName = "Wheat", amount = 10))
            advanceTime(1_000)
            ItemTracker.recordGain(gainEvent(itemId = "ENCHANTED_DIAMOND", displayName = "Enchanted Diamond", amount = 64))

            val recent = ItemTracker.getRecentGains(10)
            assertEquals(2, recent.size)
            assertEquals("ENCHANTED_DIAMOND", recent[0].itemId)
            assertEquals("WHEAT", recent[1].itemId)
        }

        @Test
        fun `getRecentGains respects limit`() {
            repeat(5) {
                ItemTracker.recordGain(gainEvent(itemId = "ITEM_$it", displayName = "Item $it", amount = 1))
                advanceTime(100)
            }

            val recent = ItemTracker.getRecentGains(3)
            assertEquals(3, recent.size)
        }
    }

    @Nested
    inner class SourceTracking {
        @Test
        fun `tracks source correctly`() {
            ItemTracker.recordGain(gainEvent(source = ItemGainSource.SACK))
            val recent = ItemTracker.getRecentGains(1)
            assertEquals(ItemGainSource.SACK, recent[0].source)
        }
    }

    @Nested
    inner class ResetBehavior {
        @Test
        fun `reset clears all tracked data`() {
            ItemTracker.recordGain(gainEvent(amount = 64))
            ItemTracker.reset()
            assertNull(ItemTracker.getRate("ENCHANTED_DIAMOND"))
            assertTrue(ItemTracker.getAllRates().isEmpty())
            assertTrue(ItemTracker.getRecentGains().isEmpty())
        }
    }
}
