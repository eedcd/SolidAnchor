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
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.entity.armorItems
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import kotlin.math.max

object BlockCounterHudComponent : NativeHudComponent(
    "BlockCounter",
    true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER,
        horizontalOffset = -20,
        verticalAlignment = Alignment.ScreenAxisY.CENTER_TRANSLATED,
        verticalOffset = 0,
    ),
    description = "Shows the number of blocks available for bridging.",
) {

    private const val ICON_SIZE = 16
    private const val GAP = 2

    private enum class IconPosition(override val tag: String) : Tagged {
        NONE("None"),
        LEFT("Left"),
        RIGHT("Right"),
        TOP("Top"),
        BOTTOM("Bottom"),
    }

    private val iconPosition by enumChoice("IconPosition", IconPosition.LEFT)

    override val guiScaledWidth: Float
        get() {
            if (mc.player == null) {
                return 0F
            }

            val textWidth = mc.font.width(blockCount().toString())

            return when (iconPosition) {
                IconPosition.NONE -> textWidth.toFloat()
                IconPosition.LEFT, IconPosition.RIGHT -> (ICON_SIZE + GAP + textWidth).toFloat()
                IconPosition.TOP, IconPosition.BOTTOM -> max(ICON_SIZE, textWidth).toFloat()
            }
        }

    override val guiScaledHeight: Float
        get() {
            if (mc.player == null) {
                return 0F
            }

            return when (iconPosition) {
                IconPosition.NONE, IconPosition.LEFT, IconPosition.RIGHT -> ICON_SIZE.toFloat()
                IconPosition.TOP, IconPosition.BOTTOM -> (ICON_SIZE + GAP + mc.font.lineHeight).toFloat()
            }
        }

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val context = event.context
        val bounds = getGuiScaledBounds()

        val text = blockCount().toString()
        val textWidth = mc.font.width(text)
        val lineHeight = mc.font.lineHeight

        val heldItem = player.mainHandItem
        val icon = if (heldItem.item is BlockItem) heldItem else ItemStack.EMPTY

        when (iconPosition) {
            IconPosition.NONE -> {
                context.hudText(
                    text,
                    bounds.xMin.toInt(),
                    (bounds.yMin + (ICON_SIZE - lineHeight) / 2).toInt(),
                    HudStyle.TEXT,
                    true,
                )
            }

            IconPosition.LEFT -> {
                if (!icon.isEmpty) {
                    context.item(icon, bounds.xMin.toInt(), bounds.yMin.toInt())
                }
                context.hudText(
                    text,
                    (bounds.xMin + ICON_SIZE + GAP).toInt(),
                    (bounds.yMin + (ICON_SIZE - lineHeight) / 2).toInt(),
                    HudStyle.TEXT,
                    true,
                )
            }

            IconPosition.RIGHT -> {
                context.hudText(
                    text,
                    bounds.xMin.toInt(),
                    (bounds.yMin + (ICON_SIZE - lineHeight) / 2).toInt(),
                    HudStyle.TEXT,
                    true,
                )
                if (!icon.isEmpty) {
                    context.item(icon, (bounds.xMax - ICON_SIZE).toInt(), bounds.yMin.toInt())
                }
            }

            IconPosition.TOP -> {
                if (!icon.isEmpty) {
                    context.item(icon, (bounds.xCenter - ICON_SIZE / 2).toInt(), bounds.yMin.toInt())
                }
                context.hudText(
                    text,
                    (bounds.xCenter - textWidth / 2).toInt(),
                    (bounds.yMin + ICON_SIZE + GAP).toInt(),
                    HudStyle.TEXT,
                    true,
                )
            }

            IconPosition.BOTTOM -> {
                context.hudText(
                    text,
                    (bounds.xCenter - textWidth / 2).toInt(),
                    bounds.yMin.toInt(),
                    HudStyle.TEXT,
                    true,
                )
                if (!icon.isEmpty) {
                    context.item(
                        icon,
                        (bounds.xCenter - ICON_SIZE / 2).toInt(),
                        (bounds.yMin + lineHeight + GAP).toInt(),
                    )
                }
            }
        }
    }

    private fun blockCount(): Int {
        var total = 0

        for (stack in player.inventory.nonEquipmentItems) {
            total += countInStack(stack)
        }

        total += countInStack(player.offhandItem)

        for (stack in player.armorItems) {
            total += countInStack(stack)
        }

        return total
    }

    private fun countInStack(stack: ItemStack): Int {
        return if (stack.isEmpty || stack.item !is BlockItem) {
            0
        } else {
            stack.count
        }
    }

}
