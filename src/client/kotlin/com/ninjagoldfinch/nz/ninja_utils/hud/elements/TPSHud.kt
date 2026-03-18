package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.features.stats.TPSTracker
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine

object TPSHud : HudElement("tps", "TPS") {

    override fun isEnabled(): Boolean = HudCategory.showTps

    override fun getData(): HudLine? {
        val tps = TPSTracker.tps ?: return null

        val color = when {
            tps >= 19.0 -> 0xFF55FF55.toInt()   // green
            tps >= 15.0 -> 0xFFFFFF55.toInt()    // yellow
            tps >= 10.0 -> 0xFFFFAA00.toInt()    // orange
            else -> 0xFFFF5555.toInt()            // red
        }

        return HudLine("TPS", String.format("%.1f", tps), valueColor = color)
    }
}
