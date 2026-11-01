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

import net.solidanchor.render.engine.type.BoundingBox2f
import net.solidanchor.utils.client.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

/**
 * Shared colors and drawing helpers for the native HUD components.
 *
 * Everything is rendered through the vanilla [GuiGraphicsExtractor], so no browser is involved.
 */
internal object HudStyle {

    val ACCENT: Int = 0xFF4677FF.toInt()
    val BACKGROUND: Int = 0xA0101018.toInt()
    val BACKGROUND_LIGHT: Int = 0xC0181824.toInt()
    val TEXT: Int = 0xFFFFFFFF.toInt()
    val TEXT_DIM: Int = 0xFFA0A0AA.toInt()
    val BORDER: Int = 0xFF2A2A38.toInt()

}

internal fun GuiGraphicsExtractor.hudText(
    text: String,
    x: Int,
    y: Int,
    color: Int = HudStyle.TEXT,
    shadow: Boolean = false,
) = text(mc.font, Component.literal(text), x, y, color, shadow)

internal fun GuiGraphicsExtractor.hudTextWidth(text: String): Int = mc.font.width(text)

internal fun GuiGraphicsExtractor.hudPanel(bounds: BoundingBox2f, color: Int = HudStyle.BACKGROUND) {
    fill(bounds.xMin.toInt(), bounds.yMin.toInt(), bounds.xMax.toInt(), bounds.yMax.toInt(), color)
}

internal fun GuiGraphicsExtractor.hudBorder(bounds: BoundingBox2f, color: Int = HudStyle.BORDER) {
    val left = bounds.xMin.toInt()
    val top = bounds.yMin.toInt()
    val right = bounds.xMax.toInt()
    val bottom = bounds.yMax.toInt()

    fill(left, top, right, top + 1, color)
    fill(left, bottom - 1, right, bottom, color)
    fill(left, top, left + 1, bottom, color)
    fill(right - 1, top, right, bottom, color)
}
