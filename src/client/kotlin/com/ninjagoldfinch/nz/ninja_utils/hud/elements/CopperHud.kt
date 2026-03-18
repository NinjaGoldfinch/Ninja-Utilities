package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import java.text.NumberFormat
import java.util.Locale

object CopperHud : HudElement("copper", "Copper") {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    override fun isEnabled(): Boolean = HudCategory.showCopper

    override fun getData(): HudLine? {
        if (HypixelState.currentIsland != SkyBlockIsland.GARDEN) return null
        val copper = HypixelState.copper
        if (copper == 0) return null
        return HudLine("Copper", numberFormat.format(copper), valueColor = 0xFFFF8C00.toInt())
    }
}
