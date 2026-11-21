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
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.speed.ModuleSpeed
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.client.Timer
import net.solidanchor.utils.entity.airTicks
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.math.multiply

/**
 * Intave 14 speed (~8.7% faster than vanilla)
 *
 * @author larryngton
 */
class SpeedIntave14Fast(parent: ModeValueGroup<*>) : SpeedBHopBase("Intave14Fast", parent) {
    private val timer by boolean("Timer", true)

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        when (player.airTicks) {
            1 -> {
                player.deltaMovement = player.deltaMovement.multiply(factorX = 1.04F, factorZ = 1.04F)
            }

            2, 3, 4 -> {
                player.deltaMovement = player.deltaMovement.multiply(factorX = 1.02F, factorZ = 1.02F)
            }
        }

        if (timer) {
            Timer.requestTimerSpeed(1.002f, Priority.NOT_IMPORTANT, ModuleSpeed)
        }
    }
}
