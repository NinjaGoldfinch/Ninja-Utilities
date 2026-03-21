package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.core.CoinChangeEvent
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.RareDropEvent
import com.ninjagoldfinch.nz.ninja_utils.core.SkillXpGainEvent
import com.ninjagoldfinch.nz.ninja_utils.testutil.TestData
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests chat message parsing and handler dispatch.
 *
 * Tests verify the end-to-end flow: raw message string -> correct event posted.
 * Uses [EventBus] as the observable output (subscribe, check received events).
 * This tests the real handler logic, not mocked handlers.
 *
 * To test a new chat handler:
 * 1. Add example messages to [TestData.Chat]
 * 2. Add a test that sends the message and asserts the correct event is posted
 */
class ChatParserTest {

    @BeforeEach
    fun setup() {
        ChatParser.reset()
        ChatParser.registerDefaultHandlers()
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    @Nested
    inner class SkillXpHandling {
        @Test
        fun `skill xp message posts SkillXpGainEvent with parsed values`() {
            val received = mutableListOf<SkillXpGainEvent>()
            EventBus.subscribe<SkillXpGainEvent> { received.add(it) }

            ChatParser.processMessage(TestData.Chat.SKILL_XP_MINING)

            assertEquals(1, received.size)
            assertEquals("Mining", received[0].skill)
            assertEquals(1234.5, received[0].xpGain)
            assertEquals(50_000L, received[0].current)
            assertEquals(100_000L, received[0].required)
        }

        @Test
        fun `farming xp message parses correctly`() {
            val received = mutableListOf<SkillXpGainEvent>()
            EventBus.subscribe<SkillXpGainEvent> { received.add(it) }

            ChatParser.processMessage(TestData.Chat.SKILL_XP_FARMING)

            assertEquals(1, received.size)
            assertEquals("Farming", received[0].skill)
            assertEquals(500.0, received[0].xpGain)
        }
    }

    @Nested
    inner class CoinChangeHandling {
        @Test
        fun `coin gain posts CoinChangeEvent`() {
            val received = mutableListOf<CoinChangeEvent>()
            EventBus.subscribe<CoinChangeEvent> { received.add(it) }

            ChatParser.processMessage(TestData.Chat.COIN_GAIN)

            assertEquals(1, received.size)
            assertEquals(500L, received[0].amount)
            assertEquals("Auction", received[0].source)
        }

        @Test
        fun `coin loss posts negative amount`() {
            val received = mutableListOf<CoinChangeEvent>()
            EventBus.subscribe<CoinChangeEvent> { received.add(it) }

            ChatParser.processMessage(TestData.Chat.COIN_LOSS)

            assertEquals(1, received.size)
            assertEquals(-1_000L, received[0].amount)
        }
    }

    @Nested
    inner class RareDropHandling {
        @Test
        fun `rare drop message posts RareDropEvent`() {
            val received = mutableListOf<RareDropEvent>()
            EventBus.subscribe<RareDropEvent> { received.add(it) }

            ChatParser.processMessage(TestData.Chat.RARE_DROP)

            assertEquals(1, received.size)
            assertEquals("Enchanted Diamond", received[0].itemName)
        }

        @Test
        fun `very rare drop parses item name`() {
            val received = mutableListOf<RareDropEvent>()
            EventBus.subscribe<RareDropEvent> { received.add(it) }

            ChatParser.processMessage(TestData.Chat.VERY_RARE_DROP)

            assertEquals(1, received.size)
            assertEquals("Enchanted Emerald Block", received[0].itemName)
        }
    }

    @Nested
    inner class SuppressionBehavior {
        @Test
        fun `suppressParsing runs block and restores state`() {
            var blockRan = false
            val result = ChatParser.suppressParsing {
                blockRan = true
                42
            }
            assertTrue(blockRan)
            assertEquals(42, result)
        }

        @Test
        fun `suppressParsing restores state even on exception`() {
            try {
                ChatParser.suppressParsing { throw RuntimeException("test") }
            } catch (_: RuntimeException) { }
            // After exception, suppression should be cleared
            // Verify by checking that processing still works
            val received = mutableListOf<SkillXpGainEvent>()
            EventBus.subscribe<SkillXpGainEvent> { received.add(it) }
            ChatParser.processMessage(TestData.Chat.SKILL_XP_MINING)
            assertEquals(1, received.size)
        }
    }

    @Nested
    inner class UnmatchedMessages {
        @Test
        fun `unmatched message does not post any event`() {
            var eventPosted = false
            EventBus.subscribe<SkillXpGainEvent> { eventPosted = true }
            EventBus.subscribe<CoinChangeEvent> { eventPosted = true }
            EventBus.subscribe<RareDropEvent> { eventPosted = true }

            ChatParser.processMessage("Just a regular chat message")

            assertTrue(!eventPosted)
        }
    }

    @Nested
    inner class ResetBehavior {
        @Test
        fun `reset clears handlers so no messages match`() {
            val received = mutableListOf<SkillXpGainEvent>()
            EventBus.subscribe<SkillXpGainEvent> { received.add(it) }

            ChatParser.reset()
            ChatParser.processMessage(TestData.Chat.SKILL_XP_MINING)

            assertTrue(received.isEmpty())
        }
    }
}
