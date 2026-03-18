package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import java.text.NumberFormat
import java.util.Locale

object CompostHud : HudElement("compost", "Compost") {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    override fun isEnabled(): Boolean = HudCategory.showCompost

    override fun getData(): HudLine? {
        if (HypixelState.currentIsland != SkyBlockIsland.GARDEN) return null
        val compost = HypixelState.compost
        if (compost == 0) return null
        return HudLine("Compost", numberFormat.format(compost), valueColor = 0xFF8B4513.toInt())
    }
}
