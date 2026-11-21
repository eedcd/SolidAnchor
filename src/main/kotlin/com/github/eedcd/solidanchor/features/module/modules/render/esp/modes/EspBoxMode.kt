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
package com.github.eedcd.solidanchor.features.module.modules.render.esp.modes

import net.ccbluex.fastutil.mapToArray
import com.github.eedcd.solidanchor.event.events.WorldRenderEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.render.esp.ModuleESP.getColor
import com.github.eedcd.solidanchor.render.WorldRenderEnvironment
import com.github.eedcd.solidanchor.render.drawBox
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.render.renderEnvironment
import com.github.eedcd.solidanchor.render.withPositionRelativeToCamera
import com.github.eedcd.solidanchor.utils.math.KeyedAabb
import com.github.eedcd.solidanchor.utils.math.mergeIntersectingAabbsSweep
import com.github.eedcd.solidanchor.utils.math.worldToLocal
import net.minecraft.world.phys.AABB

object EspBoxMode : EspMode.BoxBased("Box") {

    private val outline by boolean("Outline", true)
    private val mergeIntersecting by boolean("MergeIntersecting", false)

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        event.renderEnvironment {
            val preparedBoxes = collectPreparedBoxes(event.partialTicks)

            if (!mergeIntersecting) {
                for ((entity, localBox, position) in preparedBoxes) {
                    withPositionRelativeToCamera(position) {
                        drawColoredBox(localBox, getColor(entity))
                    }
                }
                return@renderEnvironment
            }

            val mergedBoxes = mergeIntersectingAabbsSweep(
                preparedBoxes.mapToArray { (entity, _, _, worldBox) ->
                    KeyedAabb(worldBox, getColor(entity))
                }.asList()
            )

            for ((box, color) in mergedBoxes) {
                val (origin, localBox) = box.worldToLocal()
                withPositionRelativeToCamera(origin) {
                    drawColoredBox(localBox, color)
                }
            }
        }
    }

    private fun WorldRenderEnvironment.drawColoredBox(box: AABB, color: Color4b) {
        val baseColor = color.with(a = 50)
        val outlineColor = color.with(a = 100).takeIf { outline }
        drawBox(box, baseColor, outlineColor)
    }

}
