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

package com.github.eedcd.solidanchor.render.gui

import com.github.eedcd.solidanchor.render.drawQuad
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Square panel drawn behind GUI content.
 *
 * @param margin Space the panel takes around the content.
 */
data class GuiPanel(
    val fillColor: Color4b = Color4b.DEFAULT_BG_COLOR,
    val outlineColor: Color4b = Color4b.TRANSPARENT,
    val margin: Float = DEFAULT_PANEL_MARGIN,
) : ItemStackListBackground {

    override val padding: Float
        get() = margin

    override fun withOutline(color: Color4b) = copy(outlineColor = color)

    companion object {
        @JvmField
        val DEFAULT = GuiPanel()
    }

}

/**
 * Draws [panel] around a `0..width` by `0..height` box that starts at the current pose origin, returning whether
 * anything was drawn at all.
 */
internal fun GuiGraphicsExtractor.drawPanel(width: Float, height: Float, panel: GuiPanel): Boolean {
    if (panel.fillColor.isTransparent && panel.outlineColor.isTransparent) {
        return false
    }

    // Plain quad on the regular GUI pipeline: no rounded-rect shader, no glow layers.
    drawQuad(
        x1 = -panel.margin,
        y1 = -panel.margin,
        x2 = width + panel.margin,
        y2 = height + panel.margin,
        fillColor = panel.fillColor,
        outlineColor = panel.outlineColor,
    )
    return true
}

internal const val DEFAULT_PANEL_MARGIN = 2.0F
