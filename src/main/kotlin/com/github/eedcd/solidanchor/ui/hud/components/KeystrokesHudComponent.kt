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

package com.github.eedcd.solidanchor.ui.hud.components

import com.github.eedcd.solidanchor.event.events.OverlayRenderEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.ui.hud.NativeHudComponent
import com.github.eedcd.solidanchor.utils.client.mc
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention
import com.github.eedcd.solidanchor.utils.render.Alignment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

object KeystrokesHudComponent : NativeHudComponent(
    "Keystrokes", false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.LEFT,
        horizontalOffset = 15,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 15,
    ),
    description = "Visualizes your movement key inputs.",
) {

    private val showSpace by boolean("ShowSpace", true)

    private const val KEY_SIZE = 16
    private const val GAP = 2
    private const val SPACE_HEIGHT = 6

    override val guiScaledWidth: Float
        get() = (KEY_SIZE * 3 + GAP * 2).toFloat()

    override val guiScaledHeight: Float
        get() = (KEY_SIZE * 2 + GAP * 2 + if (showSpace) SPACE_HEIGHT else 0).toFloat()

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        with(event.context) {
            val bounds = getGuiScaledBounds()
            val x = bounds.xMin.toInt()
            val y = bounds.yMin.toInt()

            drawKey("W", x + KEY_SIZE + GAP, y, mc.options.keyUp.isDown)
            drawKey("A", x, y + KEY_SIZE + GAP, mc.options.keyLeft.isDown)
            drawKey("S", x + KEY_SIZE + GAP, y + KEY_SIZE + GAP, mc.options.keyDown.isDown)
            drawKey("D", x + (KEY_SIZE + GAP) * 2, y + KEY_SIZE + GAP, mc.options.keyRight.isDown)

            if (showSpace) {
                val spaceY = y + KEY_SIZE * 2 + GAP * 2
                fill(
                    x,
                    spaceY,
                    x + KEY_SIZE * 3 + GAP * 2,
                    spaceY + SPACE_HEIGHT,
                    if (mc.options.keyJump.isDown) HudStyle.ACCENT else HudStyle.BACKGROUND_LIGHT,
                )
            }
        }
    }

    private fun GuiGraphicsExtractor.drawKey(label: String, x: Int, y: Int, pressed: Boolean) {
        fill(
            x,
            y,
            x + KEY_SIZE,
            y + KEY_SIZE,
            if (pressed) HudStyle.ACCENT else HudStyle.BACKGROUND_LIGHT,
        )

        val textWidth = hudTextWidth(label)
        hudText(
            label,
            x + (KEY_SIZE - textWidth) / 2,
            y + (KEY_SIZE - mc.font.lineHeight) / 2,
            if (pressed) HudStyle.TEXT else HudStyle.TEXT_DIM,
        )
    }

}
