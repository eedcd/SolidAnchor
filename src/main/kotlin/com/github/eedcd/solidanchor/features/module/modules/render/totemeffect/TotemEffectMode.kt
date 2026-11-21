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

package net.solidanchor.features.module.modules.render.totemeffect

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.render.totemeffect.ModuleTotemEffect.TotemPopSnapshot
import net.solidanchor.features.module.modules.render.totemeffect.ModuleTotemEffect.entities
import net.solidanchor.render.WorldRenderEnvironment
import net.solidanchor.render.renderEnvironment
import net.solidanchor.render.withPush
import net.solidanchor.utils.math.Easing

abstract class TotemEffectMode(name: String) : Mode(name) {
    final override val parent: ModeValueGroup<*>
        get() = ModuleTotemEffect.modes

    val lifetime by int("Lifetime", 20, 1..200, aliases = listOf("lifetime")).onChanged { entities.clear() }
    protected val fade by float("Fade", 0.7f, 0f..1f)
    protected val animCurve by easing("AnimCurve", Easing.EXPONENTIAL_OUT)
    protected val canBeCovered by boolean("CanBeCovered", false)

    protected abstract fun WorldRenderEnvironment
        .drawTotemEffect(progress: Float, age: Float, entity: TotemPopSnapshot, fade: Float)

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        event.renderEnvironment {
            entities.forEach {
                val entity = it.value
                val age = lifetime - entities.timeToDie(it) + event.partialTicks

                val progress = animCurve
                    .transform(age / lifetime)
                    .coerceIn(0f, 1f)

                val fade = if (progress >= fade) (progress - fade) / (1f - fade).coerceAtLeast(0.001f) else 0f

                poseStack.withPush {
                    drawTotemEffect(progress, age, entity, fade)
                }
            }
        }
    }
}
