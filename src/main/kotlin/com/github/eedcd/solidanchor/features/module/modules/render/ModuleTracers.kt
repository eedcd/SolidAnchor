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
package com.github.eedcd.solidanchor.features.module.modules.render

import com.github.eedcd.solidanchor.event.events.WorldRenderEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.misc.FriendManager
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.render.GenericDistanceHSBColorMode
import com.github.eedcd.solidanchor.render.GenericEntityHealthColorMode
import com.github.eedcd.solidanchor.render.GenericRainbowColorMode
import com.github.eedcd.solidanchor.render.GenericStaticColorMode
import com.github.eedcd.solidanchor.render.drawLines
import com.github.eedcd.solidanchor.render.drawLinesWithWidth
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.render.engine.type.Vec3f
import com.github.eedcd.solidanchor.render.renderEnvironment
import com.github.eedcd.solidanchor.utils.combat.EntityTaggingManager
import com.github.eedcd.solidanchor.utils.entity.RenderedEntities
import com.github.eedcd.solidanchor.utils.entity.cameraDistanceSq
import com.github.eedcd.solidanchor.utils.entity.interpolateCurrentPosition
import com.github.eedcd.solidanchor.utils.math.sq
import com.github.eedcd.solidanchor.utils.math.toVec3f

/**
 * Tracers module
 *
 * Draws a line to every entity a certain radius.
 */

object ModuleTracers : ClientModule("Tracers", ModuleCategories.RENDER) {

    private val modes = choices("ColorMode", 0) {
        arrayOf(
            GenericDistanceHSBColorMode.entity(it),
            GenericEntityHealthColorMode(it),
            GenericStaticColorMode(it, Color4b(0, 160, 255, 255)),
            GenericRainbowColorMode(it)
        )
    }

    private val lineWidth by float("LineWidth", 1f, 1f..16f)

    private val maximumDistance by float("MaximumDistance", 128F, 1F..512F)

    override fun onEnabled() {
        RenderedEntities.subscribe(this)
    }

    override fun onDisabled() {
        RenderedEntities.unsubscribe(this)
    }

    val renderHandler = handler<WorldRenderEvent> { event ->
        if (RenderedEntities.isEmpty()) {
            return@handler
        }

        event.renderEnvironment {
            val eyeVector = Vec3f.eyeVector(camera)

            val maxDistanceSq = maximumDistance.sq()
            for (entity in RenderedEntities) {
                val distanceSq = entity.position().cameraDistanceSq().toFloat()
                if (distanceSq > maxDistanceSq) {
                    continue
                }

                val color = if (FriendManager.isFriend(entity)) {
                    Color4b.BLUE
                } else {
                    EntityTaggingManager.getTag(entity).color ?: modes.activeMode.getColor(entity)
                }

                val pos = entity.interpolateCurrentPosition(event.partialTicks).subtract(camera.position()).toVec3f()
                val topPos = pos.add(0f, entity.bbHeight, 0f)

                if (lineWidth == 1.0f) {
                    drawLines(color.argb, eyeVector, pos, pos, topPos)
                } else {
                    drawLinesWithWidth(color.argb, lineWidth, eyeVector, pos, pos, topPos)
                }
            }
        }

    }
}
