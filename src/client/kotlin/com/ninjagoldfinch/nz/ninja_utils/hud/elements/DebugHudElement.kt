package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import com.ninjagoldfinch.nz.ninja_utils.logging.DebugOverlay

object DebugHudElement : HudElement("debug_overlay", "Debug Overlay") {

    override fun isEnabled(): Boolean = DebugCategory.debugOverlay

    override fun getData(): HudLine? = null

    override fun getLines(): List<HudLine> = DebugOverlay.buildLines()

    override fun getSampleData(): List<HudLine> = listOf(
        HudLine("Hypixel", "true", valueColor = 0xFF55FF55.toInt()),
        HudLine("SkyBlock", "true", valueColor = 0xFF55FF55.toInt()),
        HudLine("ServerType", "SKYBLOCK", valueColor = 0xFFFFFFFF.toInt()),
        HudLine("Ping", "50ms", valueColor = 0xFFFFFFFF.toInt()),
        HudLine("TPS", "20.0", valueColor = 0xFFFFFFFF.toInt()),
        HudLine("Memory", "512/2048 MB (25%)", valueColor = 0xFF55FF55.toInt()),
    )
}
