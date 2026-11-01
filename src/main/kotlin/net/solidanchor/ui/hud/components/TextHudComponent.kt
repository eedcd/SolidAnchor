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
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.render.withPush
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.network.chat.Component

object TextHudComponent : NativeHudComponent(
    "Text",
    false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER,
        horizontalOffset = 0,
        verticalAlignment = Alignment.ScreenAxisY.CENTER,
        verticalOffset = 0,
    ),
    description = "Displays customizable text.",
) {

    private val textValue by text("Text", "Text")
    private val textColor by color("Color", Color4b.WHITE)
    private val size by int("Size", 14, 1..100)
    private val shadow by boolean("Shadow", false)

    /**
     * The vanilla font is 9px tall, so 14px (the former default) is the unscaled size.
     */
    private val textScale: Float
        get() = size / 14.0F

    override val guiScaledWidth: Float
        get() = (mc.font.width(textValue) * textScale).coerceAtLeast(1.0F)

    override val guiScaledHeight: Float
        get() = (mc.font.lineHeight * textScale).coerceAtLeast(1.0F)

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val bounds = getGuiScaledBounds()
        val scale = textScale
        val color = textColor.argb

        with(event.context) {
            pose().withPush {
                translate(bounds.xMin, bounds.yMin)
                scale(scale)
                text(mc.font, Component.literal(textValue), 0, 0, color, shadow)
            }
        }
    }

}
