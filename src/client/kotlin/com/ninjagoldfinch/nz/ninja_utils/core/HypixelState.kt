package com.ninjagoldfinch.nz.ninja_utils.core

import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker

/**
 * Singleton holding the player's current state on Hypixel.
 * Updated by the Hypixel Mod API and parsers.
 */
object HypixelState {
    var isOnHypixel: Boolean = false
    var environment: String? = null

    // Location data from Mod API
    var serverName: String? = null
    var serverType: String? = null   // "SKYBLOCK", "BEDWARS", etc.
    var mode: String? = null         // Island type for SkyBlock
    var map: String? = null
    var lobbyName: String? = null

    // Derived state
    val isInSkyBlock: Boolean get() = isOnHypixel && serverType.equals("SKYBLOCK", ignoreCase = true)
    val currentIsland: SkyBlockIsland? get() = mode?.let { SkyBlockIsland.fromMode(it) }
    val isInDungeon: Boolean get() = isInSkyBlock && mode == "dungeon"
    val isOnPrivateIsland: Boolean get() = isInSkyBlock && mode == "dynamic"
    val isLocationUnknown: Boolean get() = isOnHypixel && serverType == null
    val isInLobby: Boolean get() = isOnHypixel && serverType.equals("LOBBY", ignoreCase = true)
    val isInLimbo: Boolean get() = isOnHypixel && (serverType.equals("LIMBO", ignoreCase = true) || serverName.equals("limbo", ignoreCase = true))

    // Party info
    var partyLeader: String? = null
    var partyMembers: List<String> = emptyList()
    var playerRank: String? = null

    // Timestamps for diagnostics
    var lastHelloPacketTime: Long = 0
    var lastLocationPacketTime: Long = 0

    // Scoreboard-derived data (updated by ScoreboardParser)
    var purse: Long = 0
    var bits: Int = 0
    var currentArea: String? = null  // Finer-grained than mode (e.g., "Dwarven Mines" vs "mining_3")
    var skyblockDetectedViaScoreboard: Boolean = false

    // Garden-specific data (updated by ScoreboardParser)
    var copper: Int = 0
    var sowdust: Int = 0
    var gardenPests: Int = 0
    var currentPlot: String? = null  // e.g., "5" when on Plot - 5

    fun update(serverName: String?, serverType: String?, mode: String?, map: String?, lobbyName: String?) {
        val previousIsland = this.currentIsland
        this.serverName = serverName
        this.serverType = serverType
        this.mode = mode
        this.map = map
        this.lobbyName = lobbyName

        // Clear scoreboard-derived data when leaving SkyBlock
        if (!isInSkyBlock) {
            currentArea = null
            purse = 0
            bits = 0
            copper = 0
            sowdust = 0
            gardenPests = 0
            currentPlot = null
            SlayerTracker.reset()
        }

        if (currentIsland != previousIsland) {
            EventBus.post(IslandChangeEvent(previousIsland, currentIsland))
        }
    }

    fun reset() {
        isOnHypixel = false
        environment = null
        serverName = null
        serverType = null
        mode = null
        map = null
        lobbyName = null
        partyLeader = null
        partyMembers = emptyList()
        playerRank = null
        lastHelloPacketTime = 0
        lastLocationPacketTime = 0
        purse = 0
        bits = 0
        currentArea = null
        skyblockDetectedViaScoreboard = false
        copper = 0
        sowdust = 0
        gardenPests = 0
        currentPlot = null
    }
}
