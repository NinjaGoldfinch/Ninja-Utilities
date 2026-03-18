package com.ninjagoldfinch.nz.ninja_utils.hud

import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.DebugHudElement
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

/**
 * A screen that lets the user drag HUD panels and toggle elements.
 * - Left-click + drag to move panels
 * - Right-click on a line to toggle that element on/off
 * - Hold Shift to disable edge snapping
 */
class HudConfigScreen : Screen(Text.literal("HUD Position Editor")) {

    private data class PanelState(
        val id: String,
        val title: String,
        var x: Int,
        var y: Int
    )

    private lateinit var mainPanel: PanelState
    private lateinit var debugPanel: PanelState
    private var dragTarget: PanelState? = null
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0

    private companion object {
        const val SNAP_DISTANCE = 8
        const val TITLE_HEIGHT = 12
        const val TITLE_COLOR = 0xFFFFFF55.toInt()
        const val DISABLED_TITLE_COLOR = 0xFF888888.toInt()
        const val BORDER_COLOR = 0xFF555555.toInt()
        const val BORDER_DRAG_COLOR = 0xFFFFFF00.toInt()
    }

    override fun init() {
        super.init()
        val mainPos = HudPositions.getPosition(HudManager.MAIN_PANEL_ID)
        mainPanel = PanelState(HudManager.MAIN_PANEL_ID, "Main HUD", mainPos.x, mainPos.y)

        val debugPos = HudPositions.getPosition(DebugHudElement.id)
        debugPanel = PanelState(DebugHudElement.id, "Debug Overlay", debugPos.x, debugPos.y)
    }

    private fun getMainLines(): List<HudLine> {
        // Show all elements — enabled ones with live/sample data, disabled ones dimmed
        val lines = mutableListOf<HudLine>()
        for (element in HudManager.getMainElements()) {
            if (element.isEnabled()) {
                val live = element.getLines()
                if (live.isNotEmpty()) {
                    lines.addAll(live)
                } else {
                    lines.addAll(element.getSampleData())
                }
            } else {
                // Show disabled elements as grey sample data
                for (sample in element.getSampleData()) {
                    lines.add(HudLine(sample.label, "(OFF)", labelColor = 0xFF666666.toInt(), valueColor = 0xFF666666.toInt()))
                }
            }
        }
        return lines
    }

