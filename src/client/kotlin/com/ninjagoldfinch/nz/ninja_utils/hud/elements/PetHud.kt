package com.ninjagoldfinch.nz.ninja_utils.hud.elements

import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.HudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.HudLine
import com.ninjagoldfinch.nz.ninja_utils.parsers.TabListParser

object PetHud : HudElement("pet", "Pet") {

    override fun isEnabled(): Boolean = HudCategory.showPet

    override fun getData(): HudLine? {
        val tab = TabListParser.lastData ?: return null
        val pet = tab.pet ?: return null
        return HudLine("Pet", pet, valueColor = 0xFF55FF55.toInt())
    }
}
