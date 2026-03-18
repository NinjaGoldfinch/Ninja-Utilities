package com.ninjagoldfinch.nz.ninja_utils.logging

import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkyBlockIsland
import com.ninjagoldfinch.nz.ninja_utils.features.stats.PingTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.TPSTracker
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.SkillProgressHud

/**
 * Builds debug overlay lines. Rendering is handled by DebugHudElement via HudManager.
 */
object DebugOverlay {
    private const val TRUE_COLOR = 0xFF55FF55.toInt()
    private const val FALSE_COLOR = 0xFFFF5555.toInt()
    private const val VALUE_COLOR = 0xFFFFFFFF.toInt()

    fun buildLines(): List<HudLine> {
        val lines = mutableListOf<HudLine>()
        val s = HypixelState

        lines.add(HudLine("Hypixel", s.isOnHypixel.toString(), valueColor = boolColor(s.isOnHypixel)))
        lines.add(HudLine("SkyBlock", s.isInSkyBlock.toString(), valueColor = boolColor(s.isInSkyBlock)))
        lines.add(HudLine("ServerType", s.serverType ?: "null", valueColor = VALUE_COLOR))
        lines.add(HudLine("Mode", s.mode ?: "null", valueColor = VALUE_COLOR))
        lines.add(HudLine("Island", s.currentIsland?.displayName ?: "null", valueColor = VALUE_COLOR))
        lines.add(HudLine("Area", s.currentArea ?: "null", valueColor = VALUE_COLOR))
        lines.add(HudLine("Purse", s.purse.toString(), valueColor = VALUE_COLOR))
        lines.add(HudLine("Bits", s.bits.toString(), valueColor = VALUE_COLOR))

        val now = System.currentTimeMillis()
        val helloAge = if (s.lastHelloPacketTime > 0) "${(now - s.lastHelloPacketTime) / 1000}s" else "never"
        val locAge = if (s.lastLocationPacketTime > 0) "${(now - s.lastLocationPacketTime) / 1000}s" else "never"
        lines.add(HudLine("Hello", helloAge, valueColor = VALUE_COLOR))
        lines.add(HudLine("Location", locAge, valueColor = VALUE_COLOR))
        lines.add(HudLine("SB Fallback", s.skyblockDetectedViaScoreboard.toString(), valueColor = boolColor(s.skyblockDetectedViaScoreboard)))

        // Garden-specific debug info
        if (s.currentIsland == SkyBlockIsland.GARDEN) {
            lines.add(HudLine("Copper", s.copper.toString(), valueColor = VALUE_COLOR))
            lines.add(HudLine("Compost", s.compost.toString(), valueColor = VALUE_COLOR))
        }

        val slayer = SlayerTracker.activeQuest
        if (slayer != null) {
            lines.add(HudLine("Slayer", slayer, valueColor = VALUE_COLOR))
            lines.add(HudLine("Boss", SlayerTracker.bossSpawned.toString(), valueColor = boolColor(SlayerTracker.bossSpawned)))
            lines.add(HudLine("Kills", SlayerTracker.completionsThisSession.toString(), valueColor = VALUE_COLOR))
        }

        val skill = SkillProgressHud.currentSkill
        if (skill != null) {
            val rate = SkillTracker.getRate(skill)
            if (rate != null) {
                lines.add(HudLine("XP/min", String.format("%.0f %s", rate.xpPerMinute, skill), valueColor = VALUE_COLOR))
            }
        }

        // Network stats
        val ping = PingTracker.ping
        if (ping > 0) lines.add(HudLine("Ping", "${ping}ms", valueColor = VALUE_COLOR))
        val tps = TPSTracker.tps
        if (tps != null) lines.add(HudLine("TPS", String.format("%.1f", tps), valueColor = VALUE_COLOR))

        // Performance stats
        val perf = PerformanceMonitor
        lines.add(HudLine("Tick", String.format("%.0f/%.0f \u00b5s", perf.avgTickTimeUs, perf.maxTickTimeUs), valueColor = VALUE_COLOR))
        lines.add(HudLine("Render", String.format("%.0f \u00b5s", perf.avgRenderTimeUs), valueColor = VALUE_COLOR))

        // Memory stats
        val memColor = when {
            perf.memoryPercent > 85 -> FALSE_COLOR
            perf.memoryPercent > 70 -> 0xFFFFFF55.toInt()
            else -> TRUE_COLOR
        }
        lines.add(HudLine("Memory", "${perf.usedMemoryMb}/${perf.maxMemoryMb} MB (${perf.memoryPercent}%)", valueColor = memColor))

        return lines
    }

    private fun boolColor(value: Boolean): Int = if (value) TRUE_COLOR else FALSE_COLOR
}
