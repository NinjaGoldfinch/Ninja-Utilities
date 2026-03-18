package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import java.text.NumberFormat
import java.util.Locale

object SkillProgressHud : HudElement("skillProgress", "Skill Progress") {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    var currentSkill: String? = null
    var lastXpGain: Double = 0.0
    var currentXp: Long = 0
    var requiredXp: Long = 0
    var lastUpdateTime: Long = 0

    private const val DISPLAY_DURATION_MS = 10_000L

    override fun isEnabled(): Boolean = HudCategory.showSkillProgress

    override fun getData(): HudLine? {
        val skill = currentSkill ?: return null
        val elapsed = System.currentTimeMillis() - lastUpdateTime
        if (elapsed > DISPLAY_DURATION_MS) return null

        val value = if (requiredXp > 0) {
            val percent = (currentXp.toDouble() / requiredXp * 100).toInt().coerceIn(0, 100)
            "${numberFormat.format(currentXp)}/${numberFormat.format(requiredXp)} ($percent%) +${String.format("%.1f", lastXpGain)}"
        } else {
            "+${numberFormat.format(lastXpGain.toLong())}"
        }

        return HudLine(skill, value, valueColor = 0xFF55FF55.toInt())
    }

    fun recordXpGain(skill: String, xpGain: Double, current: Long, required: Long) {
        currentSkill = skill
        lastXpGain = xpGain
        currentXp = current
        requiredXp = required
        lastUpdateTime = System.currentTimeMillis()
    }
}
