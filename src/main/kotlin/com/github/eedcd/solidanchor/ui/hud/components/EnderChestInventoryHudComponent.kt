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
import net.solidanchor.render.engine.type.BoundingBox2f
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.inventory.EnderChestInventoryTracker
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.item.ItemStack

object EnderChestInventoryHudComponent : NativeHudComponent(
    "EnderChestInventory", false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.RIGHT,
        horizontalOffset = 35,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 170,
    ),
    description = "Displays your Ender Chest contents.",
) {

    private const val SLOT_SIZE = 18
    private const val SLOT_GAP = 2
    private const val COLUMNS = 9
    private const val ROWS = 3
    private const val ITEM_OFFSET = 1

    override val guiScaledWidth: Float
        get() = (SLOT_SIZE * COLUMNS + SLOT_GAP * (COLUMNS - 1)).toFloat()

    override val guiScaledHeight: Float
        get() = (SLOT_SIZE * ROWS + SLOT_GAP * (ROWS - 1)).toFloat()

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val stacks = EnderChestInventoryTracker.stacks

        with(event.context) {
            val bounds = getGuiScaledBounds()
            val x = bounds.xMin.toInt()
            val y = bounds.yMin.toInt()

            for (row in 0 until ROWS) {
                for (column in 0 until COLUMNS) {
                    val index = row * COLUMNS + column
                    val slotX = x + column * (SLOT_SIZE + SLOT_GAP)
                    val slotY = y + row * (SLOT_SIZE + SLOT_GAP)
                    val slotBounds = BoundingBox2f(
                        slotX.toFloat(),
                        slotY.toFloat(),
                        (slotX + SLOT_SIZE).toFloat(),
                        (slotY + SLOT_SIZE).toFloat(),
                    )

                    fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, HudStyle.BACKGROUND_LIGHT)
                    hudBorder(slotBounds)

                    val stack = stacks.getOrNull(index) ?: ItemStack.EMPTY
                    if (!stack.isEmpty) {
                        item(stack, slotX + ITEM_OFFSET, slotY + ITEM_OFFSET)
                        itemDecorations(mc.font, stack, slotX + ITEM_OFFSET, slotY + ITEM_OFFSET, null)
                    }
                }
            }
        }
    }

}
