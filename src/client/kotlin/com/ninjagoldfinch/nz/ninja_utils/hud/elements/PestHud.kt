package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine

object PestHud : HudElement("pests", "Pests") {

    override fun isEnabled(): Boolean = HudCategory.showPests

    override fun getData(): HudLine? {
        if (HypixelState.currentIsland != SkyBlockIsland.GARDEN) return null
        val pests = HypixelState.gardenPests
        if (pests == 0) return null
        return HudLine("Pests", pests.toString(), valueColor = 0xFFFF5555.toInt())
    }
}
