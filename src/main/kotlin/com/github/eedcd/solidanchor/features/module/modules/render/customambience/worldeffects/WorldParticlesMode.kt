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

package net.solidanchor.features.module.modules.render.customambience.worldeffects

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.render.customambience.worldeffects.WorldParticles.coords
import net.solidanchor.render.WorldRenderEnvironment
import net.solidanchor.render.renderEnvironment
import net.solidanchor.render.withPositionRelativeToCamera
import net.solidanchor.render.withPush
import net.solidanchor.utils.math.Easing

abstract class WorldParticlesMode(name: String) : Mode(name) {

    final override val parent: ModeValueGroup<*>
        get() = WorldParticles.modes

    protected val radius by intRange("Radius", 8..16, 1..256)
    protected val count by int("Count", 256, 32..1024)
    protected val size by float("Size", 1f, 0.1f..10f)
    protected val lifetime by intRange("Life", 20..30, 1..512, "anim/life").onChanged { coords.clear() }
    protected val spawnTime by int("SpawnTime", 25, 1..200, "ms")
    protected val animCurve by easing("AnimCurve", Easing.QUAD_IN_OUT)
    protected val canBeCovered by boolean("CanBeCovered", true)

    private val yMotion = YMotion()

    private class YMotion : ToggleableValueGroup(null, "YMotion", false) {
        val motion by float("Motion", 2f, -10f..10f)
        val animBy by enumChoice("AnimBy", AnimBy.PROGRESS)
    }

    init {
        tree(yMotion)
    }

    protected var lastSpawnTime = 0L

    protected abstract fun WorldRenderEnvironment.drawWorldParticle(progress: Float, age: Float)
    protected abstract fun createParticleCoord(currentTime: Long)

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        val player = mc.player ?: return@handler

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime >= spawnTime) createParticleCoord(currentTime)

        event.renderEnvironment {
            val rot = mc.gameRenderer.mainCamera().rotation()

            coords.forEach {
                val age = lifetime.last - coords.timeToDie(it) + event.partialTicks

                val progress = animCurve
                    .transform(age / lifetime.first)
                    .coerceIn(0f, 1f)

                withPositionRelativeToCamera(it.value) {
                    poseStack.withPush {
                        if (yMotion.enabled) {
                            val anim = when (yMotion.animBy) {
                                AnimBy.PROGRESS -> progress
                                AnimBy.AGE -> age
                            }
                            translate(0F, yMotion.motion * anim, 0F)
                        }

                        rotate(rot)
                        drawWorldParticle(progress, age)
                    }
                }
            }
        }
    }

    @Suppress("unused")
    protected enum class AnimBy(
        override val tag: String,
    ) : Tagged {
        PROGRESS("Progress"),
        AGE("Age");
    }

}
