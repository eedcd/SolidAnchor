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

import net.solidanchor.SolidAnchor
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment

object WatermarkHudComponent : NativeHudComponent(
    "Watermark", true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.LEFT,
        horizontalOffset = 15,
        verticalAlignment = Alignment.ScreenAxisY.TOP,
        verticalOffset = 15,
    ),
    description = "Displays SolidAnchor branding and client information.",
) {

    private const val LINE_HEIGHT = 10
    private const val PADDING = 3

    private val serverAddress: String?
        get() = mc.currentServer?.ip

    private val clientTitle: String
        get() = "${SolidAnchor.CLIENT_NAME} ${SolidAnchor.clientVersion}"

    private val userName: String
        get() = mc.player?.gameProfile?.name ?: ""

    private val fpsText: String
        get() = "${mc.fps} FPS"

    override val guiScaledWidth: Float
        get() {
            val leftWidth = maxOf(mc.font.width(clientTitle), mc.font.width(userName))
            val rightWidth = maxOf(mc.font.width(serverAddress ?: ""), mc.font.width(fpsText))
            return (leftWidth + rightWidth + PADDING * 3).toFloat()
        }

    override val guiScaledHeight: Float
        get() = (LINE_HEIGHT * 2 + PADDING * 2).toFloat()

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        with(event.context) {
            val bounds = getGuiScaledBounds()

            hudPanel(bounds, HudStyle.BACKGROUND)
            hudBorder(bounds)

            val leftX = bounds.xMin.toInt() + PADDING
            val rightX = bounds.xMax.toInt() - PADDING
            val topY = bounds.yMin.toInt() + PADDING

            hudText(SolidAnchor.CLIENT_NAME, leftX, topY, HudStyle.ACCENT, true)
            hudText(
                " ${SolidAnchor.clientVersion}",
                leftX + hudTextWidth(SolidAnchor.CLIENT_NAME),
                topY,
                HudStyle.TEXT_DIM,
            )

            serverAddress?.let { address ->
                hudText(address, rightX - hudTextWidth(address), topY, HudStyle.TEXT_DIM)
            }

            hudText(player.gameProfile.name ?: "", leftX, topY + LINE_HEIGHT, HudStyle.TEXT)
            hudText(fpsText, rightX - hudTextWidth(fpsText), topY + LINE_HEIGHT, HudStyle.TEXT_DIM)
        }
    }

}
