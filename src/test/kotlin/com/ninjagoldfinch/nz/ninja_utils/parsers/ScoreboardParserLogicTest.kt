package com.ninjagoldfinch.nz.ninja_utils.parsers

import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.testutil.TestData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests scoreboard line parsing logic extracted into [ScoreboardParser.parseLines].
 *
 * Each @Nested group represents a category of scoreboard data (currency, location,
 * slayer, garden, dates). Tests use [TestData.Scoreboard] for individual lines
 * and [TestData.ScoreboardScenarios] for full scoreboard simulations.
 *
 * No mocking needed — [parseLines] is a pure function that takes lines + island context.
 *
 * To test a new scoreboard field:
 * 1. Add test data to [TestData.Scoreboard]
 * 2. Add a @Nested group or test method here
 * 3. Add the regex pattern to [RegexPatterns] if needed (with its own test)
 */
class ScoreboardParserLogicTest {

    @Nested
    inner class CurrencyParsing {
        @Test
        fun `parses simple purse`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.PURSE_SIMPLE), null)
            assertEquals(500L, result.purse)
        }

        @Test
        fun `parses purse with commas`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.PURSE_COMMAS), null)
            assertEquals(1_234_567L, result.purse)
        }

        @Test
        fun `parses piggy bank as purse`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.PIGGY_BANK), null)
            assertEquals(10_000L, result.purse)
        }

        @Test
        fun `parses bits`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.BITS_COMMAS), null)
            assertEquals(12_345, result.bits)
        }

        @Test
        fun `no currency lines returns null`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.IRRELEVANT_SEPARATOR), null)
            assertNull(result.purse)
            assertNull(result.bits)
        }
    }

    @Nested
    inner class LocationParsing {
        @Test
        fun `parses park location`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.LOCATION_PARK), null)
            assertEquals("The Park", result.location)
        }

        @Test
        fun `parses garden location`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.LOCATION_GARDEN), null)
            assertEquals("Garden", result.location)
        }

        @Test
        fun `parses plot location`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.LOCATION_PLOT), null)
            assertEquals("Plot - 3", result.location)
        }

        @Test
        fun `no location line returns null`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.PURSE_SIMPLE), null)
            assertNull(result.location)
        }
    }

    @Nested
    inner class SlayerSection {
        @Test
        fun `detects slayer quest name`() {
            val lines = listOf(
                TestData.Scoreboard.SLAYER_QUEST_HEADER,
                TestData.Scoreboard.SLAYER_TARANTULA_III
            )
            val result = ScoreboardParser.parseLines(lines, null)
            assertEquals("Tarantula Broodfather III", result.slayerQuest)
        }

        @Test
        fun `detects boss spawned from Slay the boss line`() {
            val lines = listOf(
                TestData.Scoreboard.SLAYER_QUEST_HEADER,
                TestData.Scoreboard.SLAYER_REVENANT_IV,
                TestData.Scoreboard.SLAYER_SLAY_BOSS
            )
            val result = ScoreboardParser.parseLines(lines, null)
            assertTrue(result.slayerBossSpawned)
        }

        @Test
        fun `no slay the boss means boss not spawned`() {
            val lines = listOf(
                TestData.Scoreboard.SLAYER_QUEST_HEADER,
                TestData.Scoreboard.SLAYER_REVENANT_IV
            )
            val result = ScoreboardParser.parseLines(lines, null)
            assertFalse(result.slayerBossSpawned)
        }

        @Test
        fun `non-matching line ends slayer section`() {
            val lines = listOf(
                TestData.Scoreboard.SLAYER_QUEST_HEADER,
                TestData.Scoreboard.SLAYER_REVENANT_IV,
                TestData.Scoreboard.PURSE_SIMPLE, // ends slayer section
                TestData.Scoreboard.SLAYER_SLAY_BOSS // this is now outside slayer section
            )
            val result = ScoreboardParser.parseLines(lines, null)
            assertFalse(result.slayerBossSpawned)
            assertEquals(500L, result.purse)
        }

        @Test
        fun `no slayer section means null quest`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.PURSE_SIMPLE), null)
            assertNull(result.slayerQuest)
            assertFalse(result.slayerBossSpawned)
        }
    }

    @Nested
    inner class GardenSpecific {
        @Test
        fun `parses copper when on garden island`() {
            val result = ScoreboardParser.parseLines(
                listOf(TestData.Scoreboard.COPPER_COMMAS),
                SkyBlockIsland.GARDEN
            )
            assertEquals(1_234, result.copper)
        }

        @Test
        fun `ignores copper when not on garden`() {
            val result = ScoreboardParser.parseLines(
                listOf(TestData.Scoreboard.COPPER_COMMAS),
                SkyBlockIsland.HUB
            )
            assertNull(result.copper)
        }

        @Test
        fun `parses sowdust on garden`() {
            val result = ScoreboardParser.parseLines(
                listOf(TestData.Scoreboard.SOWDUST_COMMAS),
                SkyBlockIsland.GARDEN
            )
            assertEquals(5_678, result.sowdust)
        }

        @Test
        fun `ignores sowdust when not on garden`() {
            val result = ScoreboardParser.parseLines(
                listOf(TestData.Scoreboard.SOWDUST_COMMAS),
                null
            )
            assertNull(result.sowdust)
        }

        @Test
        fun `parses pest count`() {
            val result = ScoreboardParser.parseLines(
                listOf(TestData.Scoreboard.PESTS_MULTIPLE),
                SkyBlockIsland.GARDEN
            )
            assertEquals(3, result.pests)
        }

        @Test
        fun `MAX PESTS sets pest count to 8`() {
            val result = ScoreboardParser.parseLines(
                listOf(TestData.Scoreboard.PESTS_MAX),
                SkyBlockIsland.GARDEN
            )
            assertEquals(8, result.pests)
        }
    }

    @Nested
    inner class DateParsing {
        @Test
        fun `parses simple date`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.DATE_SIMPLE), null)
            assertEquals("Spring 5", result.sbDate)
        }

        @Test
        fun `parses early date`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.DATE_EARLY), null)
            assertEquals("Early Spring 1", result.sbDate)
        }

        @Test
        fun `parses late date`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.DATE_LATE), null)
            assertEquals("Late Winter 22", result.sbDate)
        }
    }

    @Nested
    inner class ObjectiveParsing {
        @Test
        fun `parses objective line`() {
            val result = ScoreboardParser.parseLines(listOf(TestData.Scoreboard.OBJECTIVE), null)
            assertEquals("Talk to the Farmer", result.objective)
        }
    }

    @Nested
    inner class FullScenarios {
        @Test
        fun `parses complete garden scoreboard with pests`() {
            val result = ScoreboardParser.parseLines(
                TestData.ScoreboardScenarios.GARDEN_WITH_PESTS,
                SkyBlockIsland.GARDEN
            )
            assertEquals(1_234_567L, result.purse)
            assertEquals("Plot - 3", result.location)
            assertEquals(1_234, result.copper)
            assertEquals(2, result.pests)
            assertEquals("Early Spring 1", result.sbDate)
        }

        @Test
        fun `parses complete slayer scoreboard`() {
            val result = ScoreboardParser.parseLines(
                TestData.ScoreboardScenarios.SLAYER_ACTIVE,
                SkyBlockIsland.SPIDERS_DEN
            )
            assertEquals(500L, result.purse)
            assertEquals("Spider's Den", result.location)
            assertEquals("Tarantula Broodfather III", result.slayerQuest)
            assertTrue(result.slayerBossSpawned)
        }

        @Test
        fun `parses hub scoreboard`() {
            val result = ScoreboardParser.parseLines(
                TestData.ScoreboardScenarios.HUB_BASIC,
                SkyBlockIsland.HUB
            )
            assertEquals(10_000L, result.purse)
            assertEquals("Village", result.location)
            assertEquals(500, result.bits)
            assertEquals("Summer 2", result.sbDate)
        }

        @Test
        fun `garden scoreboard without pests returns null pests`() {
            val result = ScoreboardParser.parseLines(
                TestData.ScoreboardScenarios.GARDEN_NO_PESTS,
                SkyBlockIsland.GARDEN
            )
            assertNull(result.pests)
            assertEquals(100, result.copper)
            assertEquals(5_678, result.sowdust)
        }
    }
}
