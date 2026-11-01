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
package net.solidanchor.features.module.modules.render.esp.modes

import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.render.esp.ModuleESP.getColor
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.render.renderEnvironment
import net.solidanchor.utils.entity.RenderedEntities
import net.solidanchor.utils.entity.interpolateCurrentPosition
import net.solidanchor.utils.render.drawLegacy2DMarker

object EspLegacy2DMode : EspMode("Legacy2D") {

    private val scale by float("Scale", 0.1f, 0.02f..0.3f)
    private val yOffset by float("YOffset", 0f, -1f..1f)
    private val backgroundAlpha by int("BackgroundAlpha", 150, 0..255)

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        event.renderEnvironment {
            for (entity in RenderedEntities) {
                if (!shouldRender(entity)) continue

                val pos = entity.interpolateCurrentPosition(event.partialTicks).add(0.0, yOffset.toDouble(), 0.0)
                val color = getColor(entity).argb
                val backgroundColor = Color4b.BLACK.with(a = backgroundAlpha).argb

                drawLegacy2DMarker(
                    pos = pos,
                    entityHeight = entity.boundingBox.ysize,
                    scale = scale,
                    foregroundArgb = color,
                    backgroundArgb = backgroundColor
                )
            }
        }
    }
}
