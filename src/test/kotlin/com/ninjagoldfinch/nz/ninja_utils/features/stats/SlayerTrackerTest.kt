package com.ninjagoldfinch.nz.ninja_utils.features.stats

import com.ninjagoldfinch.nz.ninja_utils.config.SkyblockCategory
import com.ninjagoldfinch.nz.ninja_utils.util.ChatUtils
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests slayer quest state machine transitions.
 *
 * The tracker moves through states: idle -> quest started -> boss spawned -> boss slain.
 * Tests verify state transitions and observable outputs (completion count, timing).
 *
 * ChatUtils is mocked to prevent Minecraft chat calls.
 * Time is controlled via [SlayerTracker.timeProvider].
 * SkyblockCategory config fields are set directly (they're @JvmField vars).
 */
class SlayerTrackerTest {

    private var mockTime = 100_000L

    @BeforeEach
    fun setup() {
        SlayerTracker.reset()
        SlayerTracker.timeProvider = { mockTime }
        mockkObject(ChatUtils)
        every { ChatUtils.sendModMessage(any()) } returns Unit
        // Enable tracker for tests
        SkyblockCategory.slayerTracker = true
        SkyblockCategory.slayerBossAlert = false
    }

    @AfterEach
    fun cleanup() {
        SkyblockCategory.slayerTracker = false
        SlayerTracker.timeProvider = { System.currentTimeMillis() }
        unmockkAll()
    }

    private fun advanceTime(ms: Long) {
        mockTime += ms
    }

    @Nested
    inner class QuestLifecycle {
        @Test
        fun `quest started sets start time`() {
            SlayerTracker.onQuestStarted()
            assertEquals(mockTime, SlayerTracker.questStartTime)
            assertFalse(SlayerTracker.bossSpawned)
        }

        @Test
        fun `boss spawned sets spawn time and flag`() {
            SlayerTracker.onQuestStarted()
            advanceTime(5_000)
            SlayerTracker.onBossSpawned()

            assertTrue(SlayerTracker.bossSpawned)
            assertEquals(mockTime, SlayerTracker.bossSpawnTime)
        }

        @Test
        fun `full quest lifecycle increments session counter`() {
            assertEquals(0, SlayerTracker.completionsThisSession)

            SlayerTracker.onQuestStarted()
            advanceTime(5_000)
            SlayerTracker.onBossSpawned()
            advanceTime(10_000)
            SlayerTracker.onBossSlain()

            assertEquals(1, SlayerTracker.completionsThisSession)
            assertFalse(SlayerTracker.bossSpawned)
        }

        @Test
        fun `boss slain records completion time`() {
            SlayerTracker.onQuestStarted()
            SlayerTracker.onBossSpawned()
            advanceTime(15_000)
            SlayerTracker.onBossSlain()

            assertEquals(mockTime, SlayerTracker.lastCompletionTime)
        }

        @Test
        fun `multiple completions increment counter`() {
            repeat(3) {
                SlayerTracker.onQuestStarted()
                SlayerTracker.onBossSpawned()
                advanceTime(5_000)
                SlayerTracker.onBossSlain()
            }
            assertEquals(3, SlayerTracker.completionsThisSession)
        }
    }

    @Nested
    inner class ScoreboardUpdate {
        @Test
        fun `updateFromScoreboard sets active quest`() {
            SlayerTracker.updateFromScoreboard("Revenant Horror IV")
            assertEquals("Revenant Horror IV", SlayerTracker.activeQuest)
        }

        @Test
        fun `updateFromScoreboard with null clears quest`() {
            SlayerTracker.updateFromScoreboard("Revenant Horror IV")
            SlayerTracker.updateFromScoreboard(null)
            assertNull(SlayerTracker.activeQuest)
        }
    }

    @Nested
    inner class ConfigGating {
        @Test
        fun `quest started is no-op when tracker disabled`() {
            SkyblockCategory.slayerTracker = false
            SlayerTracker.onQuestStarted()
            assertEquals(0, SlayerTracker.questStartTime)
        }
    }

    @Nested
    inner class ResetBehavior {
        @Test
        fun `reset clears all state`() {
            SlayerTracker.onQuestStarted()
            SlayerTracker.onBossSpawned()
            SlayerTracker.onBossSlain()
            SlayerTracker.updateFromScoreboard("Test")

            SlayerTracker.reset()

            assertNull(SlayerTracker.activeQuest)
            assertEquals(0, SlayerTracker.questStartTime)
            assertFalse(SlayerTracker.bossSpawned)
            assertEquals(0, SlayerTracker.completionsThisSession)
        }
    }
}
