package com.ninjagoldfinch.nz.ninja_utils.api.modapi

import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPlayerInfoPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPlayerInfoPacket

object ModApiManager {
    private val logger = ModLogger.category("ModAPI")

    // Raw packet data for diagnostics
    var lastHelloRaw: String? = null
        private set
    var lastLocationRaw: String? = null
        private set
    var lastPartyRaw: String? = null
        private set
    var lastPlayerInfoRaw: String? = null
        private set

    fun initialize() {
        val api = HypixelModAPI.getInstance()

        // Subscribe to location events (server pushes updates on instance change)
        api.subscribeToEventPacket(ClientboundLocationPacket::class.java)
        logger.info("Subscribed to location events")

        // Hello packet — confirms we're on Hypixel
        api.createHandler(ClientboundHelloPacket::class.java) { packet ->
            lastHelloRaw = "environment=${packet.environment} (name=${packet.environment.name})"

            HypixelState.isOnHypixel = true
            HypixelState.environment = packet.environment.name
            HypixelState.lastHelloPacketTime = System.currentTimeMillis()
            logger.info("HelloPacket: $lastHelloRaw")
        }

        // Location packet — primary mechanism for detecting where the player is
        api.createHandler(ClientboundLocationPacket::class.java) { packet ->
            val rawServerType = packet.serverType.orElse(null)
            val serverName = packet.serverName
            val serverType = rawServerType?.name
            val mode = packet.mode.orElse(null)
            val map = packet.map.orElse(null)
            val lobbyName = packet.lobbyName.orElse(null)

            lastLocationRaw = "serverName=$serverName, serverType=$rawServerType (name=$serverType), mode=$mode, map=$map, lobbyName=$lobbyName"

            HypixelState.lastLocationPacketTime = System.currentTimeMillis()
            HypixelState.update(
                serverName = serverName,
                serverType = serverType,
                mode = mode,
                map = map,
                lobbyName = lobbyName
            )

            logger.info("LocationPacket: $lastLocationRaw")
        }

        // Party info packet — response to our request
        api.createHandler(ClientboundPartyInfoPacket::class.java) { packet ->
            try {
                val inParty = packet.isInParty
                val leader = packet.leader.orElse(null)
                lastPartyRaw = "inParty=$inParty, leader=$leader"

                if (inParty) {
                    HypixelState.partyLeader = leader?.toString()
                } else {
                    HypixelState.partyLeader = null
                    HypixelState.partyMembers = emptyList()
                }

                logger.info("PartyPacket: $lastPartyRaw")
            } catch (e: Exception) {
                lastPartyRaw = "error: ${e.message}"
                logger.error("Failed to process PartyInfoPacket", e)
            }
        }

        // Player info packet — response to our request
        api.createHandler(ClientboundPlayerInfoPacket::class.java) { packet ->
            try {
                lastPlayerInfoRaw = packet.toString()
                logger.info("PlayerInfoPacket: $lastPlayerInfoRaw")
            } catch (e: Exception) {
                lastPlayerInfoRaw = "error: ${e.message}"
                logger.error("Failed to process PlayerInfoPacket", e)
            }
        }
    }

    fun requestPartyInfo() {
        HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())
        logger.debug("Requested party info")
    }

    fun requestPlayerInfo() {
        HypixelModAPI.getInstance().sendPacket(ServerboundPlayerInfoPacket())
        logger.debug("Requested player info")
    }
}
