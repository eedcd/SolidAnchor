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

package com.github.eedcd.solidanchor.features.module.modules.movement.speed.modes.matrix

import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import com.github.eedcd.solidanchor.utils.entity.moving
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import com.github.eedcd.solidanchor.utils.math.sq

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
