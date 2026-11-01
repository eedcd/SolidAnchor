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

package net.solidanchor.ui.hud

import com.mojang.blaze3d.platform.InputConstants
import net.solidanchor.config.types.RangedValue
import net.solidanchor.config.types.Value
import net.solidanchor.config.types.ValueType
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.config.types.list.ChoiceListValue
import net.solidanchor.config.types.list.MultiChoiceListValue
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.module.modules.render.ModuleHud
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.render.getBounds
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.render.Alignment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

/**
 * Editor for the native HUD components.
 *
 * Contains a searchable, scrollable component list, a settings editor for the selected component
 * and a live preview of every enabled component that can be dragged around the screen.
 */
class HudEditorScreen(private val parent: Screen?) : Screen(Component.literal("HUD Editor")) {

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

    private class Drag(
        val component: NativeHudComponent,
        val mouseX: Double,
        val mouseY: Double,
        val startX: Float,
        val startY: Float,
    )

    private val hits = ArrayList<HitArea>()

    private var drag: Drag? = null
    private var slider: SliderDrag? = null

    private var selected: NativeHudComponent? = null
    private var settingsScroll = 0F

    private var pickerOpen = false
    private var pickerScroll = 0F

    private var contextMenu: NativeHudComponent? = null
    private var contextMenuX = 0F
    private var contextMenuY = 0F

    private var editingText: Value<*>? = null
    private var textBuffer = ""
    private val expandedLists = HashSet<Value<*>>()
    private val expandedColors = HashSet<Value<*>>()

    private var tooltip: String? = null
    private var tooltipX = 0
    private var tooltipY = 0

    private val canPreview: Boolean
        get() = mc.player != null && mc.level != null

    private val settingsWidth get() = minOf(260F, (width - 8F).toFloat())
    private val settingsX get() = width - settingsWidth - 4F
    private val settingsTop = 34F

    private val addButtonWidth = 110F
    private val addButtonX = 8F
    private val addButtonY = 32F

    private val pickerWidth = 150F
    private val pickerX = 8F
    private val pickerTop = 54F

    override fun init() {
        ModuleHud.hudEditorSelected = true
    }

    // ---------------------------------------------------------------- rendering

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        hits.clear()
        tooltip = null

        context.fill(0, 0, width, height, BACKDROP)
        context.centeredText(font, Component.literal("HUD Editor"), width / 2, 10, ACCENT)
        context.centeredText(
            font,
            Component.literal("Drag to move · Right click a component to configure or delete · Esc to close"),
            width / 2,
            22,
            TEXT_OFF,
        )

        // The preview is registered first so the panels below are always hit before it.
        if (canPreview) {
            for (component in HudComponentManager.nativeComponents) {
                if (!component.enabled) {
                    continue
                }
                drawPreview(context, component, mouseX.toDouble(), mouseY.toDouble())
            }
        }

        val current = selected
        if (current != null) {
            drawSettings(context, current, mouseX.toDouble(), mouseY.toDouble())
        }

        // Drawn after the settings panel so the add button and picker stay clickable.
        drawAddButton(context, mouseX.toDouble(), mouseY.toDouble())

        if (pickerOpen) {
            drawPicker(context, mouseX.toDouble(), mouseY.toDouble())
        }

        val menu = contextMenu
        if (menu != null) {
            drawContextMenu(context, menu, mouseX.toDouble(), mouseY.toDouble())
        }

        drawTooltip(context)

