/*
 * This file is part of SolidAnchor
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * SolidAnchor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SolidAnchor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SolidAnchor. If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.eedcd.solidanchor.ui.clickgui

import com.github.eedcd.solidanchor.config.ConfigSystem
import com.github.eedcd.solidanchor.config.types.RangedValue
import com.github.eedcd.solidanchor.config.types.Value
import com.github.eedcd.solidanchor.config.types.ValueType
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.config.types.group.ValueGroup
import com.github.eedcd.solidanchor.config.types.list.ChoiceListValue
import com.github.eedcd.solidanchor.config.types.list.MultiChoiceListValue
import com.github.eedcd.solidanchor.config.types.list.Tagged
import com.github.eedcd.solidanchor.features.global.GlobalManager
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.ModuleCategory
import com.github.eedcd.solidanchor.features.module.ModuleManager
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.render.getBounds
import com.github.eedcd.solidanchor.ui.hud.HudEditorScreen
import com.github.eedcd.solidanchor.utils.client.mc
import com.github.eedcd.solidanchor.utils.input.InputBind
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import java.io.File
import kotlin.math.roundToInt

/**
 * Native module menu. Everything is drawn with the vanilla GUI renderer.
 *
 * With [settingsOnly] set, the screen shows nothing but the settings panel for [initialSettings],
 * which is used for standalone editors such as the spoofer settings.
 */