    private fun getDebugLines(): List<HudLine> {
        return if (DebugHudElement.isEnabled()) {
            val live = DebugHudElement.getLines()
            if (live.isNotEmpty()) live else DebugHudElement.getSampleData()
        } else {
            DebugHudElement.getSampleData().map {
                HudLine(it.label, "(OFF)", labelColor = 0xFF666666.toInt(), valueColor = 0xFF666666.toInt())
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0x80000000.toInt())

        val tr = textRenderer

        // Render main panel
        val mainLines = getMainLines()
        renderConfigPanel(context, mainPanel, mainLines, tr)

        // Render debug panel
        val debugLines = getDebugLines()
        renderConfigPanel(context, debugPanel, debugLines, tr)

        // Instructions
        val hint = when {
            dragTarget != null -> "Release to place  |  Hold Shift to disable snapping"
            else -> "Left-click: drag  |  Right-click on line: toggle  |  ESC: save & close"
        }
        val hintWidth = tr.getWidth(hint)
        context.drawTextWithShadow(tr, Text.literal(hint),
            (width - hintWidth) / 2, height - 20, 0xFFCCCCCC.toInt())
    }

    private fun renderConfigPanel(context: DrawContext, panel: PanelState, lines: List<HudLine>,
                                   tr: net.minecraft.client.font.TextRenderer) {
        if (lines.isEmpty()) return
        val (panelWidth, panelHeight) = HudManager.getPanelSize(lines)
        val isDragging = dragTarget == panel

        // Title bar
        val titleY = panel.y - TITLE_HEIGHT
        val titleWidth = tr.getWidth(panel.title) + HudManager.PADDING * 2
        val fullWidth = maxOf(panelWidth, titleWidth)

        val isEnabled = if (panel.id == DebugHudElement.id) DebugHudElement.isEnabled() else true
        val titleBg = if (isEnabled) 0xC0222222.toInt() else 0xC0440000.toInt()
        context.fill(panel.x, titleY, panel.x + fullWidth, panel.y, titleBg)

        val titleColor = if (isEnabled) TITLE_COLOR else DISABLED_TITLE_COLOR
        context.drawTextWithShadow(tr, Text.literal(panel.title),
            panel.x + HudManager.PADDING, titleY + 2, titleColor)

        // Panel content
        HudManager.renderPanel(context, lines, panel.x, panel.y)

        // Border
        val borderColor = if (isDragging) BORDER_DRAG_COLOR else BORDER_COLOR
        val bx1 = panel.x - 1
        val by1 = titleY - 1
        val bx2 = panel.x + fullWidth + 1
        val by2 = panel.y + panelHeight + 1
        context.fill(bx1, by1, bx2, by1 + 1, borderColor)
        context.fill(bx1, by2 - 1, bx2, by2, borderColor)
        context.fill(bx1, by1, bx1 + 1, by2, borderColor)
        context.fill(bx2 - 1, by1, bx2, by2, borderColor)
    }

    override fun mouseClicked(click: net.minecraft.client.gui.Click, doubled: Boolean): Boolean {
        val button = click.buttonInfo().button()
        val mx = click.x().toInt()
        val my = click.y().toInt()

        // Check panels in order: debug first (on top), then main
        for (panel in listOf(debugPanel, mainPanel)) {
            val lines = if (panel.id == DebugHudElement.id) getDebugLines() else getMainLines()
            if (lines.isEmpty()) continue
            val (panelWidth, panelHeight) = HudManager.getPanelSize(lines)
            val titleWidth = textRenderer.getWidth(panel.title) + HudManager.PADDING * 2
            val fullWidth = maxOf(panelWidth, titleWidth)
            val titleY = panel.y - TITLE_HEIGHT

            if (mx in panel.x..(panel.x + fullWidth) && my in titleY..(panel.y + panelHeight)) {
                if (button == 0) {
                    dragTarget = panel
                    dragOffsetX = click.x() - panel.x
                    dragOffsetY = click.y() - panel.y
                    return true
                } else if (button == 1) {
                    if (panel.id == DebugHudElement.id) {
                        DebugCategory.debugOverlay = !DebugCategory.debugOverlay
                    } else {
                        // Find which line was clicked and toggle that element
                        val lineIndex = (my - panel.y - HudManager.PADDING) / HudManager.LINE_HEIGHT
                        toggleElementAtLine(lineIndex)
                    }
                    return true
                }
            }
        }
        return super.mouseClicked(click, doubled)
    }

    /**
     * Given a line index in the main panel, find which element owns that line and toggle it.
     */
    private fun toggleElementAtLine(lineIndex: Int) {
        if (lineIndex < 0) return
        var currentLine = 0
        for (element in HudManager.getMainElements()) {
            val lineCount = if (element.isEnabled()) {
                val live = element.getLines()
                if (live.isNotEmpty()) live.size else element.getSampleData().size
            } else {
                element.getSampleData().size
            }
            if (lineIndex < currentLine + lineCount) {
                toggleElement(element)
                return
            }
            currentLine += lineCount
        }
    }

    override fun mouseDragged(click: net.minecraft.client.gui.Click, offsetX: Double, offsetY: Double): Boolean {
        val target = dragTarget ?: return super.mouseDragged(click, offsetX, offsetY)
        if (click.buttonInfo().button() != 0) return super.mouseDragged(click, offsetX, offsetY)

        val lines = if (target.id == DebugHudElement.id) getDebugLines() else getMainLines()
        if (lines.isEmpty()) return true
        val (panelWidth, panelHeight) = HudManager.getPanelSize(lines)

        var newX = (click.x() - dragOffsetX).toInt()
        var newY = (click.y() - dragOffsetY).toInt()

        newX = newX.coerceIn(0, width - panelWidth)
        newY = newY.coerceIn(TITLE_HEIGHT, height - panelHeight)

        val window = MinecraftClient.getInstance().window.handle
        val shiftHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS

        if (!shiftHeld) {
            if (newX < SNAP_DISTANCE) newX = 0
            else if (newX + panelWidth > width - SNAP_DISTANCE) newX = width - panelWidth

            if (newY < SNAP_DISTANCE + TITLE_HEIGHT) newY = TITLE_HEIGHT
            else if (newY + panelHeight > height - SNAP_DISTANCE) newY = height - panelHeight
        }

        target.x = newX
        target.y = newY
        return true
    }

    override fun mouseReleased(click: net.minecraft.client.gui.Click): Boolean {
        if (dragTarget != null && click.buttonInfo().button() == 0) {
            dragTarget = null
            return true
        }
        return super.mouseReleased(click)
    }

    override fun close() {
        HudPositions.setPosition(mainPanel.id, mainPanel.x, mainPanel.y)
        HudPositions.setPosition(debugPanel.id, debugPanel.x, debugPanel.y)
        HudPositions.save()
        super.close()
    }

    override fun shouldPause(): Boolean = false

    private fun toggleElement(element: HudElement) {
        when (element.id) {
            "location" -> HudCategory.showLocation = !HudCategory.showLocation
            "coinPurse" -> HudCategory.showPurse = !HudCategory.showPurse
            "bits" -> HudCategory.showBits = !HudCategory.showBits
            "skillProgress" -> HudCategory.showSkillProgress = !HudCategory.showSkillProgress
            "slayer" -> HudCategory.showSlayer = !HudCategory.showSlayer
            "mayor" -> HudCategory.showMayor = !HudCategory.showMayor
            "pet" -> HudCategory.showPet = !HudCategory.showPet
            "cookie_timer" -> HudCategory.showCookieTimer = !HudCategory.showCookieTimer
            "sb_time" -> HudCategory.showSbTime = !HudCategory.showSbTime
            "ping" -> HudCategory.showPing = !HudCategory.showPing
            "tps" -> HudCategory.showTps = !HudCategory.showTps
        }
    }
}
