package com.ninjagoldfinch.nz.ninja_utils.util

import com.ninjagoldfinch.nz.ninja_utils.testutil.TestData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests all regex patterns in [RegexPatterns] against realistic Hypixel strings.
 *
 * Each pattern gets its own @Nested group. Tests verify:
 * - Pattern matches expected strings and captures correct groups
 * - Pattern does NOT match similar-but-wrong strings (negative cases)
 *
 * Test data comes from [TestData] — update there when Hypixel changes formats.
 * To add tests for a new pattern: add a @Nested inner class following the existing pattern.
 */
class RegexPatternsTest {

    @Nested
    inner class PursePattern {
        @Test
        fun `matches simple purse`() {
            val match = RegexPatterns.PURSE.find(TestData.Scoreboard.PURSE_SIMPLE)
            assertNotNull(match)
            assertEquals("500", match.groupValues[1])
        }

        @Test
        fun `matches purse with commas`() {
            val match = RegexPatterns.PURSE.find(TestData.Scoreboard.PURSE_COMMAS)
            assertNotNull(match)
            assertEquals("1,234,567", match.groupValues[1])
        }

        @Test
        fun `matches piggy bank variant`() {
            val match = RegexPatterns.PURSE.find(TestData.Scoreboard.PIGGY_BANK)
            assertNotNull(match)
            assertEquals("10,000", match.groupValues[1])
        }

        @Test
        fun `does not match bits line`() {
            assertNull(RegexPatterns.PURSE.find(TestData.Scoreboard.BITS_COMMAS))
        }

        @Test
        fun `does not match arbitrary text`() {
            assertNull(RegexPatterns.PURSE.find("Some random text"))
        }
    }

    @Nested
    inner class BitsPattern {
        @Test
        fun `matches simple bits`() {
            val match = RegexPatterns.BITS.find(TestData.Scoreboard.BITS_SIMPLE)
            assertNotNull(match)
            assertEquals("500", match.groupValues[1])
        }

        @Test
        fun `matches bits with commas`() {
            val match = RegexPatterns.BITS.find(TestData.Scoreboard.BITS_COMMAS)
            assertNotNull(match)
            assertEquals("12,345", match.groupValues[1])
        }

        @Test
        fun `does not match purse line`() {
            assertNull(RegexPatterns.BITS.find(TestData.Scoreboard.PURSE_SIMPLE))
        }
    }

    @Nested
    inner class LocationPattern {
        @Test
        fun `matches park location`() {
            val match = RegexPatterns.LOCATION.find(TestData.Scoreboard.LOCATION_PARK)
            assertNotNull(match)
            assertEquals("The Park", match.groupValues[1])
        }

        @Test
        fun `matches garden location`() {
            val match = RegexPatterns.LOCATION.find(TestData.Scoreboard.LOCATION_GARDEN)
            assertNotNull(match)
            assertEquals("Garden", match.groupValues[1])
        }

        @Test
        fun `matches dwarven mines`() {
            val match = RegexPatterns.LOCATION.find(TestData.Scoreboard.LOCATION_DWARVEN)
            assertNotNull(match)
            assertEquals("Dwarven Mines", match.groupValues[1])
        }

        @Test
        fun `matches plot location`() {
            val match = RegexPatterns.LOCATION.find(TestData.Scoreboard.LOCATION_PLOT)
            assertNotNull(match)
            assertEquals("Plot - 3", match.groupValues[1])
        }

        @Test
        fun `does not match plain text`() {
            assertNull(RegexPatterns.LOCATION.find("Dwarven Mines"))
        }
    }

    @Nested
    inner class CopperPattern {
        @Test
        fun `matches simple copper`() {
            val match = RegexPatterns.COPPER.find(TestData.Scoreboard.COPPER_SIMPLE)
            assertNotNull(match)
            assertEquals("500", match.groupValues[1])
        }

        @Test
        fun `matches copper with commas`() {
            val match = RegexPatterns.COPPER.find(TestData.Scoreboard.COPPER_COMMAS)
            assertNotNull(match)
            assertEquals("1,234", match.groupValues[1])
        }
    }

