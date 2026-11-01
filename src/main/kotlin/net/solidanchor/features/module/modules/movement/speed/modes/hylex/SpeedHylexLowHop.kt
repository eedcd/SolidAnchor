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
package net.solidanchor.features.module.modules.movement.speed.modes.hylex

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PlayerJumpEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.entity.horizontalSpeed
import net.solidanchor.utils.entity.moving

/**
 * Hylex LowHop
 *
 * Works because of a silly exemption from Hylex
 * @author @liquidsquid1
 */
class SpeedHylexLowHop(parent: ModeValueGroup<*>) : SpeedBHopBase("HylexLowHop", parent) {

    private var airTicks = 0

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (player.onGround()) {
            airTicks = 0
            if (player.moving && player.horizontalSpeed < 0.32) {

                player.deltaMovement = player.deltaMovement.multiply(
                    1.1,
                    1.0,
                    1.1
                )
            }
            return@handler
        }
        airTicks++

        if (airTicks == 9 && player.horizontalSpeed < 0.29) {
            player.deltaMovement = player.deltaMovement.multiply(
                1.007,
                1.0,
                1.007
            )
        }

        if (airTicks == 1 && player.horizontalSpeed < 0.20) {
            player.deltaMovement = player.deltaMovement.multiply(
                1.01,
                1.0,
                1.01
            )
        }

        if (player.deltaMovement.y > 0 && airTicks <= 2 && player.horizontalSpeed < 0.2) {
            player.deltaMovement = player.deltaMovement.multiply(
                1.02,
                1.0,
                1.02
            )
        }

    }

    override fun enable() {
        airTicks = 0
        super.enable()
    }

    @Suppress("unused")
    private val jumpHandler = handler<PlayerJumpEvent> { event ->
        event.motion = 0.33f
    }

}