class ClickGuiScreen(
    private val parent: Screen? = null,
    private val settingsOnly: Boolean = false,
    private val initialSettings: ValueGroup? = null,
    private val initialSettingsTitle: String = "",
) : Screen(Component.literal(if (settingsOnly) initialSettingsTitle else "SolidAnchor")) {

    private data class Panel(
        val category: ModuleCategory,
        var x: Float = 0F,
        var y: Float = 0F,
        var scroll: Float = 0F,
        var collapsed: Boolean = true,
    )

    /** One independently movable module detail window; several of them can be open at once. */
    private class SettingsWindow(
        var group: ValueGroup,
        var title: String,
        var x: Float,
        var y: Float,
        var scroll: Float = 0F,
    )

    private class PanelDrag(
        val panel: Panel,
        val startMouseX: Double,
        val startMouseY: Double,
        val startX: Float,
        val startY: Float,
    )

    private class HitArea(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val left: () -> Unit,
        val right: (() -> Unit)? = null,
    ) {
        fun contains(x: Double, y: Double) = x >= x1 && x <= x2 && y >= y1 && y <= y2
    }

    private class SliderDrag(
        val value: Value<*>,
        val startX: Float,
        val endX: Float,
        val min: Double,
        val max: Double,
        val secondHandle: Boolean,
        val colorChannel: Int? = null,
    )

    private class SettingsDrag(
        val window: SettingsWindow,
        val startMouseX: Double,
        val startMouseY: Double,
        val startX: Float,
        val startY: Float,
    )

    private val panels = ModuleCategories.entries.map { Panel(it) }.toMutableList()

    private val hits = ArrayList<HitArea>()

    private val settingsWindows = ArrayList<SettingsWindow>()
    private var layoutLoaded = false

    private var drag: SliderDrag? = null
    private var panelDrag: PanelDrag? = null
    private var settingsDrag: SettingsDrag? = null

    private var openDropdown: Value<*>? = null

    /** A dropdown that is drawn as a floating overlay, so it never shifts the settings layout. */
    private class PendingDropdown(
        val x: Float,
        val y: Float,
        val w: Float,
        val options: List<String>,
        val selected: String?,
        val checked: Set<String>? = null,
        val onSelect: (String) -> Unit,
    )

    private val pendingDropdowns = ArrayList<PendingDropdown>()
    private val dropdownBounds = ArrayList<FloatArray>()
    private var dropdownScroll = 0F

    private val panelModuleCache = HashMap<ModuleCategory, List<ClientModule>>()
    private val moduleLabelCache = HashMap<ClientModule, Component>()

    private var tooltipText: String? = null
    private var tooltipX = 0
    private var tooltipY = 0
    private var tooltipPanel: Panel? = null
    private var hoveredPanel: Panel? = null

    private var editingText: Value<*>? = null
    private var textBuffer = ""
    private var bindingValue: Value<*>? = null

    private val expandedColors = HashSet<Value<*>>()

    override fun init() {
        if (settingsOnly) {
            layoutLoaded = true
            val group = initialSettings
            if (group != null) {
                val window = SettingsWindow(group, initialSettingsTitle, 0F, 40F)
                window.x = ((width - settingsWidth(window)) / 2F).coerceIn(4F, (width - 4F).coerceAtLeast(4F))
                settingsWindows += window
            }
            return
        }

        if (!layoutLoaded) {
            layoutLoaded = true
            // Restore the positions from the last session, otherwise compute a fresh layout.
            if (!restoreLayout()) {
                layoutPanels()
            }
        }

        // Keep every panel reachable even if the window got smaller.
        for (panel in panels) {
            panel.x = panel.x.coerceIn(0F, (width - PANEL_WIDTH).coerceAtLeast(0F))
            panel.y = panel.y.coerceIn(0F, (height - HEADER_HEIGHT).coerceAtLeast(0F))
        }

        for (window in settingsWindows) {
            clampSettingsWindow(window)
        }
    }

    private fun layoutPanels() {
        val gap = 6F
        val startX = 10F
        val startY = 34F
        var x = startX
        var y = startY

        // Layout keeps the original category order even after a panel was brought to the front.
        for (panel in panels.sortedBy { ModuleCategories.entries.indexOf(it.category) }) {
            val panelHeight = panelHeight(panel, panelModules(panel))

            // Start a new column when the current one is used up and there is still space to the right.
            if (y > startY && y + panelHeight > height - 10) {
                x += PANEL_WIDTH + gap
                y = startY
            }

            // Keep the panel on screen if there is not enough room for another column.
            if (x + PANEL_WIDTH > width - 10) {
                x = (width - PANEL_WIDTH - 10F).coerceAtLeast(startX)
            }

            panel.x = x
            panel.y = y
            y += panelHeight + gap
        }
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        hits.clear()
        tooltipText = null
        pendingDropdowns.clear()
        dropdownBounds.clear()

        // Only the topmost layer under the mouse may show a tooltip, otherwise the description
        // of a module in a panel behind another panel would be drawn through it.
        val overTopButton = mouseX.toDouble() in (width - 176F).toDouble()..(width - 10F).toDouble() &&
            mouseY.toDouble() in 8.0..24.0

        val topPanel = if (settingsOnly || overTopButton || isInsideAnySettings(mouseX.toDouble(), mouseY.toDouble())) {
            null
        } else {
            panels.lastOrNull { panel ->
                val panelBottom = panel.y + panelHeight(panel, panelModules(panel))
                mouseX.toDouble() in panel.x.toDouble()..(panel.x + PANEL_WIDTH).toDouble() &&
                    mouseY.toDouble() in panel.y.toDouble()..panelBottom.toDouble()
            }
        }

        // Only the topmost panel may highlight a row, otherwise the highlight of a panel that is
        // covered by another (semi-transparent) one would shine through it.
        hoveredPanel = topPanel
        tooltipPanel = topPanel

        context.fill(0, 0, width, height, BACKGROUND)

        if (!settingsOnly) {
            context.centeredText(font, "SolidAnchor", width / 2, 10, ACCENT)

            context.centeredText(
                font,
                "Right click a module to configure it",
                width / 2,
                22,
                HINT,
            )

            for (panel in panels) {
                drawPanel(context, panel, mouseX.toDouble(), mouseY.toDouble())
            }

            drawTopButtons(context, mouseX.toDouble(), mouseY.toDouble())
        } else {
            context.centeredText(font, initialSettingsTitle, width / 2, 10, ACCENT)
        }

        for (window in settingsWindows) {
            drawSettings(context, window, mouseX.toDouble(), mouseY.toDouble())
        }

        // Dropdowns are drawn last as floating overlays: they must not shift the settings layout
        // and must not be clipped by the settings panel scissor.
        drawPendingDropdowns(context, mouseX.toDouble(), mouseY.toDouble())

        if (bindingValue != null) {
            context.centeredText(font, "Press a key or mouse button", width / 2, height - 20, ACCENT)
        }

        drawTooltip(context)

        super.extractRenderState(context, mouseX, mouseY, partialTick)
    }

    // ---------------------------------------------------------------- panels

    private fun panelModules(panel: Panel): List<ClientModule> =
        panelModuleCache.getOrPut(panel.category) {
            ModuleManager.filter { it.category == panel.category }.sortedBy { it.name }
        }

    private fun moduleLabel(module: ClientModule): Component =
        moduleLabelCache.getOrPut(module) {
            val tag = module.tag
            Component.literal(if (tag != null && tag.isNotBlank()) "${module.name} ($tag)" else module.name)
        }

    private fun panelContentHeight(modules: List<ClientModule>): Float = modules.size * ROW_HEIGHT + 6F

    private fun panelHeight(panel: Panel, modules: List<ClientModule>): Float =
        if (panel.collapsed) {
            HEADER_HEIGHT
        } else {
            val maxContent = (MAX_PANEL_HEIGHT - HEADER_HEIGHT)
                .coerceAtMost(height - 60F)
                .coerceAtLeast(ROW_HEIGHT * 3F)
            HEADER_HEIGHT + minOf(panelContentHeight(modules), maxContent)
        }

    private fun maxPanelScroll(panel: Panel, modules: List<ClientModule>): Float =
        (panelContentHeight(modules) - (panelHeight(panel, modules) - HEADER_HEIGHT)).coerceAtLeast(0F)

    private fun drawPanel(context: GuiGraphicsExtractor, panel: Panel, mouseX: Double, mouseY: Double) {
        val x = panel.x
        val y = panel.y
        val w = PANEL_WIDTH

        val modules = panelModules(panel)
        val contentHeight = panelContentHeight(modules)
        val h = panelHeight(panel, modules)

        val left = x.toInt()
        val top = y.toInt()
        val right = (x + w).toInt()
        val bottom = (y + h).toInt()
        val headerBottom = (y + HEADER_HEIGHT).toInt()

        // A single offset rectangle is used for the shadow; rounded corners were removed.
        context.fill(left + 2, top + 2, right + 3, bottom + 3, SHADOW)
        context.fill(left, top, right, bottom, PANEL_BG)
        context.fill(left, top, right, headerBottom, PANEL_HEADER)
        context.fill(left + 1, headerBottom - 1, right - 1, headerBottom, OUTLINE)
        drawOutline(context, left, top, right, bottom, OUTLINE)
        context.text(font, panel.category.tag, (x + 6).toInt(), (y + 4).toInt(), ACCENT, false)

        // Collapse / expand button
        context.text(
            font,
            if (panel.collapsed) "▼" else "▲",
            (x + w - 10).toInt(),
            (y + 4).toInt(),
            TEXT_OFF,
            false,
        )
        hits += HitArea(x + w - 14, y, x + w, y + HEADER_HEIGHT, {
            focusPanel(panel)
            panel.collapsed = !panel.collapsed
            if (panel.collapsed) {
                panel.scroll = 0F
            }
            saveLayout()
        })

        // Dragging the header moves the whole panel
        hits += HitArea(x, y, x + w - 14, y + HEADER_HEIGHT, {
            focusPanel(panel)
            panelDrag = PanelDrag(panel, mouseX, mouseY, panel.x, panel.y)
        })

        if (panel.collapsed) {
            return
        }

        // scroll bounds
        val maxScroll = maxPanelScroll(panel, modules)
        panel.scroll = panel.scroll.coerceIn(0F, maxScroll)

        val clipTop = y + HEADER_HEIGHT
        val clipBottom = y + h

        // Swallow clicks on the empty parts of the panel, so they cannot reach a module row of
        // a panel that is covered by this one.
        hits += HitArea(x, clipTop, x + w, clipBottom, { focusPanel(panel) })

        context.scissorStack.push(context.getBounds(x, clipTop, x + w, clipBottom))

        var rowY = clipTop + 3F - panel.scroll
        for (module in modules) {
            // Only rows that are at least partially inside the viewport are drawn and interactive,
            // otherwise hidden rows keep invisible hit areas that block the panel header.
            if (rowY + ROW_HEIGHT > clipTop && rowY < clipBottom) {
                drawModuleRow(context, panel, module, x, rowY, w, mouseX, mouseY)
            }
            rowY += ROW_HEIGHT
        }

        context.scissorStack.pop()

        if (maxScroll > 0F) {
            val trackH = h - HEADER_HEIGHT - 4F
            val thumbH = (trackH * (h / contentHeight)).coerceAtLeast(8F)
            val thumbY = clipTop + 2F + (trackH - thumbH) * (panel.scroll / maxScroll)
            context.fill((x + w - 4).toInt(), thumbY.toInt(), (x + w - 2).toInt(), (thumbY + thumbH).toInt(), SCROLLBAR)
        }
    }

    private fun drawModuleRow(
        context: GuiGraphicsExtractor,
        panel: Panel,
        module: ClientModule,
        x: Float,
        y: Float,
        w: Float,
        mouseX: Double,
        mouseY: Double,
    ) {
        val enabled = module.enabled
        val hovered = panel === hoveredPanel && !isInsideAnySettings(mouseX, mouseY) &&
            mouseX in x.toDouble()..(x + w).toDouble() && mouseY in y.toDouble()..(y + ROW_HEIGHT - 1).toDouble()

        if (hovered) {
            context.fill((x + 2).toInt(), y.toInt(), (x + w - 2).toInt(), (y + ROW_HEIGHT - 1).toInt(), ROW_HOVER)
            if (panel === tooltipPanel) {
                tooltipText = module.description.get()?.takeIf { it.isNotBlank() && !it.startsWith("solidanchor.") }
                tooltipX = mouseX.toInt()
                tooltipY = mouseY.toInt()
            }
        }

        context.fill((x + 5).toInt(), (y + 4).toInt(), (x + 11).toInt(), (y + 10).toInt(),
            if (enabled) ACCENT else OFF)

        val color = if (enabled) TEXT_ON else TEXT_OFF
        context.text(font, moduleLabel(module), (x + 15).toInt(), y.toInt() + 2, color, false)

        hits += HitArea(x, y, x + w, y + ROW_HEIGHT - 1, {
            focusPanel(panel)
            if (!module.notActivatable) {
                module.enabled = !module.enabled
            }
        }, {
            focusPanel(panel)
            openSettingsWindow(module, panel)
            openDropdown = null
            editingText = null
        })
    }

    private fun drawTopButtons(context: GuiGraphicsExtractor, mouseX: Double, mouseY: Double) {
        var right = width - 10F

        // Settings opens the global client settings (language, commands, targets, ...).
        val settingsW = 64F
        drawTopButton(context, "Settings", right - settingsW, settingsW, mouseX, mouseY) {
            openGlobalSettings()
        }
        right -= settingsW + 6F

        // HUD component size calculation requires a live player, so the editor is only available in-game.
        if (mc.player == null || mc.level == null) {
            return
        }

        val hudW = 90F
        drawTopButton(context, "HUD Editor", right - hudW, hudW, mouseX, mouseY) {
            mc.gui.setScreen(HudEditorScreen(this))
        }
    }

    private fun drawTopButton(
        context: GuiGraphicsExtractor,
        label: String,
        x: Float,
        w: Float,
        mouseX: Double,
        mouseY: Double,
        onClick: () -> Unit,
    ) {
        val y = 8F
        val hovered = mouseX in x.toDouble()..(x + w).toDouble() && mouseY in y.toDouble()..(y + 16).toDouble()

        context.fill(
            x.toInt(), y.toInt(), (x + w).toInt(), (y + 16).toInt(),
            if (hovered) PANEL_HEADER else PANEL_BG,
        )
        drawOutline(context, x.toInt(), y.toInt(), (x + w).toInt(), (y + 16).toInt(), OUTLINE)
        context.centeredText(font, label, (x + w / 2).toInt(), (y + 4).toInt(), TEXT_ON)

        hits += HitArea(x, y, x + w, y + 16, onClick)
    }

    private fun openGlobalSettings() {
        openDropdown = null
        editingText = null
        openSettingsWindow(GlobalManager, null)
    }

    /**
     * Opens a detail window for [group], or brings the existing one to the front. Windows cascade
     * so opening several modules at once does not stack them exactly on top of each other.
     */
    private fun openSettingsWindow(group: ValueGroup, sourcePanel: Panel?) {
        val existing = settingsWindows.firstOrNull { it.group === group }
        if (existing != null) {
            focusSettings(existing)
            return
        }

        val title = if (group === GlobalManager) "Global Settings" else group.name
        val window = SettingsWindow(group, title, 0F, 0F)
        val w = settingsWidth(window)

        val baseX = if (sourcePanel == null) {
            (width - w) / 2F
        } else if (sourcePanel.x + PANEL_WIDTH + w + 12 < width) {
            sourcePanel.x + PANEL_WIDTH + 6
        } else {
            (sourcePanel.x - w - 6).coerceAtLeast(4F)
        }
        val baseY = sourcePanel?.let { maxOf(it.y, 34F) } ?: 40F

        val offset = settingsWindows.size * 14F
        window.x = (baseX + offset).coerceIn(0F, (width - w).coerceAtLeast(0F))
        window.y = (baseY + offset).coerceIn(0F, (height - 40F).coerceAtLeast(0F))

        settingsWindows += window
        clampSettingsWindow(window)
    }

    /** Brings a detail window to the front so overlapping windows stay usable. */
    private fun focusSettings(window: SettingsWindow) {
        if (settingsWindows.lastOrNull() === window) {
            return
        }
        settingsWindows.remove(window)
        settingsWindows += window
    }

    private fun clampSettingsWindow(window: SettingsWindow) {
        window.x = window.x.coerceIn(0F, (width - settingsWidth(window)).coerceAtLeast(0F))
        window.y = window.y.coerceIn(0F, (height - 40F).coerceAtLeast(0F))
    }

    private fun drawTooltip(context: GuiGraphicsExtractor) {
        val text = tooltipText ?: return

        val lines = wrapText(text, TOOLTIP_WIDTH)
        val w = lines.maxOf { font.width(it) } + 8
        val h = lines.size * 10 + 6

        val x = tooltipX.coerceAtMost(width - w - 4).coerceAtLeast(4)
        val y = tooltipY.coerceAtMost(height - h - 4).coerceAtLeast(4)

        context.fill(x + 2, y + 2, x + w + 3, y + h + 3, SHADOW)
        context.fill(x, y, x + w, y + h, PANEL_BG)
        context.fill(x + 1, y, x + w - 1, y + 1, ACCENT)
        lines.forEachIndexed { index, line ->
            context.text(font, line, x + 4, y + 4 + index * 10, TEXT_ON, false)
        }
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        val lines = ArrayList<String>()
        val words = text.split(' ')

        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (font.width(candidate) > maxWidth && current.isNotEmpty()) {
                lines += current.toString()
                current = StringBuilder(word)
            } else {
                current = StringBuilder(candidate)
            }
        }

        if (current.isNotEmpty()) {
            lines += current.toString()
        }

        return lines.ifEmpty { listOf(text) }
    }

    // -------------------------------------------------------------- settings

    private fun settingsWidth(window: SettingsWindow): Float {
        val preferred = when {
            window.group === GlobalManager -> GLOBAL_SETTINGS_WIDTH
            settingsOnly -> 260F
            else -> SETTINGS_WIDTH
        }
        return minOf(preferred, (width - 16F).toFloat()).coerceAtLeast(SETTINGS_WIDTH)
    }

    private fun settingsHeight(window: SettingsWindow): Float {
        val maxHeight = when {
            settingsOnly -> height - window.y - 10F
            window.group === GlobalManager -> height - 60F
            else -> 260F
        }
        return minOf((height - window.y - 10).toFloat(), maxHeight)
    }

    private fun isInsideAnySettings(x: Double, y: Double): Boolean =
        settingsWindows.any { window ->
            x >= window.x && x <= window.x + settingsWidth(window) &&
                y >= window.y && y <= window.y + settingsHeight(window)
        }

    private fun drawSettings(
        context: GuiGraphicsExtractor,
        window: SettingsWindow,
        mouseX: Double,
        mouseY: Double,
    ) {
        val x = window.x
        val y = window.y
        val w = settingsWidth(window)
        val h = settingsHeight(window)
        val hitStart = hits.size

        val left = x.toInt()
        val top = y.toInt()
        val right = (x + w).toInt()
        val bottom = (y + h).toInt()
        val headerBottom = (y + HEADER_HEIGHT).toInt()

        // A single offset rectangle is used for the shadow; rounded corners were removed.
        context.fill(left + 2, top + 2, right + 3, bottom + 3, SHADOW)
        context.fill(left, top, right, bottom, PANEL_BG)
        context.fill(left, top, right, headerBottom, PANEL_HEADER)
        context.fill(left + 1, headerBottom - 1, right - 1, headerBottom, OUTLINE)
        drawOutline(context, left, top, right, bottom, OUTLINE)
        context.text(font, window.title, (x + 6).toInt(), (y + 4).toInt(), ACCENT, false)

        // Swallow clicks on the empty parts of the settings panel so they do not reach the
        // modules of a panel that happens to be underneath it.
        hits += HitArea(x, y, x + w, y + h, {
            openDropdown = null
            focusSettings(window)
        })

        val closeX = x + w - 14

        // Dragging the header moves this window. Bringing it to the front is deferred to the
        // next frame, otherwise the hit order of the current frame would no longer match.
        hits += HitArea(x, y, closeX, y + HEADER_HEIGHT, {
            focusSettings(window)
            settingsDrag = SettingsDrag(window, mouseX, mouseY, window.x, window.y)
        })

        context.text(font, "x", closeX.toInt(), (y + 4).toInt(), TEXT_OFF, false)
        hits += HitArea(closeX, y, x + w, y + HEADER_HEIGHT, {
            openDropdown = null
            editingText = null
            if (settingsOnly) {
                mc.gui.setScreen(parent)
            } else {
                settingsWindows.remove(window)
            }
        })

        context.scissorStack.push(context.getBounds(x, y + HEADER_HEIGHT, x + w, y + h))

        val values = collectSettings(window.group)
        var cursorY = y + HEADER_HEIGHT + 4F - window.scroll
        val viewTop = y + HEADER_HEIGHT
        val viewBottom = y + h

        for (value in values) {
            val height = valueHeight(value)
            // Rows outside the viewport are not drawn at all: they would still submit text and
            // rectangle quads (clipped by the scissor) and register hit areas every frame.
            if (cursorY + height > viewTop && cursorY < viewBottom) {
                drawValue(context, value, x + 4F, cursorY, w - 8F, mouseX, mouseY)
            }
            cursorY += height
        }

        val contentHeight = cursorY - (y + HEADER_HEIGHT + 4F) + window.scroll
        val viewport = h - HEADER_HEIGHT
        val maxScroll = (contentHeight - viewport).coerceAtLeast(0F)
        window.scroll = window.scroll.coerceIn(0F, maxScroll)

        // Settings rows that are scrolled out of the panel still register hit areas while they
        // are drawn. Drop those so they cannot be clicked through another panel below the popup.
        hits.subList(hitStart, hits.size).removeIf { hit ->
            hit.x2 < x || hit.x1 > x + w || hit.y2 < y || hit.y1 > y + h
        }

        context.scissorStack.pop()
    }

    private fun collectSettings(group: ValueGroup): List<Value<*>> {
        val result = ArrayList<Value<*>>()

        fun walk(current: ValueGroup) {
            for (value in current.inner) {
                if (value.notAnOption || !value.visibleCondition.asBoolean) {
                    continue
                }
                when (value) {
                    is ModeValueGroup<*> -> {
                        result += value
                        walk(value.activeMode)
                    }
                    is ToggleableValueGroup -> {
                        result += value
                        if (value.enabled) {
                            walk(value)
                        }
                    }
                    is ValueGroup -> {
                        result += value
                        walk(value)
                    }
                    else -> result += value
                }
            }
        }

        walk(group)
        return result
    }

    /**
     * Height a settings row consumes. Mirrors the values returned by [drawValue] so rows outside
     * the viewport can be skipped without drawing them first.
     */
    private fun valueHeight(value: Value<*>): Float = when {
        value is ModeValueGroup<*> || value is ToggleableValueGroup -> ROW_HEIGHT + 2F
        value.valueType == ValueType.FLOAT || value.valueType == ValueType.INT ||
            value.valueType == ValueType.FLOAT_RANGE || value.valueType == ValueType.INT_RANGE -> ROW_HEIGHT + 6F
        value.valueType == ValueType.COLOR ->
            ROW_HEIGHT + 2F + if (value in expandedColors) 50F else 0F
        value.valueType == ValueType.CONFIGURABLE || value.valueType == ValueType.CHOICE -> ROW_HEIGHT
        else -> ROW_HEIGHT + 2F
    }

    private fun drawValue(
        context: GuiGraphicsExtractor,
        value: Value<*>,
        x: Float,
        y: Float,
        w: Float,
        mouseX: Double,
        mouseY: Double,
    ): Float {
        when (value) {
            is ModeValueGroup<*> -> {
                drawChoiceRow(context, value, x, y, w, "${value.name}: ${value.activeMode.name} ▾")
                hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { toggleDropdown(value) })

                if (openDropdown === value) {
                    pendingDropdowns += PendingDropdown(
                        x, y + ROW_HEIGHT + 2F, w,
                        value.modes.map { it.tag },
                        value.activeMode.tag,
                    ) { tag ->
                        value.setByString(tag)
                        openDropdown = null
                    }
                }

                return ROW_HEIGHT + 2F
            }

            is ToggleableValueGroup -> {
                drawCheckRow(context, value, x, y, w, value.name, value.enabled)
                hits += HitArea(x, y, x + w, y + ROW_HEIGHT, {
                    value.enabled = !value.enabled
                })
                return ROW_HEIGHT + 2F
            }

            else -> Unit
        }

        return when (value.valueType) {
            ValueType.BOOLEAN -> {
                val current = value.get() as? Boolean ?: false
                drawCheckRow(context, value, x, y, w, value.name, current)
                hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { setRaw(value, !current) })
                ROW_HEIGHT + 2F
            }

            ValueType.FLOAT, ValueType.INT, ValueType.FLOAT_RANGE, ValueType.INT_RANGE -> {
                drawSliderValue(context, value, x, y, w, mouseX, mouseY)
            }

            ValueType.TEXT -> {
                drawTextRow(context, value, x, y, w)
                ROW_HEIGHT + 2F
            }

            ValueType.COLOR -> drawColorValue(context, value, x, y, w)

            ValueType.BIND, ValueType.KEY -> {
                val bind = value.get() as? InputBind

                if (bind == null) {
                    val key = value.get()
                    val text = if (key is InputConstants.Key) key.displayName.string else key.toString()
                    drawChoiceRow(context, value, x, y, w, "${value.name}: $text")
                    hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { bindingValue = value })
                    return ROW_HEIGHT + 2F
                }

                val keyText = if (bind.isUnbound) "None" else bind.boundKey.displayName.string
                val actionX = x + w * 0.55F

                context.text(font, "${value.name}: $keyText", x.toInt(), y.toInt() + 2, TEXT_ON, false)
                context.fill(actionX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + ROW_HEIGHT - 2).toInt(), FIELD_BG)
                context.text(font, "${bind.action.tag} ▾", (actionX + 4).toInt(), y.toInt() + 2, ACCENT, false)

                // Clicking the key part starts rebinding, the action box opens the action dropdown.
                hits += HitArea(x, y, actionX - 2, y + ROW_HEIGHT, { bindingValue = value })
                hits += HitArea(actionX, y, x + w, y + ROW_HEIGHT, { toggleDropdown(value) })

                if (openDropdown === value) {
                    pendingDropdowns += PendingDropdown(
                        actionX, y + ROW_HEIGHT + 2F, x + w - actionX,
                        InputBind.BindAction.entries.map { it.tag } + UNBIND_ACTION,
                        bind.action.tag,
                    ) { tag ->
                        if (tag == UNBIND_ACTION) {
                            setRaw(value, bind.copy(boundKey = InputConstants.UNKNOWN))
                        } else {
                            InputBind.BindAction.of(tag)?.let { setRaw(value, bind.copy(action = it)) }
                        }
                        openDropdown = null
                    }
                }

                return ROW_HEIGHT + 2F
            }

            ValueType.CHOOSE -> drawChoiceValue(context, value, x, y, w, mouseX, mouseY)

            ValueType.MULTI_CHOOSE -> drawMultiChoiceValue(context, value, x, y, w)

            ValueType.CONFIGURABLE, ValueType.CHOICE -> {
                context.text(font, value.name, x.toInt(), y.toInt(), GROUP_HEADER, false)
                ROW_HEIGHT
            }

            else -> {
                drawChoiceRow(context, value, x, y, w, "${value.name}: ${value.getValue()}")
                ROW_HEIGHT + 2F
            }
        }
    }

    private fun drawCheckRow(
        context: GuiGraphicsExtractor,
        value: Value<*>,
        x: Float,
        y: Float,
        w: Float,
        name: String,
        checked: Boolean,
    ) {
        context.fill((x + 1).toInt(), (y + 3).toInt(), (x + 8).toInt(), (y + 10).toInt(), if (checked) ACCENT else OFF)
        context.text(font, name, (x + 12).toInt(), y.toInt() + 2, if (checked) TEXT_ON else TEXT_OFF, false)
    }

    private fun drawChoiceRow(
        context: GuiGraphicsExtractor,
        value: Value<*>,
        x: Float,
        y: Float,
        w: Float,
        label: String,
    ) {
        context.fill(x.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(), FIELD_BG)
        context.text(font, label, (x + 3).toInt(), y.toInt() + 2, TEXT_ON, false)
    }

    private fun drawChoiceValue(
        context: GuiGraphicsExtractor,
        value: Value<*>,
        x: Float,
        y: Float,
        w: Float,
        mouseX: Double,
        mouseY: Double,
    ): Float {
        val choice = value as? ChoiceListValue<*> ?: return ROW_HEIGHT + 2F

        drawChoiceRow(context, value, x, y, w, "${value.name}: ${choice.get().tag} ▾")
        hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { toggleDropdown(value) })

        if (openDropdown === value) {
            pendingDropdowns += PendingDropdown(
                x, y + ROW_HEIGHT + 2F, w,
                choice.choices.map { it.tag },
                choice.get().tag,
            ) { tag ->
                choice.choices.firstOrNull { it.tag == tag }?.let { setRaw(value, it) }
                openDropdown = null
            }
        }

        return ROW_HEIGHT + 2F
    }

    /**
     * Draws all open dropdowns as floating overlays above the settings panel and registers their
     * option hit areas last, so they always win over the content underneath.
     */
    private fun drawPendingDropdowns(context: GuiGraphicsExtractor, mouseX: Double, mouseY: Double) {
        for (dropdown in pendingDropdowns) {
            val contentHeight = dropdown.options.size * OPTION_HEIGHT + 6F
            val listHeight = contentHeight.coerceAtMost(OPTION_MAX_HEIGHT)
            val boxX = dropdown.x.coerceIn(4F, (width - dropdown.w - 4F).coerceAtLeast(4F))
            val boxY = if (dropdown.y + listHeight + 4F > height) {
                (dropdown.y - ROW_HEIGHT - 4F - listHeight).coerceAtLeast(4F)
            } else {
                dropdown.y
            }
            val boxH = listHeight + 4F

            val maxScroll = (contentHeight - listHeight).coerceAtLeast(0F)
            dropdownScroll = dropdownScroll.coerceIn(0F, maxScroll)

            drawPopupBox(context, boxX, boxY, dropdown.w, boxH)
            dropdownBounds += floatArrayOf(boxX, boxY, boxX + dropdown.w, boxY + boxH)

            context.scissorStack.push(context.getBounds(boxX, boxY + 2F, boxX + dropdown.w, boxY + boxH - 2F))

            var rowY = boxY + 2F - dropdownScroll
            for (option in dropdown.options) {
                val hovered = mouseX in (boxX + 3).toDouble()..(boxX + dropdown.w - 3).toDouble() &&
                    mouseY in rowY.toDouble()..(rowY + OPTION_HEIGHT).toDouble()
                val checked = dropdown.checked?.contains(option)
                val active = checked ?: (option == dropdown.selected)
                val rowBottom = (rowY + OPTION_HEIGHT).toInt()
                val middleY = (rowY + OPTION_HEIGHT / 2F).toInt()

                if (hovered) {
                    context.fill((boxX + 3).toInt(), rowY.toInt(), (boxX + dropdown.w - 3).toInt(), rowBottom, ROW_HOVER)
                }

                if (checked != null) {
                    context.fill(
                        (boxX + 8).toInt(), middleY - 3, (boxX + 14).toInt(), middleY + 3,
                        if (checked) ACCENT else OFF,
                    )
                    context.text(font, option, (boxX + 18).toInt(), (rowY + 2).toInt(), TEXT_ON, false)
                } else {
                    if (active) {
                        context.fill(
                            (boxX + 6).toInt(), middleY - 3, (boxX + 9).toInt(), middleY + 3,
                            ACCENT,
                        )
                    }
                    context.text(
                        font,
                        option,
                        (boxX + 13).toInt(),
                        (rowY + 2).toInt(),
                        if (active) ACCENT else TEXT_ON,
                        false,
                    )
                }

                // Rows scrolled out of the visible area must not keep an invisible hit area.
                if (rowY + OPTION_HEIGHT > boxY + 2F && rowY < boxY + boxH - 2F) {
                    hits += HitArea(
                        boxX + 3,
                        maxOf(rowY, boxY + 2F),
                        boxX + dropdown.w - 3,
                        minOf(rowY + OPTION_HEIGHT, boxY + boxH - 2F),
                        left = { dropdown.onSelect(option) },
                    )
                }

                rowY += OPTION_HEIGHT
            }

            context.scissorStack.pop()

            if (maxScroll > 0F) {
                val trackY = boxY + 3F
                val trackH = listHeight - 2F
                val thumbH = (trackH * (listHeight / contentHeight)).coerceAtLeast(10F)
                val thumbY = trackY + (trackH - thumbH) * (dropdownScroll / maxScroll).coerceIn(0F, 1F)
                context.fill((boxX + dropdown.w - 3F).toInt(), thumbY.toInt(), (boxX + dropdown.w - 1F).toInt(), (thumbY + thumbH).toInt(), SCROLLBAR)
            }
        }
    }

    private fun drawMultiChoiceValue(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float): Float {
        @Suppress("UNCHECKED_CAST")
        val multi = (value as? MultiChoiceListValue<*>) as? MultiChoiceListValue<Tagged> ?: return ROW_HEIGHT + 2F
        val active = multi.get()

        drawChoiceRow(context, value, x, y, w, "${value.name}: ${active.size}/${multi.choices.size} ▾")
        hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { toggleDropdown(value) })

        if (openDropdown === value) {
            pendingDropdowns += PendingDropdown(
                x, y + ROW_HEIGHT + 2F, w,
                multi.choices.map { it.tag },
                null,
                active.map { it.tag }.toSet(),
            ) { tag ->
                multi.choices.firstOrNull { it.tag == tag }?.let { multi.toggle(it) }
            }
        }

        return ROW_HEIGHT + 2F
    }

    private fun drawTextRow(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float) {
        val editing = editingText === value
        val shown = if (editing) textBuffer else value.get().toString()

        context.text(font, value.name, x.toInt(), y.toInt() + 2, TEXT_OFF, false)
        val boxX = x + w * 0.5F
        context.fill(boxX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(),
            if (editing) PANEL_HEADER else FIELD_BG)
        context.text(font, shown, (boxX + 3).toInt(), (y + 2).toInt(), TEXT_ON, false)

        hits += HitArea(boxX, y, x + w, y + ROW_HEIGHT, {
            editingText = value
            textBuffer = value.get().toString()
        })
    }

    private fun drawColorValue(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float): Float {
        val color = value.get() as? Color4b ?: Color4b.WHITE
        val expanded = value in expandedColors

        context.text(font, value.name, x.toInt(), y.toInt() + 2, TEXT_OFF, false)
        val swatchX = x + w * 0.5F
        context.fill(swatchX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(), color.argb)
        drawOutline(context, swatchX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(), OUTLINE)

        hits += HitArea(swatchX, y, x + w, y + ROW_HEIGHT, {
            if (expanded) expandedColors.remove(value) else expandedColors.add(value)
        })

        var consumed = ROW_HEIGHT + 2F

        if (expanded) {
            val levels = intArrayOf(color.r, color.g, color.b, color.a)
            for (index in levels.indices) {
                val name = COLOR_CHANNELS[index]
                val level = levels[index]
                val rowY = y + consumed
                context.text(font, name, x.toInt(), rowY.toInt() + 2, TEXT_OFF, false)

                val startX = x + 16F
                val endX = x + w
                context.fill(startX.toInt(), (rowY + 2).toInt(), endX.toInt(), (rowY + 8).toInt(), OFF)
                context.fill(startX.toInt(), (rowY + 2).toInt(),
                    (startX + (endX - startX) * (level / 255F)).toInt().coerceAtLeast(startX.toInt()),
                    (rowY + 8).toInt(), ACCENT)

                val channelIndex = index
                hits += HitArea(startX, rowY, endX, rowY + 10, {
                    editingText = null
                    drag = SliderDrag(value, startX, endX, 0.0, 255.0, false, channelIndex)
                })

                consumed += 12F
            }
            consumed += 2F
        }

        return consumed
    }

    private fun drawSliderValue(
        context: GuiGraphicsExtractor,
        value: Value<*>,
        x: Float,
        y: Float,
        w: Float,
        mouseX: Double,
        mouseY: Double,
    ): Float {
        val ranged = value as? RangedValue<*>
        val label = "${value.name}: ${formatValue(value)}"

        context.text(font, label, x.toInt(), y.toInt(), TEXT_ON, false)

        val barX = x
        val barW = w
        val barY = y + 10F
        val range = ranged?.range

        context.fill(barX.toInt(), (barY + 1).toInt(), (barX + barW).toInt(), (barY + 4).toInt(), OFF)

        if (range == null) {
            return ROW_HEIGHT + 2F
        }

        val min = (range.start as Number).toDouble()
        val max = (range.endInclusive as Number).toDouble()
        val span = (max - min).coerceAtLeast(1.0)

        when (val current = value.get()) {
            is ClosedFloatingPointRange<*> -> {
                val first = current.start as Float
                val second = current.endInclusive as Float
                val firstX = barX + barW * ((first - min) / span).toFloat()
                val secondX = barX + barW * ((second - min) / span).toFloat()
                context.fill(firstX.toInt(), barY.toInt(), secondX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(firstX.toInt(), (barY - 2).toInt(), (firstX + 3).toInt(), (barY + 7).toInt(), TEXT_ON)
                context.fill(secondX.toInt(), (barY - 2).toInt(), (secondX + 3).toInt(), (barY + 7).toInt(), TEXT_ON)

                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    val pickSecond = mouseX > (firstX + secondX) / 2
                    drag = SliderDrag(value, barX, barX + barW, min, max, pickSecond)
                })
            }

            is IntRange -> {
                val firstX = barX + barW * ((current.first - min) / span).toFloat()
                val secondX = barX + barW * ((current.last - min) / span).toFloat()
                context.fill(firstX.toInt(), barY.toInt(), secondX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(firstX.toInt(), (barY - 2).toInt(), (firstX + 3).toInt(), (barY + 7).toInt(), TEXT_ON)
                context.fill(secondX.toInt(), (barY - 2).toInt(), (secondX + 3).toInt(), (barY + 7).toInt(), TEXT_ON)

                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    val pickSecond = mouseX > (firstX + secondX) / 2
                    drag = SliderDrag(value, barX, barX + barW, min, max, pickSecond)
                })
            }

            else -> {
                val number = (current as? Number)?.toDouble() ?: 0.0
                val handleX = barX + barW * ((number - min) / span).toFloat()
                context.fill(barX.toInt(), barY.toInt(), handleX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(handleX.toInt(), (barY - 2).toInt(), (handleX + 3).toInt(), (barY + 7).toInt(), TEXT_ON)

                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    drag = SliderDrag(value, barX, barX + barW, min, max, false)
                })
            }
        }

        return ROW_HEIGHT + 6F
    }

    private fun formatValue(value: Value<*>): String = when (val current = value.get()) {
        is ClosedFloatingPointRange<*> -> String.format("%.2f..%.2f", (current.start as Float), (current.endInclusive as Float))
        is IntRange -> "${current.first}..${current.last}"
        is Float -> String.format("%.2f", current)
        else -> current.toString()
    }

    // ------------------------------------------------------------- input

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        if (drag != null || panelDrag != null || settingsDrag != null) {
            return true
        }

        val x = click.x
        val y = click.y
        val button = click.button()

        // A click outside the settings popup and outside an open dropdown overlay closes it.
        // Clicks inside the overlay are kept, otherwise every option of a multi choice would close it.
        if (!isInsideAnySettings(x, y) && !isInsideAnyDropdown(x, y)) {
            openDropdown = null
        }

        for (index in hits.indices.reversed()) {
            val hit = hits[index]
            if (!hit.contains(x, y)) {
                continue
            }

            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                hit.right?.invoke()
            } else {
                hit.left()
            }
            return true
        }

        return super.mouseClicked(click, doubled)
    }

    override fun mouseDragged(click: MouseButtonEvent, offsetX: Double, offsetY: Double): Boolean {
        settingsDrag?.let { active ->
            active.window.x = (active.startX + (click.x - active.startMouseX).toFloat())
                .coerceIn(0F, (width - settingsWidth(active.window)).coerceAtLeast(0F))
            active.window.y = (active.startY + (click.y - active.startMouseY).toFloat())
                .coerceIn(0F, (height - 40F).coerceAtLeast(0F))
            return true
        }

        panelDrag?.let { active ->
            active.panel.x = active.startX + (click.x - active.startMouseX).toFloat()
            active.panel.y = active.startY + (click.y - active.startMouseY).toFloat()
            return true
        }

        val active = drag ?: return super.mouseDragged(click, offsetX, offsetY)

        val ratio = ((click.x - active.startX) / (active.endX - active.startX)).coerceIn(0.0, 1.0)
        val newValue = active.min + (active.max - active.min) * ratio

        @Suppress("UNCHECKED_CAST")
        val value = active.value as Value<Any>

        val channel = active.colorChannel
        if (channel != null) {
            val color = value.get() as? Color4b ?: return true
            val level = newValue.roundToInt().coerceIn(0, 255)
            val updated = when (channel) {
                0 -> Color4b(level, color.g, color.b, color.a)
                1 -> Color4b(color.r, level, color.b, color.a)
                2 -> Color4b(color.r, color.g, level, color.a)
                else -> Color4b(color.r, color.g, color.b, level)
            }
            value.set(updated as Any)
            return true
        }

        when (val current = value.get()) {
            is ClosedFloatingPointRange<*> -> {
                val first = (current.start as Float)
                val second = (current.endInclusive as Float)
                if (active.secondHandle) {
                    value.set((first..newValue.toFloat().coerceAtLeast(first)) as Any)
                } else {
                    value.set((newValue.toFloat().coerceAtMost(second)..second) as Any)
                }
            }
            is IntRange -> {
                val first = current.first
                val second = current.last
                if (active.secondHandle) {
                    value.set((first..newValue.roundToInt().coerceAtLeast(first)) as Any)
                } else {
                    value.set((newValue.roundToInt().coerceAtMost(second)..second) as Any)
                }
            }
            is Int -> value.set(newValue.roundToInt() as Any)
            is Float -> value.set(newValue.toFloat() as Any)
            is Long -> value.set(newValue.toLong() as Any)
            is Double -> value.set(newValue as Any)
            else -> Unit
        }

        return true
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        if (settingsDrag != null) {
            settingsDrag = null
            if (!settingsOnly) {
                saveLayout()
            }
            return true
        }
        if (panelDrag != null) {
            panelDrag = null
            saveLayout()
            return true
        }
        if (drag != null) {
            drag = null
            return true
        }
        return super.mouseReleased(click)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        // An open dropdown floats above everything else, so it gets the scroll first.
        if (isInsideAnyDropdown(mouseX, mouseY)) {
            dropdownScroll = (dropdownScroll - verticalAmount.toFloat() * 8F).coerceAtLeast(0F)
            return true
        }

        // The settings windows are drawn on top of the panels, so they have to consume the scroll
        // first, otherwise a panel underneath them would scroll instead.
        for (window in settingsWindows.asReversed()) {
            val w = settingsWidth(window)
            val h = settingsHeight(window)
            if (mouseX in window.x.toDouble()..(window.x + w).toDouble() &&
                mouseY in window.y.toDouble()..(window.y + h).toDouble()
            ) {
                window.scroll = (window.scroll - verticalAmount.toFloat() * 12F).coerceAtLeast(0F)
                return true
            }
        }

        // Topmost panel first: when two panels overlap, the one drawn in front has to consume the scroll.
        for (panel in panels.asReversed()) {
            val w = PANEL_WIDTH
            val modules = panelModules(panel)
            val h = panelHeight(panel, modules)
            if (mouseX in panel.x.toDouble()..(panel.x + w).toDouble() &&
                mouseY in panel.y.toDouble()..(panel.y + h).toDouble()
            ) {
                if (panel.collapsed) {
                    return true
                }

                val maxScroll = maxPanelScroll(panel, modules)
                panel.scroll = (panel.scroll - verticalAmount.toFloat() * 12F).coerceIn(0F, maxScroll)
                return true
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        val binding = bindingValue
        if (binding != null) {
            val key = InputConstants.Type.KEYBOARD.getOrCreate(input.key)
            @Suppress("UNCHECKED_CAST")
            val value = binding as Value<Any>
            val current = value.get()
            when (current) {
                is InputBind -> value.set(current.copy(boundKey = key) as Any)
                is InputConstants.Key -> value.set(key as Any)
            }
            bindingValue = null
            return true
        }

        val editing = editingText
        if (editing != null) {
            when (input.key) {
                InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
                    editing.setByString(textBuffer)
                    editingText = null
                }
                InputConstants.KEY_ESCAPE -> editingText = null
                InputConstants.KEY_BACKSPACE -> {
                    if (textBuffer.isNotEmpty()) {
                        textBuffer = textBuffer.dropLast(1)
                    }
                }
            }
            return true
        }

        if (input.key == InputConstants.KEY_ESCAPE) {
            mc.gui.setScreen(if (settingsOnly) parent else null)
            return true
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        if (editingText != null) {
            val codePoint = input.codepoint()
            if (codePoint >= 32) {
                textBuffer += codePoint.toChar()
            }
            return true
        }
        return super.charTyped(input)
    }

    override fun onClose() {
        openDropdown = null
        if (!settingsOnly) {
            saveLayout()
        }
        super.onClose()
    }

    override fun isPauseScreen() = false

    // --------------------------------------------------------- helpers

    private fun layoutFile(): File = File(ConfigSystem.rootFolder, "clickgui.json")

    /** Restores the positions of the category panels and the settings panel from the previous session. */
    private fun restoreLayout(): Boolean = runCatching {
        val file = layoutFile()
        if (!file.exists()) {
            return@runCatching false
        }

        val root = JsonParser.parseString(file.readText()).asJsonObject

        var restored = 0
        root.getAsJsonObject("panels")?.let { stored ->
            for (panel in panels) {
                val entry = stored.getAsJsonObject(panel.category.tag) ?: continue
                val x = entry.get("x")?.asFloat ?: continue
                val y = entry.get("y")?.asFloat ?: continue
                panel.x = x
                panel.y = y
                panel.collapsed = entry.get("collapsed")?.asBoolean ?: true
                restored++
            }
        }

        root.getAsJsonArray("windows")?.forEach { element ->
            val entry = element.asJsonObject
            val x = entry.get("x")?.asFloat ?: return@forEach
            val y = entry.get("y")?.asFloat ?: return@forEach
            val group = when (val target = entry.get("module")?.asString) {
                "Global" -> GlobalManager
                null, "" -> return@forEach
                else -> ModuleManager.firstOrNull { it.name == target } ?: return@forEach
            }
            if (settingsWindows.any { it.group === group }) {
                return@forEach
            }
            val title = if (group === GlobalManager) "Global Settings" else group.name
            val window = SettingsWindow(group, title, x, y)
            clampSettingsWindow(window)
            settingsWindows += window
        }

        // A partial or outdated file would leave panels stacked at the top left, so fall back to a layout.
        if (restored < panels.size) {
            return@runCatching false
        }

        true
    }.getOrDefault(false)

    private fun saveLayout() = runCatching {
        val root = JsonObject()
        val storedPanels = JsonObject()

        for (panel in panels) {
            storedPanels.add(panel.category.tag, JsonObject().apply {
                addProperty("x", panel.x)
                addProperty("y", panel.y)
                addProperty("collapsed", panel.collapsed)
            })
        }

        root.add("panels", storedPanels)

        val storedWindows = JsonArray()
        for (window in settingsWindows) {
            storedWindows.add(JsonObject().apply {
                addProperty(
                    "module",
                    if (window.group === GlobalManager) "Global" else (window.group as? ClientModule)?.name ?: "",
                )
                addProperty("x", window.x)
                addProperty("y", window.y)
            })
        }
        root.add("windows", storedWindows)

        layoutFile().writeText(root.toString())
    }

    /** Brings the panel that received interaction to the front so overlapping panels stay usable. */
    private fun focusPanel(panel: Panel) {
        if (panels.lastOrNull() === panel) {
            return
        }
        panels.remove(panel)
        panels.add(panel)
    }

    private fun toggleDropdown(value: Value<*>) {
        openDropdown = if (openDropdown === value) null else value
        dropdownScroll = 0F
    }

    /** True when the cursor is over an open dropdown overlay, which must survive a click. */
    private fun isInsideAnyDropdown(x: Double, y: Double): Boolean =
        dropdownBounds.any { x >= it[0] && x <= it[2] && y >= it[1] && y <= it[3] }

    /** Draws a 1px outline plus a soft shadow so the floating layers stay readable. */
    private fun drawOutline(context: GuiGraphicsExtractor, x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        context.fill(x1, y2 - 1, x2, y2, color)
        context.fill(x1, y1, x1 + 1, y2, color)
        context.fill(x2 - 1, y1, x2, y2, color)
    }

    private fun drawPopupBox(context: GuiGraphicsExtractor, x: Float, y: Float, w: Float, h: Float) {
        val left = x.toInt()
        val top = y.toInt()
        val right = (x + w).toInt()
        val bottom = (y + h).toInt()

        context.fill(left + 2, top + 2, right + 3, bottom + 3, SHADOW)
        context.fill(left, top, right, bottom, DROPDOWN_BG)
        context.fill(left, top, right, top + 3, PANEL_HEADER)
        drawOutline(context, left, top, right, bottom, OUTLINE)
    }

    @Suppress("UNCHECKED_CAST")
    private fun setRaw(value: Value<*>, newValue: Any) {
        (value as Value<Any>).set(newValue)
    }

    private companion object {
        const val PANEL_WIDTH = 132F
        const val SETTINGS_WIDTH = 200F
        const val GLOBAL_SETTINGS_WIDTH = 340F
        const val HEADER_HEIGHT = 16F
        const val ROW_HEIGHT = 13F
        const val OPTION_HEIGHT = 11F
        const val OPTION_MAX_HEIGHT = 132F
        const val MAX_PANEL_HEIGHT = 260F
        const val TOOLTIP_WIDTH = 200
        const val UNBIND_ACTION = "None"

        val SHADOW = 0x50000000
        val BACKGROUND = 0xC0101014.toInt()
        val PANEL_BG = 0xE01A1A20.toInt()
        val PANEL_HEADER = 0xFF26262E.toInt()
        val ROW_HOVER = 0xFF30303A.toInt()
        val FIELD_BG = 0xFF20202A.toInt()
        val DROPDOWN_BG = 0xF016161C.toInt()
        val SCROLLBAR = 0xFF3A3A46.toInt()
        val OFF = 0xFF3A3A46.toInt()
        val OUTLINE = 0xFF55555F.toInt()

        val ACCENT = 0xFF5C9DFF.toInt()
        val TEXT_ON = 0xFFE8E8EC.toInt()
        val TEXT_OFF = 0xFF9A9AA6.toInt()
        val HINT = 0xFF7A7A86.toInt()
        val GROUP_HEADER = 0xFF9FC3FF.toInt()
        val COLOR_CHANNELS = arrayOf("R", "G", "B", "A")
    }

}
