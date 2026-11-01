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
package net.solidanchor.features.module.modules.movement.speed.modes.intave

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.EventListener
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.entity.airTicks
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.multiply

/**
 * Intave 14 speed
 *
 * @author larryngton
 */
class SpeedIntave14(parent: ModeValueGroup<*>) : SpeedBHopBase("Intave14", parent) {
    companion object {
        private const val BOOST_CONSTANT = 0.003
    }

    private inner class Strafe(parent: EventListener) : ToggleableValueGroup(parent, "Strafe", true) {
        private val strength by float("Strength", 0.27f, 0.01f..0.27f)

        @Suppress("unused")
        private val tickHandler = handler<GameTickEvent> {
            if (player.isSprinting && (player.onGround() || player.airTicks == 11)) {
                player.deltaMovement = player.deltaMovement.withStrafe(strength = strength.toDouble())
            }
        }
    }

    private inner class AirBoost(parent: EventListener) : ToggleableValueGroup(parent, "AirBoost", true) {

        @Suppress("unused")
        private val tickHandler = handler<GameTickEvent> {
            if (player.deltaMovement.y > 0.003 && player.isSprinting) {
                player.deltaMovement = player.deltaMovement.multiply(
                    factorX = 1f + (BOOST_CONSTANT * 0.25),
                    factorZ = 1f + (BOOST_CONSTANT * 0.25),
                )
            }
        }
    }

    init {
        tree(Strafe(this))
        tree(AirBoost(this))
    }
}
