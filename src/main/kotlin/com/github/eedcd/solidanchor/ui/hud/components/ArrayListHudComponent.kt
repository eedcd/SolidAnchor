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

package net.solidanchor.ui.hud.components

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment

object ArrayListHudComponent : NativeHudComponent(
    "ArrayList", true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.RIGHT,
        horizontalOffset = 2,
        verticalAlignment = Alignment.ScreenAxisY.TOP,
        verticalOffset = 2,
    ),
    description = "Shows currently enabled modules.",
) {

    private enum class Order(override val tag: String) : Tagged {
        Ascending("Ascending"),
        Descending("Descending"),
    }

    private enum class ItemAlignment(override val tag: String) : Tagged {
        Left("Left"),
        Right("Right"),
    }

    private val showTags by boolean("ShowTags", true)
    private val itemAlignment by enumChoice("ItemAlignment", ItemAlignment.Right)
    private val order by enumChoice("Order", Order.Descending)

    private const val LINE_HEIGHT = 10
    private const val PADDING = 2
    private const val ACCENT_BAR = 2

    private class Entry(val module: ClientModule, val text: String) {
        val width: Int get() = mc.font.width(text)
    }

    private fun moduleText(module: ClientModule): String {
        val tag = module.tag
        return if (showTags && !tag.isNullOrBlank()) "${module.name} ($tag)" else module.name
    }

    private fun displayedEntries(): List<Entry> {
        // Keep a stable base order (by name) before sorting by text width so the list does not
        // shuffle between frames when several modules have the same width.
        val entries = ModuleManager
            .filter { it.enabled && !it.hidden }
            .sortedBy { it.name }
            .map { Entry(it, moduleText(it)) }
            .sortedBy { it.width }

        return if (order == Order.Descending) entries.reversed() else entries
    }

    private fun widthOf(entries: List<Entry>): Float =
        (entries.maxOfOrNull { it.width } ?: 0).toFloat() + ACCENT_BAR + PADDING * 3

    private fun heightOf(entries: List<Entry>): Float =
        (entries.size * LINE_HEIGHT + PADDING * 2).toFloat()

    override val guiScaledWidth: Float
        get() = widthOf(displayedEntries())

    override val guiScaledHeight: Float
        get() = heightOf(displayedEntries())

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val entries = displayedEntries()
        if (entries.isEmpty()) {
            return@handler
        }

        val width = widthOf(entries)
        val height = heightOf(entries)
        val bounds = getGuiScaledBounds(width, height)

        with(event.context) {
            val left = bounds.xMin.toInt()
            val right = bounds.xMax.toInt()

            entries.forEachIndexed { index, entry ->
                val rowTop = bounds.yMin.toInt() + PADDING + index * LINE_HEIGHT
                val rowBottom = rowTop + LINE_HEIGHT - 1

                val textWidth = entry.width
                val textX: Int
                val barX: Int
                val backgroundLeft: Int
                val backgroundRight: Int

                if (itemAlignment == ItemAlignment.Right) {
                    backgroundRight = right - PADDING
                    textX = backgroundRight - PADDING - textWidth
                    barX = textX - PADDING - ACCENT_BAR
                    backgroundLeft = barX
                } else {
                    backgroundLeft = left + PADDING
                    barX = backgroundLeft
                    textX = backgroundLeft + ACCENT_BAR + PADDING
                    backgroundRight = textX + textWidth + PADDING
                }

                fill(backgroundLeft, rowTop, backgroundRight, rowBottom, HudStyle.BACKGROUND)
                fill(barX, rowTop, barX + ACCENT_BAR, rowBottom, HudStyle.ACCENT)
                hudText(entry.text, textX, rowTop, HudStyle.TEXT)
            }
        }
    }

}
