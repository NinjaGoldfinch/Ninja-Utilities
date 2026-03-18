package com.ninjagoldfinch.nz.ninja_utils.hud

import com.ninjagoldfinch.nz.ninja_utils.NinjaUtils
import com.ninjagoldfinch.nz.ninja_utils.config.GeneralCategory
import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.SkillXpGainEvent
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillTracker
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.BitsHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.CoinPurseHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.LocationHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.CookieTimerHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.DebugHudElement
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.MayorHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.PetHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.PingHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.SkillProgressHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.SkyBlockTimeHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.SlayerHud
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.TPSHud
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.logging.PerformanceMonitor
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement as FabricHudElement

object HudManager {
    private val logger = ModLogger.category("HUD")
    private val elements = mutableListOf<HudElement>()
    private var lastRenderState: Boolean? = null

    const val PADDING = 4
    const val LINE_HEIGHT = 11
    const val LABEL_SEPARATOR = ": "
    const val MAIN_PANEL_ID = "main"

    fun register(element: HudElement) {
        elements.add(element)
        logger.info("Registered HUD element: ${element.id}")
    }

    fun initialize() {
        register(LocationHud)
        register(CoinPurseHud)
        register(BitsHud)
        register(SkillProgressHud)
        register(SlayerHud)
        register(MayorHud)
        register(PetHud)
        register(CookieTimerHud)
        register(SkyBlockTimeHud)
        register(PingHud)
        register(TPSHud)
        register(DebugHudElement)

        EventBus.subscribe<SkillXpGainEvent> { event ->
            SkillProgressHud.recordXpGain(event.skill, event.xpGain, event.current, event.required)
            SkillTracker.recordXpGain(event.skill, event.xpGain)
        }

        HudPositions.load()
        HudPositions.initDefaults(elements)
        HudPositions.save()

        val overlay = FabricHudElement { drawContext, renderTickCounter ->
            renderAll(drawContext, renderTickCounter)
        }
        HudElementRegistry.addLast(Identifier.of(NinjaUtils.MOD_ID, "hud_overlay"), overlay)

        logger.info("HUD system initialized with ${elements.size} elements")
    }

    /** All registered elements (excluding debug overlay). */
    fun getMainElements(): List<HudElement> = elements.filter { it.id != "debug_overlay" }

    /** All registered elements including debug overlay. */
    fun getElements(): List<HudElement> = elements

    /** Collect all lines from enabled main elements into one list. */
    fun collectMainLines(): List<HudLine> {
        val lines = mutableListOf<HudLine>()
        for (element in elements) {
            if (element.id == "debug_overlay") continue
            if (!element.isEnabled()) continue
            lines.addAll(element.getLines())
        }
        return lines
    }

    private fun renderAll(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        val renderStart = System.nanoTime()
        if (!GeneralCategory.enabled) return

        val shouldRender = !GeneralCategory.skyblockOnly || HypixelState.isInSkyBlock

        if (lastRenderState != shouldRender) {
            if (shouldRender) {
                logger.info("HUD now rendering (skyblockOnly=${GeneralCategory.skyblockOnly}, isOnHypixel=${HypixelState.isOnHypixel}, serverType=${HypixelState.serverType})")
            } else {
                logger.info("HUD hidden (skyblockOnly=${GeneralCategory.skyblockOnly}, isOnHypixel=${HypixelState.isOnHypixel}, serverType=${HypixelState.serverType})")
            }
            lastRenderState = shouldRender
        }

        if (!shouldRender) return

        val scale = HudCategory.hudScale

        if (scale != 1.0f) {
            drawContext.matrices.pushMatrix()
            drawContext.matrices.scale(scale, scale)
        }

        // Main HUD — single combined panel
        val mainLines = collectMainLines()
        if (mainLines.isNotEmpty()) {
            val pos = HudPositions.getPosition(MAIN_PANEL_ID)
            val x = if (scale != 1.0f) (pos.x / scale).toInt() else pos.x
            val y = if (scale != 1.0f) (pos.y / scale).toInt() else pos.y
            renderPanel(drawContext, mainLines, x, y)
        }

        // Debug overlay — separate panel
        if (DebugHudElement.isEnabled()) {
            val debugLines = DebugHudElement.getLines()
            if (debugLines.isNotEmpty()) {
                val pos = HudPositions.getPosition(DebugHudElement.id)
                val x = if (scale != 1.0f) (pos.x / scale).toInt() else pos.x
                val y = if (scale != 1.0f) (pos.y / scale).toInt() else pos.y
                renderPanel(drawContext, debugLines, x, y)
            }
        }

        if (scale != 1.0f) {
            drawContext.matrices.popMatrix()
        }

        PerformanceMonitor.recordRenderTime(System.nanoTime() - renderStart)
    }

    fun renderPanel(context: DrawContext, lines: List<HudLine>, x: Int, y: Int) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val (panelWidth, panelHeight) = getPanelSize(lines)

        val bgAlpha = (HudCategory.hudBackgroundOpacity * 255).toInt().coerceIn(0, 255)
        if (bgAlpha > 0) {
            val bgColor = bgAlpha shl 24
            context.fill(x, y, x + panelWidth, y + panelHeight, bgColor)
        }

        for ((i, line) in lines.withIndex()) {
            val lineY = y + PADDING + i * LINE_HEIGHT
            val labelText = "${line.label}$LABEL_SEPARATOR"
            val labelWidth = textRenderer.getWidth(labelText)

            context.drawTextWithShadow(textRenderer, Text.literal(labelText), x + PADDING, lineY, line.labelColor)
            context.drawTextWithShadow(textRenderer, Text.literal(line.value), x + PADDING + labelWidth, lineY, line.valueColor)
        }
    }

    fun getPanelSize(lines: List<HudLine>): Pair<Int, Int> {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        var maxWidth = 0
        for (line in lines) {
            val fullText = "${line.label}$LABEL_SEPARATOR${line.value}"
            val width = textRenderer.getWidth(fullText)
            if (width > maxWidth) maxWidth = width
        }
        val panelWidth = maxWidth + PADDING * 2
        val panelHeight = lines.size * LINE_HEIGHT + PADDING * 2 - 2
        return panelWidth to panelHeight
    }
}
