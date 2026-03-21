package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import java.text.NumberFormat
import java.util.Locale

object BitsHud : HudElement("bits", "Bits") {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    override fun isEnabled(): Boolean = HudCategory.showBits

    override fun getData(): HudLine? {
        val bits = HypixelState.bits
        if (bits == 0) return null
        return HudLine("Bits", numberFormat.format(bits), valueColor = 0xFF55FFFF.toInt())
    }
}
