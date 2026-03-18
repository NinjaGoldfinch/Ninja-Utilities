package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine

object SlayerHud : HudElement("slayer", "Slayer") {

    override fun isEnabled(): Boolean = HudCategory.showSlayer

    override fun getData(): HudLine? {
        val quest = SlayerTracker.activeQuest ?: return null

        val value = if (SlayerTracker.bossSpawned) {
            "$quest \u00a7c[BOSS]"
        } else {
            quest
        }

        return HudLine("Slayer", value, valueColor = 0xFFFF5555.toInt())
    }
}
