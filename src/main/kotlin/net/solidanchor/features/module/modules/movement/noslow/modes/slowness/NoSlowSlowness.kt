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
package net.solidanchor.features.module.modules.movement.noslow.modes.slowness

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.noslow.ModuleNoSlow
import net.minecraft.resources.Identifier
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

internal object NoSlowSlowness : ToggleableValueGroup(ModuleNoSlow, "Slowness", true) {
    private val multiplier by float("PerLevelMultiplier", 0f, 0f..0.15f)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> { setSlownessMultiplier(multiplier) }

    private fun setSlownessMultiplier(multiplier: Float) {
        val slowness = player.activeEffectsMap[MobEffects.SLOWNESS]?.amplifier ?: return
        player.attributes.getInstance(Attributes.MOVEMENT_SPEED)?.addOrUpdateTransientModifier(
            AttributeModifier(
                Identifier.parse("effect.slowness"),
                -multiplier * (slowness + 1.0),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        )
    }

    override fun onDisabled() {
        setSlownessMultiplier(0.15f)
    }
}
