package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import com.ninjagoldfinch.nz.ninja_utils.parsers.ScoreboardParser

object SkyBlockTimeHud : HudElement("sb_time", "SkyBlock Time") {

    override fun isEnabled(): Boolean = HudCategory.showSbTime

    override fun getData(): HudLine? {
        val date = ScoreboardParser.lastDate ?: return null
        return HudLine("Date", date, valueColor = 0xFFAAAAFF.toInt())
    }
}
