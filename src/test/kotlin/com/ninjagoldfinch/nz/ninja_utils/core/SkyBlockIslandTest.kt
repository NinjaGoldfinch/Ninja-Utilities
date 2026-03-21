package com.ninjagoldfinch.nz.ninja_utils.core

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests [SkyBlockIsland] enum mapping from Hypixel mode strings to islands.
 *
 * The mode strings come from the Hypixel Mod API location packet.
 * To add a new island: add the enum entry, then add it to the parameterized test data.
 */
class SkyBlockIslandTest {

    /** All known mode-to-island mappings. */
    @ParameterizedTest(name = "mode \"{0}\" maps to {1}")
    @MethodSource("allMappings")
    fun `mode string maps to correct island`(mode: String, expected: SkyBlockIsland) {
        assertEquals(expected, SkyBlockIsland.fromMode(mode))
    }

    @Nested
    inner class UnknownModes {
        @Test
        fun `unknown mode returns null`() {
            assertNull(SkyBlockIsland.fromMode("nonexistent_mode"))
        }

        @Test
        fun `empty string returns null`() {
            assertNull(SkyBlockIsland.fromMode(""))
        }

        @Test
        fun `case sensitive - uppercase mode returns null`() {
            assertNull(SkyBlockIsland.fromMode("HUB"))
        }
    }

    companion object {
        @JvmStatic
        fun allMappings(): List<Array<Any>> = SkyBlockIsland.entries.map {
            arrayOf(it.mode, it)
        }
    }
}
