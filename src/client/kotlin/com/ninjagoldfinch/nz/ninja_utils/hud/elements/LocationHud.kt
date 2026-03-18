package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine

object LocationHud : HudElement("location", "Location") {

    override fun isEnabled(): Boolean = HudCategory.showLocation

    override fun getData(): HudLine? {
        val islandName = HypixelState.currentIsland?.displayName
        val scoreboardArea = HypixelState.currentArea

        val value = when {
            islandName != null && scoreboardArea != null -> "$islandName ($scoreboardArea)"
            islandName != null -> islandName
            scoreboardArea != null -> {
                val server = HypixelState.serverName
                if (server != null) "$scoreboardArea ($server)" else scoreboardArea
            }
            else -> return null // no data — hide
        }

        return HudLine("Location", value, valueColor = 0xFF55FFFF.toInt())
    }
}
