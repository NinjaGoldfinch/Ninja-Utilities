package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import java.text.NumberFormat
import java.util.Locale

object CoinPurseHud : HudElement("coinPurse", "Coin Purse") {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    override fun isEnabled(): Boolean = HudCategory.showPurse

    override fun getData(): HudLine? {
        val coins = HypixelState.purse
        if (coins == 0L) return null // hide when no purse data
        return HudLine("Purse", numberFormat.format(coins), valueColor = 0xFFFFAA00.toInt())
    }
}
