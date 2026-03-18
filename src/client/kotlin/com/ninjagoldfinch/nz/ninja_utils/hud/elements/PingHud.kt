package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.features.stats.PingTracker
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine

object PingHud : HudElement("ping", "Ping") {

    override fun isEnabled(): Boolean = HudCategory.showPing

    override fun getData(): HudLine? {
        val ping = PingTracker.ping
        if (ping <= 0) return null

        val color = when {
            ping < 100 -> 0xFF55FF55.toInt()   // green
            ping < 200 -> 0xFFFFFF55.toInt()    // yellow
            ping < 400 -> 0xFFFFAA00.toInt()    // orange
            else -> 0xFFFF5555.toInt()           // red
        }

        return HudLine("Ping", "${ping}ms", valueColor = color)
    }
}
