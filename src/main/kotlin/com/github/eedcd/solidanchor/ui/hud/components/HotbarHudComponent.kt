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
import net.solidanchor.ui.hud.HudComponentTweak
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.item.ItemStack

object HotbarHudComponent : NativeHudComponent(
    "Hotbar", false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER_TRANSLATED,
        horizontalOffset = 0,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 15,
    ),
    tweaks = arrayOf(
        HudComponentTweak.TWEAK_HOTBAR,
        HudComponentTweak.DISABLE_STATUS_BAR,
        HudComponentTweak.DISABLE_EXP_BAR,
        HudComponentTweak.DISABLE_HELD_ITEM_TOOL_TIP,
        HudComponentTweak.DISABLE_OVERLAY_MESSAGE,
        HudComponentTweak.DISABLE_LOCATOR_BAR,
    ),
    description = "Replaces the vanilla hotbar and status display.",
) {

    private const val SLOT_SIZE = 20
    private const val SLOT_GAP = 2
    private const val SLOT_COUNT = 9
    private const val ITEM_OFFSET = 2

    override val guiScaledWidth: Float
        get() = (SLOT_SIZE * SLOT_COUNT + SLOT_GAP * (SLOT_COUNT - 1)).toFloat()

    override val guiScaledHeight: Float
        get() = SLOT_SIZE.toFloat()

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val inventory = player.inventory
        val selectedSlot = inventory.selectedSlot
        val stacks = inventory.nonEquipmentItems

        with(event.context) {
            val bounds = getGuiScaledBounds()
            val x = bounds.xMin.toInt()
            val y = bounds.yMin.toInt()

            for (index in 0 until SLOT_COUNT) {
                val slotX = x + index * (SLOT_SIZE + SLOT_GAP)
                val slotBounds = BoundingBox2f(
                    slotX.toFloat(),
                    y.toFloat(),
                    (slotX + SLOT_SIZE).toFloat(),
                    (y + SLOT_SIZE).toFloat(),
                )

                fill(slotX, y, slotX + SLOT_SIZE, y + SLOT_SIZE, HudStyle.BACKGROUND_LIGHT)
                hudBorder(slotBounds, if (index == selectedSlot) HudStyle.ACCENT else HudStyle.BORDER)

                val stack = stacks.getOrNull(index) ?: ItemStack.EMPTY
                if (!stack.isEmpty) {
                    item(stack, slotX + ITEM_OFFSET, y + ITEM_OFFSET)
                    itemDecorations(mc.font, stack, slotX + ITEM_OFFSET, y + ITEM_OFFSET, null)
                }
            }
        }
    }

}
