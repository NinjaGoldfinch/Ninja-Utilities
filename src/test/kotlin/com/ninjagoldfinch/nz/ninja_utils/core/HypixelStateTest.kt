package com.ninjagoldfinch.nz.ninja_utils.core

import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests HypixelState derived properties and state transitions.
 *
 * Tests verify the behavioral contracts:
 * - Derived properties (isInSkyBlock, currentIsland, etc.) return correct values
 *   based on the current state combination
 * - update() triggers correct side effects (event posting, data clearing)
 * - reset() returns to clean state
 *
 * SlayerTracker is mocked since update() calls SlayerTracker.reset().
 */
class HypixelStateTest {

    @BeforeEach
    fun setup() {
        HypixelState.reset()
        mockkObject(SlayerTracker)
        io.mockk.every { SlayerTracker.reset() } returns Unit
    }

    @AfterEach
    fun cleanup() {
        HypixelState.reset()
        unmockkAll()
    }

    @Nested
    inner class DerivedProperties {
        @Test
        fun `isInSkyBlock requires both hypixel connection and skyblock server type`() {
            assertFalse(HypixelState.isInSkyBlock)

            HypixelState.isOnHypixel = true
            assertFalse(HypixelState.isInSkyBlock)

            HypixelState.serverType = "SKYBLOCK"
            assertTrue(HypixelState.isInSkyBlock)
        }

        @Test
        fun `isInSkyBlock is case insensitive`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "skyblock"
            assertTrue(HypixelState.isInSkyBlock)
        }

        @Test
        fun `currentIsland maps mode string through SkyBlockIsland enum`() {
            HypixelState.mode = "garden"
            assertEquals(SkyBlockIsland.GARDEN, HypixelState.currentIsland)
        }

        @Test
        fun `currentIsland returns null for unknown mode`() {
            HypixelState.mode = "unknown_mode"
            assertNull(HypixelState.currentIsland)
        }

        @Test
        fun `currentIsland returns null when mode is null`() {
            assertNull(HypixelState.currentIsland)
        }

        @Test
        fun `isInDungeon requires skyblock and dungeon mode`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "SKYBLOCK"
            HypixelState.mode = "dungeon"
            assertTrue(HypixelState.isInDungeon)
        }

        @Test
        fun `isOnPrivateIsland requires skyblock and dynamic mode`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "SKYBLOCK"
            HypixelState.mode = "dynamic"
            assertTrue(HypixelState.isOnPrivateIsland)
        }

        @Test
        fun `isInLobby detects lobby server type`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "LOBBY"
            assertTrue(HypixelState.isInLobby)
        }

        @Test
        fun `isInLimbo detects limbo`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "LIMBO"
            assertTrue(HypixelState.isInLimbo)
        }
    }

    @Nested
    inner class StateTransitions {
        @Test
        fun `leaving skyblock clears scoreboard-derived data`() {
            HypixelState.isOnHypixel = true
            HypixelState.purse = 1000
            HypixelState.bits = 500
            HypixelState.currentArea = "Garden"
            HypixelState.copper = 100
            HypixelState.sowdust = 200
            HypixelState.gardenPests = 3

            // Update to non-skyblock server type
            HypixelState.update("server1", "BEDWARS", null, null, null)

            assertEquals(0, HypixelState.purse)
            assertEquals(0, HypixelState.bits)
            assertNull(HypixelState.currentArea)
            assertEquals(0, HypixelState.copper)
            assertEquals(0, HypixelState.sowdust)
            assertEquals(0, HypixelState.gardenPests)
        }

        @Test
        fun `island change posts IslandChangeEvent`() {
            HypixelState.isOnHypixel = true
            val received = mutableListOf<IslandChangeEvent>()
            EventBus.subscribe<IslandChangeEvent> { received.add(it) }

            HypixelState.update("server1", "SKYBLOCK", "hub", null, null)

            assertEquals(1, received.size)
            assertNull(received[0].previous)
            assertEquals(SkyBlockIsland.HUB, received[0].current)
        }

        @Test
        fun `same island does not post event`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "SKYBLOCK"
            HypixelState.mode = "hub"

            val received = mutableListOf<IslandChangeEvent>()
            EventBus.subscribe<IslandChangeEvent> { received.add(it) }

            HypixelState.update("server1", "SKYBLOCK", "hub", null, null)

            assertTrue(received.isEmpty())
        }
    }

    @Nested
    inner class ResetBehavior {
        @Test
        fun `reset clears all state`() {
            HypixelState.isOnHypixel = true
            HypixelState.serverType = "SKYBLOCK"
            HypixelState.mode = "garden"
            HypixelState.purse = 1000

            HypixelState.reset()

            assertFalse(HypixelState.isOnHypixel)
            assertNull(HypixelState.serverType)
            assertNull(HypixelState.mode)
            assertEquals(0, HypixelState.purse)
        }
    }
}
