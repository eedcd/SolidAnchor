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

import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.collection.itemSortedSetOf
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object InventoryStatisticsHudComponent : NativeHudComponent(
    "InventoryStatistics", false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER_TRANSLATED,
        horizontalOffset = 220,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 15,
    ),
    description = "Tracks configured items in your inventory.",
) {

    private val items by items(
        "Items",
        itemSortedSetOf(
            Items.EMERALD,
            Items.DIAMOND,
            Items.GOLD_INGOT,
            Items.IRON_INGOT,
            Items.COPPER_INGOT,
        ),
    )
    private val showEmpty by boolean("ShowEmpty", true)
    private val rowLength by int("RowLength", 1, 1..9)

    private const val ICON_SIZE = 16
    private const val GAP = 2
    private const val ROW_HEIGHT = 18

    private fun countOf(item: Item): Int =
        player.inventory.nonEquipmentItems.sumOf { stack -> if (stack.item === item) stack.count else 0 }

    private fun entries(): List<Pair<Item, Int>> = items
        .map { it to countOf(it) }
        .filter { showEmpty || it.second > 0 }

    private fun cellWidth(entries: List<Pair<Item, Int>>): Int =
        ICON_SIZE + GAP + mc.font.width("x${entries.maxOf { it.second }}") + GAP

    private fun rowCount(entries: List<Pair<Item, Int>>): Int =
        (entries.size + rowLength - 1) / rowLength

    override val guiScaledWidth: Float
        get() {
            val entries = entries()
            if (entries.isEmpty()) {
                return 0F
            }
            return (minOf(entries.size, rowLength) * cellWidth(entries)).toFloat()
        }

    override val guiScaledHeight: Float
        get() {
            val entries = entries()
            if (entries.isEmpty()) {
                return 0F
            }
            return (rowCount(entries) * ROW_HEIGHT).toFloat()
        }

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val entries = entries()
        if (entries.isEmpty()) {
            return@handler
        }

        with(event.context) {
            val bounds = getGuiScaledBounds()
            val x = bounds.xMin.toInt()
            val y = bounds.yMin.toInt()
            val cellWidth = cellWidth(entries)

            entries.forEachIndexed { index, (trackedItem, count) ->
                val column = index % rowLength
                val row = index / rowLength
                val cellX = x + column * cellWidth
                val cellY = y + row * ROW_HEIGHT

                item(trackedItem.defaultInstance, cellX, cellY + (ROW_HEIGHT - ICON_SIZE) / 2)
                hudText(
                    "x$count",
                    cellX + ICON_SIZE + GAP,
                    cellY + (ROW_HEIGHT - mc.font.lineHeight) / 2,
                    if (count > 0) HudStyle.TEXT else HudStyle.TEXT_DIM,
                )
            }
        }
    }

}
