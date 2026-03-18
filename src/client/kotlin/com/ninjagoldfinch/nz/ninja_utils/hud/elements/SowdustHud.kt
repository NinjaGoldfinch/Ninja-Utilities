package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import java.text.NumberFormat
import java.util.Locale

object SowdustHud : HudElement("sowdust", "Sowdust") {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    override fun isEnabled(): Boolean = HudCategory.showSowdust

    override fun getData(): HudLine? {
        if (HypixelState.currentIsland != SkyBlockIsland.GARDEN) return null
        val sowdust = HypixelState.sowdust
        if (sowdust == 0) return null
        return HudLine("Sowdust", numberFormat.format(sowdust), valueColor = 0xFF8B4513.toInt())
    }
}
