package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import com.ninjagoldfinch.nz.ninja_utils.parsers.TabListParser

object CookieTimerHud : HudElement("cookie_timer", "Cookie Timer") {

    override fun isEnabled(): Boolean = HudCategory.showCookieTimer

    override fun getData(): HudLine? {
        val tab = TabListParser.lastData ?: return null
        val cookie = tab.cookie ?: return null
        return HudLine("Cookie", cookie, valueColor = 0xFFFF55FF.toInt())
    }
}
