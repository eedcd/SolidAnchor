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

package net.solidanchor.features.module.modules.movement.speed.modes.matrix

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.sq

/**
 * bypassing matrix version > 7
 * testing in 6/23/25 at loyisa
 *
 * @author XeContrast
 */
class SpeedMatrix7(parent : ModeValueGroup<*>) : SpeedBHopBase("Matrix7",parent) {

    @Suppress("unused")
    private val tickHandle = handler<GameTickEvent> {
        if (player.moving) {
            if (player.onGround()) {
                player.deltaMovement.y = 0.419652
                player.deltaMovement = player.deltaMovement.withStrafe()
            } else {
                if (player.deltaMovement.x.sq() + player.deltaMovement.z.sq() < 0.04) {
                    player.deltaMovement = player.deltaMovement.withStrafe()
                }
            }
        }
    }
}
