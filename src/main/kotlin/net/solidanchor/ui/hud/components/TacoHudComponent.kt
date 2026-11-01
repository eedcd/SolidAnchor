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
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.client.gui.GuiGraphicsExtractor

object TacoHudComponent : NativeHudComponent(
    "Taco",
    false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.LEFT,
        horizontalOffset = 0,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 65,
    ),
    description = "Displays a friendly taco.",
) {

    private const val WIDTH = 60f
    private const val HEIGHT = 36f

    private const val SHELL_DARK = 0xFFD99C2B.toInt()
    private const val SHELL_LIGHT = 0xFFF3C969.toInt()
    private const val SHELL_HIGHLIGHT = 0xFFFFE29A.toInt()
    private const val MEAT = 0xFF7A4A21.toInt()
    private const val LETTUCE = 0xFF5FA83D.toInt()
    private const val CHEESE = 0xFFFFC93C.toInt()
    private const val TOMATO = 0xFFD9453D.toInt()

    override val guiScaledWidth: Float
        get() = WIDTH

    override val guiScaledHeight: Float
        get() = HEIGHT

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val bounds = getGuiScaledBounds()
        val x = bounds.xMin
        val y = bounds.yMin

        with(event.context) {
            // Bottom shell
            fillRect(x + 5, y + 27, x + 55, y + 34, SHELL_DARK)
            // Main shell
            fillRect(x + 3, y + 9, x + 57, y + 30, SHELL_DARK)
            // Upper shell edge
            fillRect(x + 7, y + 2, x + 53, y + 13, SHELL_LIGHT)
            fillRect(x + 4, y + 8, x + 56, y + 14, SHELL_LIGHT)
            fillRect(x + 10, y + 1, x + 50, y + 4, SHELL_HIGHLIGHT)
            // Filling
            fillRect(x + 8, y + 12, x + 52, y + 17, LETTUCE)
            fillRect(x + 10, y + 16, x + 50, y + 21, MEAT)
            fillRect(x + 12, y + 20, x + 48, y + 24, CHEESE)
            fillRect(x + 16, y + 15, x + 20, y + 18, TOMATO)
            fillRect(x + 30, y + 16, x + 36, y + 19, TOMATO)
            fillRect(x + 42, y + 14, x + 46, y + 17, TOMATO)
            // Shell front lip
            fillRect(x + 4, y + 24, x + 56, y + 29, SHELL_LIGHT)
            fillRect(x + 8, y + 29, x + 52, y + 32, SHELL_DARK)
        }
    }

    private fun GuiGraphicsExtractor.fillRect(
        x: Float,
        y: Float,
        right: Float,
        bottom: Float,
        color: Int,
    ) {
        fill(x.toInt(), y.toInt(), right.toInt(), bottom.toInt(), color)
    }

}