    @Nested
    inner class SowdustPattern {
        @Test
        fun `matches simple sowdust`() {
            val match = RegexPatterns.SOWDUST.find(TestData.Scoreboard.SOWDUST_SIMPLE)
            assertNotNull(match)
            assertEquals("100", match.groupValues[1])
        }

        @Test
        fun `matches sowdust with commas`() {
            val match = RegexPatterns.SOWDUST.find(TestData.Scoreboard.SOWDUST_COMMAS)
            assertNotNull(match)
            assertEquals("5,678", match.groupValues[1])
        }
    }

    @Nested
    inner class PestsPattern {
        @Test
        fun `matches single pest`() {
            val match = RegexPatterns.PESTS.find(TestData.Scoreboard.PESTS_SINGLE)
            assertNotNull(match)
            assertEquals("1", match.groupValues[1])
        }

        @Test
        fun `matches multiple pests`() {
            val match = RegexPatterns.PESTS.find(TestData.Scoreboard.PESTS_MULTIPLE)
            assertNotNull(match)
            assertEquals("3", match.groupValues[1])
        }

        @Test
        fun `max pests matches MAX_PESTS pattern`() {
            assert(RegexPatterns.MAX_PESTS.containsMatchIn(TestData.Scoreboard.PESTS_MAX))
        }

        @Test
        fun `max pests does not match numeric pests`() {
            assert(!RegexPatterns.MAX_PESTS.containsMatchIn(TestData.Scoreboard.PESTS_MULTIPLE))
        }
    }

    @Nested
    inner class GardenPlotPattern {
        @Test
        fun `extracts plot number from location`() {
            val match = RegexPatterns.GARDEN_PLOT.find("Plot - 3")
            assertNotNull(match)
            assertEquals("3", match.groupValues[1])
        }

        @Test
        fun `does not match non-plot location`() {
            assertNull(RegexPatterns.GARDEN_PLOT.find("Garden"))
        }
    }

    @Nested
    inner class SlayerQuestPattern {
        @Test
        fun `matches revenant horror IV`() {
            val match = RegexPatterns.SLAYER_QUEST.find(TestData.Scoreboard.SLAYER_REVENANT_IV)
            assertNotNull(match)
            assertEquals("Revenant Horror", match.groupValues[1])
            assertEquals("IV", match.groupValues[2])
        }

        @Test
        fun `matches tarantula broodfather III`() {
            val match = RegexPatterns.SLAYER_QUEST.find(TestData.Scoreboard.SLAYER_TARANTULA_III)
            assertNotNull(match)
            assertEquals("Tarantula Broodfather", match.groupValues[1])
            assertEquals("III", match.groupValues[2])
        }

        @Test
        fun `matches tier V`() {
            val match = RegexPatterns.SLAYER_QUEST.find("Voidgloom Seraph V")
            assertNotNull(match)
            assertEquals("Voidgloom Seraph", match.groupValues[1])
            assertEquals("V", match.groupValues[2])
        }

        @Test
        fun `matches tier I`() {
            val match = RegexPatterns.SLAYER_QUEST.find("Revenant Horror I")
            assertNotNull(match)
            assertEquals("Revenant Horror", match.groupValues[1])
            assertEquals("I", match.groupValues[2])
        }
    }

    @Nested
    inner class SbDatePattern {
        @Test
        fun `matches simple date`() {
            val match = RegexPatterns.SB_DATE.find(TestData.Scoreboard.DATE_SIMPLE)
            assertNotNull(match)
            assertEquals("", match.groupValues[1])
            assertEquals("Spring", match.groupValues[2])
            assertEquals("5", match.groupValues[3])
        }

        @Test
        fun `matches early date`() {
            val match = RegexPatterns.SB_DATE.find(TestData.Scoreboard.DATE_EARLY)
            assertNotNull(match)
            assertEquals("Early", match.groupValues[1])
            assertEquals("Spring", match.groupValues[2])
            assertEquals("1", match.groupValues[3])
        }

        @Test
        fun `matches late date`() {
            val match = RegexPatterns.SB_DATE.find(TestData.Scoreboard.DATE_LATE)
            assertNotNull(match)
            assertEquals("Late", match.groupValues[1])
            assertEquals("Winter", match.groupValues[2])
            assertEquals("22", match.groupValues[3])
        }

        @Test
        fun `matches 2nd ordinal`() {
            val match = RegexPatterns.SB_DATE.find(TestData.Scoreboard.DATE_SECOND)
            assertNotNull(match)
            assertEquals("2", match.groupValues[3])
        }

        @Test
        fun `matches 3rd ordinal`() {
            val match = RegexPatterns.SB_DATE.find(TestData.Scoreboard.DATE_THIRD)
            assertNotNull(match)
            assertEquals("3", match.groupValues[3])
        }
    }

