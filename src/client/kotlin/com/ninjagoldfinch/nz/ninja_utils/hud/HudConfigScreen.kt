package com.ninjagoldfinch.nz.ninja_utils.hud

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

/**
 * A screen that lets the user drag individual HUD elements to reposition them.
 * - Left-click + drag to move an element
 * - Right-click to toggle an element on/off
 * - Hold Shift to disable edge snapping
 */
class HudConfigScreen : Screen(Text.literal("HUD Position Editor")) {

    private data class ElementState(
        val element: HudElement,
        var x: Int,
        var y: Int
    )

    private val elementStates = mutableListOf<ElementState>()
    private var dragTarget: ElementState? = null
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0

    private companion object {
        const val SNAP_DISTANCE = 8
        const val TITLE_HEIGHT = 12
        const val TITLE_COLOR = 0xFFFFFF55.toInt()
        const val DISABLED_TITLE_COLOR = 0xFF888888.toInt()
        const val BORDER_COLOR = 0xFF555555.toInt()
        const val BORDER_DRAG_COLOR = 0xFFFFFF00.toInt()
        const val DISABLED_BG_COLOR = 0x40000000
    }

    override fun init() {
        super.init()
        elementStates.clear()
        for (element in HudManager.getElements()) {
            val pos = HudPositions.getPosition(element.id)
            elementStates.add(ElementState(element, pos.x, pos.y))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        // Dim background
        context.fill(0, 0, width, height, 0x80000000.toInt())

        val tr = textRenderer

        for (state in elementStates) {
            val element = state.element
            val enabled = element.isEnabled()
            val lines = if (enabled) {
                val live = element.getLines()
                if (live.isNotEmpty()) live else element.getSampleData()
            } else element.getSampleData()
            val (panelWidth, panelHeight) = HudManager.getPanelSize(lines)

            val isDragging = dragTarget == state

            // Title bar above the panel
            val titleY = state.y - TITLE_HEIGHT
            val titleText = element.displayName + if (!enabled) " (OFF)" else ""
            val titleWidth = tr.getWidth(titleText) + HudManager.PADDING * 2
            val fullWidth = maxOf(panelWidth, titleWidth)

            // Title background
            val titleBg = if (enabled) 0xC0222222.toInt() else 0xC0440000.toInt()
            context.fill(state.x, titleY, state.x + fullWidth, state.y, titleBg)

            // Title text
            val titleColor = if (enabled) TITLE_COLOR else DISABLED_TITLE_COLOR
            context.drawTextWithShadow(tr, Text.literal(titleText),
                state.x + HudManager.PADDING, titleY + 2, titleColor)

            // Panel content
            if (enabled) {
                HudManager.renderPanel(context, lines, state.x, state.y)
            } else {
                // Draw dimmed panel for disabled elements
                context.fill(state.x, state.y, state.x + panelWidth, state.y + panelHeight, DISABLED_BG_COLOR)
                for ((i, line) in lines.withIndex()) {
                    val labelText = "${line.label}${HudManager.LABEL_SEPARATOR}"
                    val labelWidth = tr.getWidth(labelText)
                    val lineY = state.y + HudManager.PADDING + i * HudManager.LINE_HEIGHT
                    context.drawTextWithShadow(tr, Text.literal(labelText),
                        state.x + HudManager.PADDING, lineY, 0xFF666666.toInt())
                    context.drawTextWithShadow(tr, Text.literal(line.value),
                        state.x + HudManager.PADDING + labelWidth, lineY, 0xFF666666.toInt())
                }
            }

            // Border
            val borderColor = if (isDragging) BORDER_DRAG_COLOR else BORDER_COLOR
            val totalHeight = TITLE_HEIGHT + panelHeight
            val bx1 = state.x - 1
            val by1 = titleY - 1
            val bx2 = state.x + fullWidth + 1
            val by2 = state.y + panelHeight + 1
            context.fill(bx1, by1, bx2, by1 + 1, borderColor)     // top
            context.fill(bx1, by2 - 1, bx2, by2, borderColor)     // bottom
            context.fill(bx1, by1, bx1 + 1, by2, borderColor)     // left
            context.fill(bx2 - 1, by1, bx2, by2, borderColor)     // right
        }

        // Instructions
        val hint = when {
            dragTarget != null -> "Release to place  |  Hold Shift to disable snapping"
            else -> "Left-click: drag  |  Right-click: toggle on/off  |  ESC: save & close"
        }
        val hintWidth = tr.getWidth(hint)
        context.drawTextWithShadow(tr, Text.literal(hint),
            (width - hintWidth) / 2, height - 20, 0xFFCCCCCC.toInt())
    }

    override fun mouseClicked(click: net.minecraft.client.gui.Click, doubled: Boolean): Boolean {
        val button = click.buttonInfo().button()
        val mx = click.x().toInt()
        val my = click.y().toInt()

        // Find which element was clicked (check in reverse order for z-ordering)
        for (state in elementStates.reversed()) {
            val lines = if (state.element.isEnabled()) {
                val live = state.element.getLines()
                if (live.isNotEmpty()) live else state.element.getSampleData()
            } else state.element.getSampleData()
            val (panelWidth, panelHeight) = HudManager.getPanelSize(lines)
            val titleText = state.element.displayName + if (!state.element.isEnabled()) " (OFF)" else ""
            val titleWidth = textRenderer.getWidth(titleText) + HudManager.PADDING * 2
            val fullWidth = maxOf(panelWidth, titleWidth)
            val titleY = state.y - TITLE_HEIGHT
            val totalHeight = TITLE_HEIGHT + panelHeight

            if (mx in state.x..(state.x + fullWidth) && my in titleY..(state.y + panelHeight)) {
                if (button == 0) {
                    // Left click — start dragging
                    dragTarget = state
                    dragOffsetX = click.x() - state.x
                    dragOffsetY = click.y() - state.y
                    return true
                } else if (button == 1) {
                    // Right click — toggle enabled
                    toggleElement(state.element)
                    return true
                }
            }
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseDragged(click: net.minecraft.client.gui.Click, offsetX: Double, offsetY: Double): Boolean {
        val target = dragTarget ?: return super.mouseDragged(click, offsetX, offsetY)
        if (click.buttonInfo().button() != 0) return super.mouseDragged(click, offsetX, offsetY)

        val lines = if (target.element.isEnabled()) {
            val live = target.element.getLines()
            if (live.isNotEmpty()) live else target.element.getSampleData()
        } else target.element.getSampleData()
        val (panelWidth, panelHeight) = HudManager.getPanelSize(lines)
        val totalHeight = TITLE_HEIGHT + panelHeight

        var newX = (click.x() - dragOffsetX).toInt()
        var newY = (click.y() - dragOffsetY).toInt()

        // Clamp to screen
        newX = newX.coerceIn(0, width - panelWidth)
        newY = newY.coerceIn(TITLE_HEIGHT, height - panelHeight)

        // Edge snapping (unless Shift is held)
        val window = MinecraftClient.getInstance().window.handle
        val shiftHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS

        if (!shiftHeld) {
            if (newX < SNAP_DISTANCE) newX = 0
            else if (newX + panelWidth > width - SNAP_DISTANCE) newX = width - panelWidth

            if (newY < SNAP_DISTANCE + TITLE_HEIGHT) newY = TITLE_HEIGHT
            else if (newY + panelHeight > height - SNAP_DISTANCE) newY = height - panelHeight

            // Snap to other elements
            for (other in elementStates) {
                if (other == target) continue
                // Snap X to other element's X
                if (kotlin.math.abs(newX - other.x) < SNAP_DISTANCE) newX = other.x
            }
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
        // Save all positions
        for (state in elementStates) {
            HudPositions.setPosition(state.element.id, state.x, state.y)
        }
        HudPositions.save()
        super.close()
    }

    override fun shouldPause(): Boolean = false

    private fun toggleElement(element: HudElement) {
        // Toggle the config field for this element
        when (element.id) {
            "location" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showLocation = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showLocation
            "coinPurse" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showPurse = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showPurse
            "bits" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showBits = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showBits
            "skillProgress" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showSkillProgress = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showSkillProgress
            "slayer" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showSlayer = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showSlayer
            "mayor" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showMayor = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showMayor
            "pet" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showPet = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showPet
            "cookie_timer" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showCookieTimer = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showCookieTimer
            "sb_time" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showSbTime = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showSbTime
            "ping" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showPing = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showPing
            "tps" -> com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showTps = !com.ninjagoldfinch.nz.ninja_utils.config.HudCategory.showTps
            "debug_overlay" -> com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory.debugOverlay = !com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory.debugOverlay
        }
    }
}
