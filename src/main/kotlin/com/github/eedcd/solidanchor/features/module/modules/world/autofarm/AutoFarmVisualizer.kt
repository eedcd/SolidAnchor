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
package com.github.eedcd.solidanchor.features.module.modules.world.autofarm

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.WorldRenderEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.render.FULL_BOX
import com.github.eedcd.solidanchor.render.WorldRenderEnvironment
import com.github.eedcd.solidanchor.render.drawBox
import com.github.eedcd.solidanchor.render.drawBoxSides
import com.github.eedcd.solidanchor.render.drawLine
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.render.renderEnvironment
import com.github.eedcd.solidanchor.render.utils.rainbow
import com.github.eedcd.solidanchor.render.withPositionRelativeToCamera
import com.github.eedcd.solidanchor.utils.entity.interpolateCurrentPosition
import com.github.eedcd.solidanchor.utils.math.horizontalDistanceToSqr
import com.github.eedcd.solidanchor.utils.math.sq
import com.github.eedcd.solidanchor.utils.math.toVec3f

object AutoFarmVisualizer : ToggleableValueGroup(ModuleAutoFarm, "Visualize", true) {
    private object Path : ToggleableValueGroup(this, "Path", true) {
        val color by color("PathColor", Color4b(36, 237, 0, 255))

        override val running: Boolean
            get() = super.running && AutoFarmAutoWalk.running

        @Suppress("unused")
        private val renderHandler = handler<WorldRenderEvent> { event ->
            event.renderEnvironment {
                AutoFarmAutoWalk.walkTarget?.let { target ->
                    withPositionRelativeToCamera {
                        drawLine(
                            player.interpolateCurrentPosition(event.partialTicks).toVec3f(),
                            target.toVec3f(),
                            color.argb,
                        )
                    }
                }
            }
        }
    }

    private object Blocks : ToggleableValueGroup(this, "Blocks", true) {
        val outline by boolean("Outline", true)

        private val readyColor by color("ReadyColor", Color4b(36, 237, 0, 255))
        private val placeColor by color("PlaceColor", Color4b(191, 245, 66, 100))
        private val range by int("Range", 50, 10..128)

        private val colorRainbow by boolean("Rainbow", false)

        private val placeTargets by multiEnumChoice("PlaceTargets", AutoFarmTrackedState.Plantable.entries)

        private object CurrentTarget : ToggleableValueGroup(this.parent, "CurrentTarget", true) {
            private val color by color("Color", Color4b(66, 120, 245, 255))
            private val colorRainbow by boolean("Rainbow", false)

            fun render(renderEnvironment: WorldRenderEnvironment) {
                if (!this.enabled) return
                val target = ModuleAutoFarm.currentTarget ?: return
                with(renderEnvironment) {
                    withPositionRelativeToCamera(target) {
                        drawBox(FULL_BOX, if (colorRainbow) rainbow(alpha = 0.2f) else color.with(a = 50))
                    }
                }
            }
        }

        @Suppress("unused")
        private val renderHandler = handler<WorldRenderEvent> { event ->
            val baseColor = if (colorRainbow) rainbow() else readyColor

            val fillColor = baseColor.with(a = 50)

            event.renderEnvironment {
                CurrentTarget.render(this)
                for ((pos, type) in AutoFarmBlockTracker.iterate()) {
                    if (player.position().horizontalDistanceToSqr(pos) > range.sq()) continue

                    withPositionRelativeToCamera(pos) {
                        when (type) {
                            AutoFarmTrackedState.ReadyForHarvest -> {
                                drawBox(
                                    FULL_BOX,
                                    fillColor,
                                    if (outline) baseColor.with(a = 100) else null,
                                )
                            }

                            is AutoFarmTrackedState.Plantable -> if (type in placeTargets) {
                                val availableSides = type.findPlantableSides(pos, world.getBlockState(pos))
                                drawBoxSides(
                                    FULL_BOX,
                                    sides = availableSides,
                                    faceColor = placeColor,
                                    outlineColor = if (outline) baseColor.with(a = 100) else null,
                                )
                            }

                            AutoFarmTrackedState.Bonemealable -> {
                                // NOOP
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        tree(Path)
        tree(Blocks)
    }
}