    @Nested
    inner class ObjectivePattern {
        @Test
        fun `matches objective line`() {
            val match = RegexPatterns.OBJECTIVE.find(TestData.Scoreboard.OBJECTIVE)
            assertNotNull(match)
            assertEquals("Talk to the Farmer", match.groupValues[1])
        }
    }

    @Nested
    inner class SkillXpPattern {
        @Test
        fun `matches mining xp message`() {
            val match = RegexPatterns.SKILL_XP.find(TestData.Chat.SKILL_XP_MINING)
            assertNotNull(match)
            assertEquals("1,234.5", match.groupValues[1])
            assertEquals("Mining", match.groupValues[2])
            assertEquals("50,000", match.groupValues[3])
            assertEquals("100,000", match.groupValues[4])
        }

        @Test
        fun `matches farming xp message`() {
            val match = RegexPatterns.SKILL_XP.find(TestData.Chat.SKILL_XP_FARMING)
            assertNotNull(match)
            assertEquals("Farming", match.groupValues[2])
        }
    }

    @Nested
    inner class CoinChangePattern {
        @Test
        fun `matches coin gain`() {
            val match = RegexPatterns.COIN_CHANGE.find(TestData.Chat.COIN_GAIN)
            assertNotNull(match)
            assertEquals("+500", match.groupValues[1])
            assertEquals("Auction", match.groupValues[2])
        }

        @Test
        fun `matches coin loss`() {
            val match = RegexPatterns.COIN_CHANGE.find(TestData.Chat.COIN_LOSS)
            assertNotNull(match)
            assertEquals("-1,000", match.groupValues[1])
            assertEquals("Shop", match.groupValues[2])
        }

        @Test
        fun `matches large coin amount`() {
            val match = RegexPatterns.COIN_CHANGE.find(TestData.Chat.COIN_LARGE)
            assertNotNull(match)
            assertEquals("+1,234,567", match.groupValues[1])
        }
    }

    @Nested
    inner class RareDropPattern {
        @Test
        fun `matches rare drop`() {
            val match = RegexPatterns.RARE_DROP.find(TestData.Chat.RARE_DROP)
            assertNotNull(match)
            assertEquals("Enchanted Diamond", match.groupValues[1])
        }

        @Test
        fun `matches very rare drop`() {
            val match = RegexPatterns.RARE_DROP.find(TestData.Chat.VERY_RARE_DROP)
            assertNotNull(match)
            assertEquals("Enchanted Emerald Block", match.groupValues[1])
        }

        @Test
        fun `matches crazy rare drop with magic find`() {
            val match = RegexPatterns.RARE_DROP.find(TestData.Chat.CRAZY_RARE_WITH_MF)
            assertNotNull(match)
            assertEquals("Wither Catalyst", match.groupValues[1])
        }
    }

    @Nested
    inner class PestChatPatterns {
        @Test
        fun `matches pest spawned message`() {
            val match = RegexPatterns.PEST_SPAWNED.find(TestData.Chat.PEST_SPAWNED)
            assertNotNull(match)
            assertEquals("5", match.groupValues[1])
        }

        @Test
        fun `matches pest cleared message`() {
            assert(RegexPatterns.PEST_CLEARED.containsMatchIn(TestData.Chat.PEST_CLEARED))
        }
    }

    @Nested
    inner class SlayerChatPatterns {
        @Test
        fun `matches slayer started`() {
            assertNotNull(RegexPatterns.SLAYER_SPAWNED.find(TestData.Chat.SLAYER_STARTED))
        }

        @Test
        fun `matches slayer complete`() {
            assertNotNull(RegexPatterns.SLAYER_COMPLETE.find(TestData.Chat.SLAYER_COMPLETE))
        }
    }

    @Nested
    inner class SkyBlockJoinPattern {
        @Test
        fun `matches skyblock join message`() {
            assertNotNull(RegexPatterns.SKYBLOCK_JOIN.find(TestData.Chat.SKYBLOCK_JOIN))
        }
    }
}
