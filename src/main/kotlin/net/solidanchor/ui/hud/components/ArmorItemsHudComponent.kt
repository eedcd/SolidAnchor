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
import net.minecraft.world.item.ItemStack

object ArmorItemsHudComponent : NativeHudComponent(
    "ArmorItems",
    false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER_TRANSLATED,
        horizontalOffset = 240,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 15,
    ),
    description = "Displays your equipped armor.",
) {

    private const val ICON_SIZE = 16

    private enum class Layout(override val tag: String) : Tagged {
        VERTICAL("Vertical"),
        HORIZONTAL("Horizontal"),
    }

    private val layout by enumChoice("Layout", Layout.VERTICAL)

    override val guiScaledWidth: Float
        get() {
            if (mc.player == null) {
                return 0F
            }

            val count = armorStacks().size

            return when (layout) {
                Layout.VERTICAL -> ICON_SIZE.toFloat()
                Layout.HORIZONTAL -> (count * ICON_SIZE).toFloat()
            }
        }

    override val guiScaledHeight: Float
        get() {
            if (mc.player == null) {
                return 0F
            }

            val count = armorStacks().size

            return when (layout) {
                Layout.VERTICAL -> (count * ICON_SIZE).toFloat()
                Layout.HORIZONTAL -> ICON_SIZE.toFloat()
            }
        }

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val stacks = armorStacks()
        if (stacks.isEmpty()) {
            return@handler
        }

        val context = event.context
        val bounds = getGuiScaledBounds()

        for ((index, stack) in stacks.withIndex()) {
            val x = if (layout == Layout.VERTICAL) bounds.xMin else bounds.xMin + index * ICON_SIZE
            val y = if (layout == Layout.VERTICAL) bounds.yMin + index * ICON_SIZE else bounds.yMin

            context.item(stack, x.toInt(), y.toInt())
        }
    }

    private fun armorStacks(): List<ItemStack> {
        return player.armorItems.reversed().filter { !it.isEmpty }
    }

}
