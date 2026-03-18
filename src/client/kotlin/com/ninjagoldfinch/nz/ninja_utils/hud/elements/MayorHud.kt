package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.HypixelApiClient
import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger

object MayorHud : HudElement("mayor", "Mayor") {
    private val logger = ModLogger.category("MayorHud")
    private var fetchRequested = false

    override fun isEnabled(): Boolean = HudCategory.showMayor

    override fun getData(): HudLine? {
        val cached = HypixelApiClient.electionCache.get()
        if (cached != null) {
            val mayor = cached.mayor ?: return null
            val minister = mayor.minister
            val value = if (minister != null) {
                "${mayor.name} + ${minister.name}"
            } else {
                mayor.name
            }
            return HudLine("Mayor", value, valueColor = 0xFF55FFFF.toInt())
        }

        // Trigger async fetch if not already requested
        if (!fetchRequested) {
            fetchRequested = true
            HypixelApiClient.fetchElection().thenAccept {
                fetchRequested = false
            }
        }

        return null
    }
}