        super.extractRenderState(context, mouseX, mouseY, partialTick)
    }

    private fun drawAddButton(context: GuiGraphicsExtractor, mouseX: Double, mouseY: Double) {
        val x = addButtonX
        val y = addButtonY
        val w = addButtonWidth
        val hovered = mouseX in x.toDouble()..(x + w).toDouble() && mouseY in y.toDouble()..(y + 16).toDouble()

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + 16).toInt(), if (hovered) HEADER_BG else PANEL_BG)
        context.centeredText(font, Component.literal("Add Component"), (x + w / 2).toInt(), y.toInt() + 4, TEXT_ON)

        hits += HitArea(x, y, x + w, y + 16, {
            pickerOpen = !pickerOpen
            pickerScroll = 0F
        })
    }

    private fun drawPicker(context: GuiGraphicsExtractor, mouseX: Double, mouseY: Double) {
        val x = pickerX
        val w = pickerWidth
        val top = pickerTop
        val rows = HudComponentManager.nativeComponents
        val headerHeight = 16F
        val contentHeight = rows.size * ROW_HEIGHT + 4F
        val maxPanelHeight = (height - top - 40F).coerceAtLeast(ROW_HEIGHT * 4F)
        val panelHeight = headerHeight + contentHeight.coerceAtMost(maxPanelHeight)
        val listTop = top + headerHeight
        val bottom = top + panelHeight

        context.fill(x.toInt(), top.toInt(), (x + w).toInt(), bottom.toInt(), PANEL_BG)
        context.fill(x.toInt(), top.toInt(), (x + w).toInt(), listTop.toInt(), HEADER_BG)
        context.text(font, Component.literal("Add Component"), x.toInt() + 5, top.toInt() + 4, TEXT_ON, false)

        // Swallow clicks so nothing behind the picker is triggered.
        hits += HitArea(x, top, x + w, bottom, {})

        val viewport = bottom - listTop
        pickerScroll = pickerScroll.coerceIn(0F, (contentHeight - viewport).coerceAtLeast(0F))

        context.scissorStack.push(context.getBounds(x, listTop, x + w, bottom))

        var rowY = listTop + 2F - pickerScroll
        for (component in rows) {
            val rowBottom = rowY + ROW_HEIGHT - 1F
            if (rowBottom > listTop && rowY < bottom) {
                val hovered = mouseX in x.toDouble()..(x + w).toDouble() &&
                    mouseY in rowY.toDouble()..rowBottom.toDouble()
                if (hovered) {
                    context.fill(x.toInt(), rowY.toInt(), (x + w).toInt(), rowBottom.toInt(), ROW_HOVER)
                }

                context.text(
                    font,
                    Component.literal(component.name),
                    (x + 6).toInt(),
                    rowY.toInt() + 3,
                    if (component.enabled) ACCENT else TEXT_ON,
                    false,
                )

                hits += HitArea(x, rowY, x + w, rowBottom, {
                    component.enabled = true
                    selected = component
                    settingsScroll = 0F
                    editingText = null
                    pickerOpen = false
                })
            }
            rowY += ROW_HEIGHT
        }

        context.scissorStack.pop()

        if (contentHeight > viewport) {
            val trackH = viewport - 4F
            val thumbH = (trackH * (viewport / contentHeight)).coerceAtLeast(8F)
            val thumbY = listTop + 2F + (trackH - thumbH) * (pickerScroll / (contentHeight - viewport))
            context.fill((x + w - 4).toInt(), thumbY.toInt(), (x + w - 2).toInt(), (thumbY + thumbH).toInt(), SCROLLBAR)
        }
    }

    private fun drawContextMenu(
        context: GuiGraphicsExtractor,
        component: NativeHudComponent,
        mouseX: Double,
        mouseY: Double,
    ) {
        val items = listOf("Configure", "Reset Position", "Delete")
        val w = 96F
        val x = contextMenuX.coerceIn(2F, (width - w - 2F).coerceAtLeast(2F))
        val maxY = (height - items.size * ROW_HEIGHT - 4F).coerceAtLeast(2F)
        val y = contextMenuY.coerceIn(2F, maxY)

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + items.size * ROW_HEIGHT + 3).toInt(), PANEL_BG)
        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + 1).toInt(), ACCENT)

        // Registered first so the menu entries below take precedence over the dismiss area.
        hits += HitArea(0F, 0F, width.toFloat(), height.toFloat(), { contextMenu = null })

        var rowY = y + 2F
        for (item in items) {
            val rowBottom = rowY + ROW_HEIGHT
            val hovered = mouseX in x.toDouble()..(x + w).toDouble() && mouseY in rowY.toDouble()..rowBottom.toDouble()
            if (hovered) {
                context.fill(x.toInt(), rowY.toInt(), (x + w).toInt(), rowBottom.toInt(), ROW_HOVER)
            }

            context.text(
                font,
                Component.literal(item),
                (x + 6).toInt(),
                rowY.toInt() + 3,
                when (item) {
                    "Delete" -> 0xFFFF6666.toInt()
                    else -> TEXT_ON
                },
                false,
            )

            hits += HitArea(x, rowY, x + w, rowBottom, {
                when (item) {
                    "Configure" -> {
                        selected = component
                        settingsScroll = 0F
                        editingText = null
                    }

                    "Reset Position" -> component.resetAlignment()

                    "Delete" -> {
                        component.enabled = false
                        if (selected === component) {
                            selected = null
                        }
                    }
                }
                contextMenu = null
            })

            rowY += ROW_HEIGHT
        }
    }

    private fun drawPreview(
        context: GuiGraphicsExtractor,
        component: NativeHudComponent,
        mouseX: Double,
        mouseY: Double,
    ) {
        val w = component.guiScaledWidth
        val h = component.guiScaledHeight
        val bounds = component.alignment.getBounds(w, h)

        if (contextMenu == null &&
            mouseX in bounds.xMin.toDouble()..bounds.xMax.toDouble() &&
            mouseY in bounds.yMin.toDouble()..bounds.yMax.toDouble()
        ) {
            tooltip = component.componentDescription.takeIf { it.isNotBlank() }
            tooltipX = mouseX.toInt()
            tooltipY = mouseY.toInt()
        }

        context.fill(bounds.xMin.toInt(), bounds.yMin.toInt(), bounds.xMax.toInt(), bounds.yMax.toInt(), PREVIEW)
        context.fill(
            bounds.xMin.toInt(), bounds.yMin.toInt(), bounds.xMax.toInt(), (bounds.yMin + 1).toInt(), ACCENT,
        )
        context.text(
            font,
            Component.literal(component.name),
            bounds.xMin.toInt() + 2,
            bounds.yMin.toInt() + 2,
            TEXT_ON,
            true,
        )

        if (drag?.component === component) {
            val label = "${bounds.xMin.toInt()}, ${bounds.yMin.toInt()}"
            context.fill(
                bounds.xMin.toInt(), (bounds.yMax + 1).toInt(),
                bounds.xMin.toInt() + font.width(label) + 4, (bounds.yMax + 12).toInt(),
                HEADER_BG,
            )
            context.text(font, Component.literal(label), bounds.xMin.toInt() + 2, bounds.yMax.toInt() + 3, TEXT_ON, false)
        }

        hits += HitArea(bounds.xMin, bounds.yMin, bounds.xMax, bounds.yMax, {
            drag = Drag(component, mouseX, mouseY, bounds.xMin, bounds.yMin)
        }, {
            contextMenu = component
            contextMenuX = mouseX.toFloat()
            contextMenuY = mouseY.toFloat()
        })
    }

    // ---------------------------------------------------------------- settings

    private fun drawSettings(
        context: GuiGraphicsExtractor,
        component: NativeHudComponent,
        mouseX: Double,
        mouseY: Double,
    ) {
        val x = settingsX
        val y = settingsTop
        val w = settingsWidth
        val h = height - y - 8F
        val hitStart = hits.size

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), PANEL_BG)
        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + HEADER).toInt(), HEADER_BG)
        context.text(font, Component.literal(component.name), (x + 6).toInt(), (y + 4).toInt(), ACCENT, false)

        val closeX = x + w - 14
        context.text(font, Component.literal("x"), closeX.toInt(), (y + 4).toInt(), TEXT_OFF, false)

        // Swallow clicks on the empty parts of the settings panel.
        hits += HitArea(x, y, x + w, y + h, {})
        hits += HitArea(closeX, y, x + w, y + HEADER, { selected = null })

        context.scissorStack.push(context.getBounds(x, y + HEADER, x + w, y + h))

        // The component's own Enabled value is handled through the picker / context menu.
        val values = collectValues(component).filterNot { it === component.enabledValue }
        var cursorY = y + HEADER + 4F - settingsScroll
        for (value in values) {
            cursorY += drawValue(context, value, x + 4F, cursorY, w - 8F, mouseX, mouseY)
        }

        val contentHeight = cursorY - (y + HEADER + 4F) + settingsScroll
        val viewport = h - HEADER
        settingsScroll = settingsScroll.coerceIn(0F, (contentHeight - viewport).coerceAtLeast(0F))

        // Drop hit areas of rows that scrolled out of the panel.
        hits.subList(hitStart, hits.size).removeIf { hit ->
            hit.x2 < x || hit.x1 > x + w || hit.y2 < y || hit.y1 > y + h
        }

        context.scissorStack.pop()
    }

    private fun collectValues(group: ValueGroup): List<Value<*>> {
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
                val modes = value.modes
                val index = modes.indexOfFirst { it === value.activeMode }
                val next = modes[(index + 1) % modes.size]
                drawText(context, "${value.name}: ${value.activeMode.name}", x, y)
                hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { value.setByString(next.tag) })
                return ROW_HEIGHT + 2F
            }

            is ToggleableValueGroup -> {
                drawCheckRow(context, x, y, value.name, value.enabled)
                hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { value.enabled = !value.enabled })
                return ROW_HEIGHT + 2F
            }

            else -> Unit
        }

        return when (value.valueType) {
            ValueType.BOOLEAN -> {
                val current = value.get() as? Boolean ?: false
                drawCheckRow(context, x, y, value.name, current)
                hits += HitArea(x, y, x + w, y + ROW_HEIGHT, { setRaw(value, !current) })
                ROW_HEIGHT + 2F
            }

            ValueType.FLOAT, ValueType.INT, ValueType.FLOAT_RANGE, ValueType.INT_RANGE -> {
                drawSliderValue(context, value, x, y, w, mouseX)
            }

            ValueType.TEXT -> {
                drawTextRow(context, value, x, y, w)
                ROW_HEIGHT + 2F
            }

            ValueType.COLOR -> drawColorValue(context, value, x, y, w)

            ValueType.CHOOSE, ValueType.CHOICE -> drawChoiceValue(context, value, x, y, w)

            ValueType.MULTI_CHOOSE -> drawMultiChoiceValue(context, value, x, y, w)

            ValueType.CONFIGURABLE -> {
                context.text(font, Component.literal(value.name), x.toInt(), y.toInt(), GROUP_TITLE, false)
                ROW_HEIGHT
            }

            else -> {
                drawText(context, "${value.name}: ${value.getValue()}", x, y)
                ROW_HEIGHT + 2F
            }
        }
    }

    private fun drawText(context: GuiGraphicsExtractor, text: String, x: Float, y: Float) {
        context.text(font, Component.literal(text), x.toInt(), y.toInt() + 2, TEXT_ON, false)
    }

    private fun drawCheckRow(
        context: GuiGraphicsExtractor,
        x: Float,
        y: Float,
        name: String,
        checked: Boolean,
    ) {
        context.fill((x + 1).toInt(), (y + 3).toInt(), (x + 8).toInt(), (y + 10).toInt(), if (checked) ACCENT else OFF)
        context.text(
            font,
            Component.literal(name),
            (x + 12).toInt(),
            y.toInt() + 2,
            if (checked) TEXT_ON else TEXT_OFF,
            false,
        )
    }

    private fun drawChoiceValue(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float): Float {
        drawText(context, "${value.name}: ${value.getValue()}", x, y)
        hits += HitArea(x, y, x + w, y + ROW_HEIGHT, {
            val choice = value as? ChoiceListValue<*>
            if (choice != null) {
                val options = choice.choices.toList()
                val index = options.indexOf(choice.get())
                setRaw(value, options[(index + 1) % options.size])
            }
        })
        return ROW_HEIGHT + 2F
    }

    private fun drawMultiChoiceValue(context: GuiGraphicsExtractor, value: Value<*>, x: Float, y: Float, w: Float): Float {
        @Suppress("UNCHECKED_CAST")
        val multi = (value as? MultiChoiceListValue<*>) as? MultiChoiceListValue<Tagged> ?: return ROW_HEIGHT + 2F
        val expanded = value in expandedLists

        drawText(context, "${value.name}: ${multi.get().size}/${multi.choices.size}", x, y)
        hits += HitArea(x, y, x + w, y + ROW_HEIGHT, {
            if (expanded) expandedLists.remove(value) else expandedLists.add(value)
        })

        var consumed = ROW_HEIGHT + 2F

        if (expanded) {
            for (choice in multi.choices) {
                val checked = choice in multi.get()
                context.fill((x + 6).toInt(), (y + consumed + 2).toInt(), (x + 12).toInt(), (y + consumed + 8).toInt(),
                    if (checked) ACCENT else OFF)
                context.text(font, Component.literal(choice.tag), (x + 16).toInt(), (y + consumed).toInt(), TEXT_ON, false)
                hits += HitArea(x + 4, y + consumed, x + w, y + consumed + 10, { multi.toggle(choice) })
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
        context.fill(boxX.toInt(), (y + 1).toInt(), (x + w).toInt(), (y + 11).toInt(), if (editing) HEADER_BG else OFF)
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

                hits += HitArea(startX, rowY, endX, rowY + 10, {
                    editingText = null
                    slider = SliderDrag(value, startX, endX, 0.0, 255.0, false, index)
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
    ): Float {
        val ranged = value as? RangedValue<*>
        val range = ranged?.range
        val label = "${value.name}: ${formatValue(value)}"

        context.text(font, Component.literal(label), x.toInt(), y.toInt(), TEXT_ON, false)

        val barX = x
        val barW = w
        val barY = y + 10F

        context.fill(barX.toInt(), (barY + 1).toInt(), (barX + barW).toInt(), (barY + 4).toInt(), OFF)

        if (range == null) {
            return ROW_HEIGHT + 6F
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
                context.fill(firstX.toInt(), (barY - 2).toInt(), (firstX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)
                context.fill(secondX.toInt(), (barY - 2).toInt(), (secondX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)
                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    val pickSecond = mouseX > (firstX + secondX) / 2
                    slider = SliderDrag(value, barX, barX + barW, min, max, pickSecond)
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
                    slider = SliderDrag(value, barX, barX + barW, min, max, pickSecond)
                })
            }

            else -> {
                val number = (current as? Number)?.toDouble() ?: 0.0
                val handleX = barX + barW * ((number - min) / span).toFloat()
                context.fill(barX.toInt(), barY.toInt(), handleX.toInt(), (barY + 5).toInt(), ACCENT)
                context.fill(handleX.toInt(), (barY - 2).toInt(), (handleX + 2).toInt(), (barY + 7).toInt(), TEXT_ON)
                hits += HitArea(barX, barY - 4, barX + barW, barY + 8, {
                    slider = SliderDrag(value, barX, barX + barW, min, max, false)
                })
            }
        }

        return ROW_HEIGHT + 6F
    }

    private fun formatValue(value: Value<*>): String = when (val current = value.get()) {
        is ClosedFloatingPointRange<*> -> String.format("%.2f..%.2f", current.start as Float, current.endInclusive as Float)
        is IntRange -> "${current.first}..${current.last}"
        is Float -> String.format("%.2f", current)
        else -> current.toString()
    }

    // ---------------------------------------------------------------- tooltip

    private fun drawTooltip(context: GuiGraphicsExtractor) {
        val text = tooltip ?: return

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

    // ---------------------------------------------------------------- dragging

    private fun applyPosition(component: NativeHudComponent, targetX: Float, targetY: Float) {
        val scale = mc.window.guiScale
        val w = component.guiScaledWidth
        val h = component.guiScaledHeight
        val screenW = mc.window.guiScaledWidth.toFloat()
        val screenH = mc.window.guiScaledHeight.toFloat()

        val x = snap(targetX, 0F, screenW - w)
        val y = snap(targetY, 0F, screenH - h)

        val horizontalOffset = when (component.alignment.horizontalAlignment) {
            Alignment.ScreenAxisX.LEFT -> x
            Alignment.ScreenAxisX.CENTER -> x - screenW / 2F
            Alignment.ScreenAxisX.CENTER_TRANSLATED -> x - (screenW / 2F - w / 2F)
            Alignment.ScreenAxisX.RIGHT -> screenW - w - x
        }
        val verticalOffset = when (component.alignment.verticalAlignment) {
            Alignment.ScreenAxisY.TOP -> y
            Alignment.ScreenAxisY.CENTER -> y - screenH / 2F
            Alignment.ScreenAxisY.CENTER_TRANSLATED -> y - (screenH / 2F - h / 2F)
            Alignment.ScreenAxisY.BOTTOM -> screenH - h - y
        }

        component.alignment.horizontalOffset = (horizontalOffset * scale).roundToInt()
        component.alignment.verticalOffset = (verticalOffset * scale).roundToInt()
    }

    /**
     * Keeps the component on screen and snaps it to an edge when it gets close to one.
     */
    private fun snap(value: Float, min: Float, max: Float): Float {
        val upper = max.coerceAtLeast(min)
        return when {
            value <= min + SNAP_DISTANCE -> min
            value >= upper - SNAP_DISTANCE -> upper
            else -> value.coerceIn(min, upper)
        }
    }

    // ---------------------------------------------------------------- input

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        if (slider != null || drag != null) {
            return true
        }

        val x = click.x
        val y = click.y
        val button = click.button()

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
        drag?.let { active ->
            applyPosition(
                active.component,
                active.startX + (click.x - active.mouseX).toFloat(),
                active.startY + (click.y - active.mouseY).toFloat(),
            )
            return true
        }

        val active = slider ?: return super.mouseDragged(click, offsetX, offsetY)
        if (click.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return true
        }

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
                val first = current.start as Float
                val second = current.endInclusive as Float
                if (active.secondHandle) {
                    value.set((first..newValue.toFloat().coerceAtLeast(first)) as Any)
                } else {
                    value.set((newValue.toFloat().coerceAtMost(second)..second) as Any)
                }
            }
            is IntRange -> {
                val first = current.first
                val last = current.last
                if (active.secondHandle) {
                    value.set((first..newValue.roundToInt().coerceAtLeast(first)) as Any)
                } else {
                    value.set((newValue.roundToInt().coerceAtMost(last)..last) as Any)
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
        if (drag != null) {
            drag = null
            return true
        }
        if (slider != null) {
            slider = null
            return true
        }
        return super.mouseReleased(click)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (selected != null && mouseX in settingsX.toDouble()..(settingsX + settingsWidth).toDouble()) {
            settingsScroll = (settingsScroll - verticalAmount.toFloat() * 12F).coerceAtLeast(0F)
            return true
        }

        if (pickerOpen &&
            mouseX in pickerX.toDouble()..(pickerX + pickerWidth).toDouble() &&
            mouseY >= pickerTop
        ) {
            pickerScroll = (pickerScroll - verticalAmount.toFloat() * 12F).coerceAtLeast(0F)
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        val editing = editingText
        if (editing != null) {
            when (input.key) {
                InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
                    editing.setByString(textBuffer)
                    editingText = null
                }
                InputConstants.KEY_ESCAPE -> editingText = null
                InputConstants.KEY_BACKSPACE -> if (textBuffer.isNotEmpty()) textBuffer = textBuffer.dropLast(1)
            }
            return true
        }

        if (input.key == InputConstants.KEY_ESCAPE) {
            if (contextMenu != null) {
                contextMenu = null
                return true
            }
            if (pickerOpen) {
                pickerOpen = false
                return true
            }
            ModuleHud.hudEditorSelected = false
            mc.gui.setScreen(parent)
            return true
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        val codePoint = input.codepoint()
        if (codePoint < 32) {
            return super.charTyped(input)
        }

        if (editingText != null) {
            textBuffer += codePoint.toChar()
            return true
        }

        return super.charTyped(input)
    }

    override fun onClose() {
        ModuleHud.hudEditorSelected = false
        super.onClose()
    }

    override fun isPauseScreen() = false

    // ---------------------------------------------------------------- helpers

    @Suppress("UNCHECKED_CAST")
    private fun setRaw(value: Value<*>, newValue: Any) {
        (value as Value<Any>).set(newValue)
    }

    private companion object {
        const val ROW_HEIGHT = 14F
        const val HEADER = 16F
        const val SNAP_DISTANCE = 4F
        const val TOOLTIP_WIDTH = 200

        val BACKDROP = 0x60000000.toInt()
        val PREVIEW = 0x40007FFF.toInt()
        val PANEL_BG = 0xE01A1A20.toInt()
        val HEADER_BG = 0xFF26262E.toInt()
        val ROW_HOVER = 0xFF30303A.toInt()
        val SCROLLBAR = 0xFF3A3A46.toInt()
        val OFF = 0xFF3A3A46.toInt()
        val OUTLINE = 0xFF55555F.toInt()

        val ACCENT = 0xFF5C9DFF.toInt()
        val TEXT_ON = 0xFFE8E8EC.toInt()
        val TEXT_OFF = 0xFF9A9AA6.toInt()
        val GROUP_TITLE = 0xFF9FC3FF.toInt()
    }

}
