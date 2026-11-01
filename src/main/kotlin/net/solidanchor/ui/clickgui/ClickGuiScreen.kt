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

package net.solidanchor.ui.clickgui

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.blaze3d.platform.InputConstants
import net.solidanchor.config.ConfigSystem
import net.solidanchor.config.types.RangedValue
import net.solidanchor.config.types.Value
import net.solidanchor.config.types.ValueType
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.config.types.list.MultiChoiceListValue
import net.solidanchor.config.types.list.ChoiceListValue
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategory
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.features.global.GlobalManager
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.render.getBounds
import net.solidanchor.ui.hud.HudEditorScreen
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.input.InputBind
import net.solidanchor.utils.text.asPlainText
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
        val startMouseX: Double,
        val startMouseY: Double,
        val startX: Float,
        val startY: Float,
    )

    private val panels = ModuleCategories.entries.map { Panel(it) }.toMutableList()

    private val hits = ArrayList<HitArea>()

    private var settingsGroup: ValueGroup? = null
    private var settingsTitle = ""
    private var settingsPlaced = false
    private var layoutLoaded = false
    private var settingsScroll = 0F
    private var settingsX = 0F
    private var settingsY = 0F

    private var drag: SliderDrag? = null
    private var panelDrag: PanelDrag? = null
    private var settingsDrag: SettingsDrag? = null

    private var openDropdown: Value<*>? = null

    private var tooltipText: String? = null
    private var tooltipX = 0
    private var tooltipY = 0
    private var tooltipPanel: Panel? = null
    private var hoveredPanel: Panel? = null

    private var editingText: Value<*>? = null
    private var textBuffer = ""
    private var bindingValue: Value<*>? = null

    private val expandedLists = HashSet<Value<*>>()
    private val expandedColors = HashSet<Value<*>>()

    override fun init() {
        if (settingsOnly) {
            layoutLoaded = true
            val group = initialSettings
            if (group != null) {
                settingsGroup = group
                settingsTitle = initialSettingsTitle
                settingsScroll = 0F
                settingsX = ((width - settingsWidth()) / 2F).coerceIn(4F, (width - 4F).coerceAtLeast(4F))
                settingsY = 40F
                settingsPlaced = true
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

        // Only the topmost layer under the mouse may show a tooltip, otherwise the description
        // of a module in a panel behind another panel would be drawn through it.
        val overTopButton = mouseX.toDouble() in (width - 176F).toDouble()..(width - 10F).toDouble() &&
            mouseY.toDouble() in 8.0..24.0

        val topPanel = if (settingsOnly || overTopButton || isInsideSettings(mouseX.toDouble(), mouseY.toDouble())) {
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
            val title = Component.literal("SolidAnchor")
            context.centeredText(font, title, width / 2, 10, ACCENT)

            context.centeredText(
                font,
                Component.literal("Right click a module to configure it"),
                width / 2,
                22,
                HINT,
            )

            for (panel in panels) {
                drawPanel(context, panel, mouseX.toDouble(), mouseY.toDouble())
            }

            drawTopButtons(context, mouseX.toDouble(), mouseY.toDouble())
        } else {
            context.centeredText(font, Component.literal(settingsTitle), width / 2, 10, ACCENT)
        }

        val group = settingsGroup
        if (group != null) {
            drawSettings(context, group, settingsTitle, mouseX.toDouble(), mouseY.toDouble())
        }

        if (bindingValue != null) {
            context.centeredText(font, Component.literal("Press a key or mouse button"), width / 2, height - 20, ACCENT)
        }

        drawTooltip(context)

        super.extractRenderState(context, mouseX, mouseY, partialTick)
    }

    // ---------------------------------------------------------------- panels

    private fun panelModules(panel: Panel): List<ClientModule> =
        ModuleManager.filter { it.category == panel.category }.sortedBy { it.name }

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

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), PANEL_BG)
        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + HEADER_HEIGHT).toInt(), PANEL_HEADER)
        context.text(font, Component.literal(panel.category.tag), (x + 6).toInt(), (y + 4).toInt(), ACCENT, false)

        // Collapse / expand button
        context.text(
            font,
            Component.literal(if (panel.collapsed) "▼" else "▲"),
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
        val hovered = panel === hoveredPanel && !isInsideSettings(mouseX, mouseY) &&
            mouseX in x.toDouble()..(x + w).toDouble() && mouseY in y.toDouble()..(y + ROW_HEIGHT - 1).toDouble()

        if (hovered) {
            context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + ROW_HEIGHT - 1).toInt(), ROW_HOVER)
            if (panel === tooltipPanel) {
                tooltipText = module.description.get()?.takeIf { it.isNotBlank() && !it.startsWith("solidanchor.") }
                tooltipX = mouseX.toInt()
                tooltipY = mouseY.toInt()
            }
        }

        context.fill((x + 4).toInt(), (y + 4).toInt(), (x + 10).toInt(), (y + 10).toInt(),
            if (enabled) ACCENT else OFF)

        val color = if (enabled) TEXT_ON else TEXT_OFF
        val label = module.name
        val tag = module.tag
        val text = if (tag != null && tag.isNotBlank()) "$label ($tag)" else label
        context.text(font, Component.literal(text), (x + 14).toInt(), y.toInt() + 2, color, false)

        hits += HitArea(x, y, x + w, y + ROW_HEIGHT - 1, {
            focusPanel(panel)
            if (!module.notActivatable) {
                module.enabled = !module.enabled
            }
        }, {
            focusPanel(panel)
            settingsGroup = module
            settingsTitle = module.name
            settingsScroll = 0F
            openDropdown = null
            editingText = null
            if (!settingsPlaced) {
                settingsX = if (panel.x + PANEL_WIDTH + settingsWidth() + 12 < width) {
                    panel.x + PANEL_WIDTH + 6
                } else {
                    (panel.x - settingsWidth() - 6).coerceAtLeast(4F)
                }
                settingsY = maxOf(panel.y, 34F)
                settingsPlaced = true
            }
            clampSettingsPosition()
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

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + 16).toInt(), if (hovered) PANEL_HEADER else PANEL_BG)
        context.centeredText(font, Component.literal(label), (x + w / 2).toInt(), (y + 4).toInt(), TEXT_ON)

        hits += HitArea(x, y, x + w, y + 16, onClick)
    }

    private fun openGlobalSettings() {
        settingsGroup = GlobalManager
        settingsTitle = "Global Settings"
        settingsScroll = 0F
        openDropdown = null
        editingText = null

        if (!settingsPlaced) {
            settingsX = ((width - settingsWidth()) / 2F).coerceAtLeast(4F)
            settingsY = 40F
            settingsPlaced = true
        }
        clampSettingsPosition()
    }

    private fun clampSettingsPosition() {
        settingsX = settingsX.coerceIn(0F, (width - settingsWidth()).coerceAtLeast(0F))
        settingsY = settingsY.coerceIn(0F, (height - 40F).coerceAtLeast(0F))
    }

    private fun drawTooltip(context: GuiGraphicsExtractor) {
        val text = tooltipText ?: return

        val lines = wrapText(text, TOOLTIP_WIDTH)
        val w = lines.maxOf { font.width(it) } + 8
        val h = lines.size * 10 + 6

        val x = tooltipX.coerceAtMost(width - w - 4).coerceAtLeast(4)
        val y = tooltipY.coerceAtMost(height - h - 4).coerceAtLeast(4)

        context.fill(x, y, x + w, y + h, PANEL_BG)
        context.fill(x, y, x + w, y + 1, ACCENT)
        lines.forEachIndexed { index, line ->
            context.text(font, Component.literal(line), x + 4, y + 4 + index * 10, TEXT_ON, false)
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

    private fun settingsWidth(): Float {
        val preferred = when {
            settingsGroup === GlobalManager -> GLOBAL_SETTINGS_WIDTH
            settingsOnly -> 260F
            else -> SETTINGS_WIDTH
        }
        return minOf(preferred, (width - 16F).toFloat()).coerceAtLeast(SETTINGS_WIDTH)
    }

    private fun settingsHeight(): Float {
        if (settingsGroup == null) {
            return 0F
        }
        val maxHeight = when {
            settingsOnly -> height - settingsY - 10F
            settingsGroup === GlobalManager -> height - 60F
            else -> 260F
        }
        return minOf((height - settingsY - 10).toFloat(), maxHeight)
    }

    private fun isInsideSettings(x: Double, y: Double): Boolean =
        settingsGroup != null &&
            x >= settingsX && x <= settingsX + settingsWidth() &&
            y >= settingsY && y <= settingsY + settingsHeight()

    private fun drawSettings(
        context: GuiGraphicsExtractor,
        group: ValueGroup,
        title: String,
        mouseX: Double,
        mouseY: Double,
    ) {
        val x = settingsX
        val y = settingsY
        val w = settingsWidth()
        val h = settingsHeight()
        val hitStart = hits.size

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), PANEL_BG)
        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + HEADER_HEIGHT).toInt(), PANEL_HEADER)
        context.text(font, Component.literal(title), (x + 6).toInt(), (y + 4).toInt(), ACCENT, false)

        // Swallow clicks on the empty parts of the settings panel so they do not reach the
        // modules of a panel that happens to be underneath it.
        hits += HitArea(x, y, x + w, y + h, { openDropdown = null })

        val closeX = x + w - 14

        // Dragging the header moves the settings panel.
        hits += HitArea(x, y, closeX, y + HEADER_HEIGHT, {
            settingsDrag = SettingsDrag(mouseX, mouseY, settingsX, settingsY)
        })

        context.text(font, Component.literal("x"), closeX.toInt(), (y + 4).toInt(), TEXT_OFF, false)
        hits += HitArea(closeX, y, x + w, y + HEADER_HEIGHT, {
            openDropdown = null
            if (settingsOnly) {
                mc.gui.setScreen(parent)
            } else {
                settingsGroup = null
            }
        })

        context.scissorStack.push(context.getBounds(x, y + HEADER_HEIGHT, x + w, y + h))

        val values = collectSettings(group)
        var cursorY = y + HEADER_HEIGHT + 4F - settingsScroll

        for (value in values) {
            val consumed = drawValue(context, value, x + 4F, cursorY, w - 8F, mouseX, mouseY)
            cursorY += consumed
        }

        val contentHeight = cursorY - (y + HEADER_HEIGHT + 4F) + settingsScroll
        val viewport = h - HEADER_HEIGHT
        val maxScroll = (contentHeight - viewport).coerceAtLeast(0F)
        settingsScroll = settingsScroll.coerceIn(0F, maxScroll)

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

                var consumed = ROW_HEIGHT + 2F

                if (openDropdown === value) {
                    consumed += drawDropdown(
                        context, x, y + consumed, w,
                        value.modes.map { it.tag },
                        value.activeMode.tag,
                        mouseX, mouseY,
                    ) { tag ->
                        value.setByString(tag)
                        openDropdown = null
                    }
                }

                return consumed
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

                context.text(font, Component.literal("${value.name}: $keyText"), x.toInt(), y.toInt() + 2, TEXT_ON, false)
                context.fill(actionX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + ROW_HEIGHT - 2).toInt(), PANEL_HEADER)
                context.text(font, Component.literal("${bind.action.tag} ▾"), (actionX + 4).toInt(), y.toInt() + 2, ACCENT, false)

                // Clicking the key part starts rebinding, the action box opens the action dropdown.
                hits += HitArea(x, y, actionX - 2, y + ROW_HEIGHT, { bindingValue = value })
                hits += HitArea(actionX, y, x + w, y + ROW_HEIGHT, { toggleDropdown(value) })

                var consumed = ROW_HEIGHT + 2F

                if (openDropdown === value) {
                    consumed += drawDropdown(
                        context, x, y + consumed, w,
                        InputBind.BindAction.entries.map { it.tag },
                        bind.action.tag,
                        mouseX, mouseY,
                    ) { tag ->
                        InputBind.BindAction.of(tag)?.let { setRaw(value, bind.copy(action = it)) }
                        openDropdown = null
                    }

                    consumed += drawDropdown(
                        context, x, y + consumed, w,
                        listOf("Unbind"),
                        null,
                        mouseX, mouseY,
                    ) {
                        setRaw(value, bind.copy(boundKey = InputConstants.UNKNOWN))
                        openDropdown = null
                    }
                }

                return consumed
            }

            ValueType.CHOOSE -> drawChoiceValue(context, value, x, y, w, mouseX, mouseY)

            ValueType.MULTI_CHOOSE -> drawMultiChoiceValue(context, value, x, y, w)

            ValueType.CONFIGURABLE, ValueType.CHOICE -> {
                context.text(font, Component.literal(value.name), x.toInt(), y.toInt(), GROUP_HEADER, false)
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
        context.text(font, Component.literal(name), (x + 12).toInt(), y.toInt() + 2, if (checked) TEXT_ON else TEXT_OFF, false)
    }

    private fun drawChoiceRow(
        context: GuiGraphicsExtractor,
        value: Value<*>,
        x: Float,
        y: Float,
        w: Float,
        label: String,
    ) {
        context.text(font, Component.literal(label), x.toInt(), y.toInt() + 2, TEXT_ON, false)
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

        var consumed = ROW_HEIGHT + 2F

        if (openDropdown === value) {
            consumed += drawDropdown(
                context, x, y + consumed, w,
                choice.choices.map { it.tag },
                choice.get().tag,
                mouseX, mouseY,
            ) { tag ->
                choice.choices.firstOrNull { it.tag == tag }?.let { setRaw(value, it) }
                openDropdown = null
            }
        }

        return consumed
    }

    /**
     * Renders a dropdown list below [y] and registers the option hit areas.
     * Returns the height that was consumed.
     */
    private fun drawDropdown(
        context: GuiGraphicsExtractor,
        x: Float,
        y: Float,
        w: Float,
        options: List<String>,
        selected: String?,
        mouseX: Double,
        mouseY: Double,
        onSelect: (String) -> Unit,
    ): Float {
        val height = options.size * OPTION_HEIGHT + 3F

        context.fill(x.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + height).toInt(), DROPDOWN_BG)

        var rowY = y + 2F
        for (option in options) {
            val hovered = mouseX in (x + 3).toDouble()..(x + w - 3).toDouble() &&
                mouseY in rowY.toDouble()..(rowY + OPTION_HEIGHT).toDouble()

            if (hovered) {
                context.fill((x + 3).toInt(), rowY.toInt(), (x + w - 3).toInt(), (rowY + OPTION_HEIGHT).toInt(), ROW_HOVER)
            }

            if (option == selected) {
                context.fill((x + 5).toInt(), (rowY + 3).toInt(), (x + 7).toInt(), (rowY + 6).toInt(), ACCENT)
            }

            context.text(
                font,
                Component.literal(option),
                (x + 10).toInt(),
                (rowY + 1).toInt(),
                if (option == selected) ACCENT else TEXT_ON,
                false,
            )

            hits += HitArea(x + 3, rowY, x + w - 3, rowY + OPTION_HEIGHT, { onSelect(option) })

            rowY += OPTION_HEIGHT
        }

        return height
    }

    private fun drawMultiChoiceValue(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float): Float {
        @Suppress("UNCHECKED_CAST")
        val multi = (value as? MultiChoiceListValue<*>) as? MultiChoiceListValue<Tagged> ?: return ROW_HEIGHT + 2F
        val active = multi.get()
        val expanded = value in expandedLists

        drawChoiceRow(context, value, x, y, w, "${value.name}: ${active.size}/${multi.choices.size}")

        hits += HitArea(x, y, x + w, y + ROW_HEIGHT, {
            if (expanded) expandedLists.remove(value) else expandedLists.add(value)
        })

        var consumed = ROW_HEIGHT + 2F

        if (expanded) {
            for (choice in multi.choices) {
                val checked = choice in active
                context.fill((x + 6).toInt(), (y + consumed + 2).toInt(), (x + 12).toInt(), (y + consumed + 8).toInt(),
                    if (checked) ACCENT else OFF)
                context.text(font, Component.literal(choice.tag), (x + 16).toInt(), (y + consumed).toInt(), TEXT_ON, false)
                hits += HitArea(x + 4, y + consumed, x + w, y + consumed + 10, {
                    multi.toggle(choice)
                })
                consumed += 12F
            }
            consumed += 2F
        }

        return consumed
    }

    private fun drawTextRow(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float) {
        val editing = editingText === value
        val shown = if (editing) textBuffer else value.get().toString()

        context.text(font, Component.literal(value.name), x.toInt(), y.toInt() + 2, TEXT_OFF, false)
        val boxX = x + w * 0.5F
        context.fill(boxX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(), if (editing) PANEL_HEADER else OFF)
        context.text(font, Component.literal(shown), (boxX + 3).toInt(), (y + 2).toInt(), TEXT_ON, false)

        hits += HitArea(boxX, y, x + w, y + ROW_HEIGHT, {
            editingText = value
            textBuffer = value.get().toString()
        })
    }

    private fun drawColorValue(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float): Float {
        val color = value.get() as? Color4b ?: Color4b.WHITE
        val expanded = value in expandedColors

        context.text(font, Component.literal(value.name), x.toInt(), y.toInt() + 2, TEXT_OFF, false)
        val swatchX = x + w * 0.5F
        context.fill(swatchX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(), color.argb)
        context.fill(swatchX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 2).toInt(), OUTLINE)

        hits += HitArea(swatchX, y, x + w, y + ROW_HEIGHT, {
            if (expanded) expandedColors.remove(value) else expandedColors.add(value)
        })

        var consumed = ROW_HEIGHT + 2F

        if (expanded) {
            val channels = listOf("R" to color.r, "G" to color.g, "B" to color.b, "A" to color.a)
            for ((index, channel) in channels.withIndex()) {
                val (name, level) = channel
                val rowY = y + consumed
                context.text(font, Component.literal(name), x.toInt(), rowY.toInt() + 2, TEXT_OFF, false)

                val startX = x + 16F
                val endX = x + w
                context.fill(startX.toInt(), (rowY + 2).toInt(), endX.toInt(), (rowY + 8).toInt(), OFF)
                context.fill(startX.toInt(), (rowY + 2).toInt(),
                    (startX + (endX - startX) * (level / 255F)).toInt(), (rowY + 8).toInt(), ACCENT)

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

        context.text(font, Component.literal(label), x.toInt(), y.toInt(), TEXT_ON, false)

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
                context.fill(firstX.toInt(), (barY).toInt(), secondX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(firstX.toInt(), (barY - 2).toInt(), (firstX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)
                context.fill(secondX.toInt(), (barY - 2).toInt(), (secondX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)

                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    val pickSecond = mouseX > (firstX + secondX) / 2
                    drag = SliderDrag(value, barX, barX + barW, min, max, pickSecond)
                })
            }

            is IntRange -> {
                val firstX = barX + barW * ((current.first - min) / span).toFloat()
                val secondX = barX + barW * ((current.last - min) / span).toFloat()
                context.fill(firstX.toInt(), barY.toInt(), secondX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(firstX.toInt(), (barY - 2).toInt(), (firstX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)
                context.fill(secondX.toInt(), (barY - 2).toInt(), (secondX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)

                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    val pickSecond = mouseX > (firstX + secondX) / 2
                    drag = SliderDrag(value, barX, barX + barW, min, max, pickSecond)
                })
            }

            else -> {
                val number = (current as? Number)?.toDouble() ?: 0.0
                val handleX = barX + barW * ((number - min) / span).toFloat()
                context.fill(barX.toInt(), barY.toInt(), handleX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(handleX.toInt(), (barY - 2).toInt(), (handleX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)

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

        // A click anywhere outside the settings popup closes an open dropdown.
        if (!isInsideSettings(x, y)) {
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
            settingsX = (active.startX + (click.x - active.startMouseX).toFloat())
                .coerceIn(0F, (width - settingsWidth()).coerceAtLeast(0F))
            settingsY = (active.startY + (click.y - active.startMouseY).toFloat())
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
        // The settings popup is drawn on top of the panels, so it has to consume the scroll first,
        // otherwise a panel underneath it would scroll instead.
        if (isInsideSettings(mouseX, mouseY)) {
            settingsScroll = (settingsScroll - verticalAmount.toFloat() * 12F).coerceAtLeast(0F)
            return true
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
        settingsGroup = null
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

        root.getAsJsonObject("settings")?.let { stored ->
            settingsX = stored.get("x")?.asFloat ?: settingsX
            settingsY = stored.get("y")?.asFloat ?: settingsY
            settingsPlaced = true

            // Reopen the module detail that was expanded when the GUI was closed.
            when (val target = stored.get("module")?.asString) {
                "Global" -> openGlobalSettings()
                null, "" -> {}
                else -> ModuleManager.firstOrNull { it.name == target }?.let { module ->
                    settingsGroup = module
                    settingsTitle = module.name
                }
            }
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

        root.add("settings", JsonObject().apply {
            addProperty("x", settingsX)
            addProperty("y", settingsY)
            addProperty(
                "module",
                when {
                    settingsGroup === GlobalManager -> "Global"
                    settingsGroup != null -> (settingsGroup as? ClientModule)?.name ?: ""
                    else -> ""
                },
            )
        })

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
        const val MAX_PANEL_HEIGHT = 260F
        const val TOOLTIP_WIDTH = 200

        val BACKGROUND = 0xC0101014.toInt()
        val PANEL_BG = 0xE01A1A20.toInt()
        val PANEL_HEADER = 0xFF26262E.toInt()
        val ROW_HOVER = 0xFF30303A.toInt()
        val DROPDOWN_BG = 0xF016161C.toInt()
        val SCROLLBAR = 0xFF3A3A46.toInt()
        val OFF = 0xFF3A3A46.toInt()
        val OUTLINE = 0xFF55555F.toInt()

        val ACCENT = 0xFF5C9DFF.toInt()
        val TEXT_ON = 0xFFE8E8EC.toInt()
        val TEXT_OFF = 0xFF9A9AA6.toInt()
        val HINT = 0xFF7A7A86.toInt()
        val GROUP_HEADER = 0xFF9FC3FF.toInt()
    }

}
