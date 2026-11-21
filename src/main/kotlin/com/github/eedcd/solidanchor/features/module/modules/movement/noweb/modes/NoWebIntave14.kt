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

package net.solidanchor.features.module.modules.movement.noweb.modes

import net.solidanchor.features.module.modules.movement.noweb.NoWebMode
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.withStrafe
import net.minecraft.core.BlockPos

/**
 * Intave needs to improve their movement checks
 * works on intave 14.8.4
 */
object NoWebIntave14 : NoWebMode("Intave14") {
    override fun handleEntityCollision(pos: BlockPos): Boolean {
        if (player.moving) {
            if (player.onGround()) {
                if (player.tickCount % 3 == 0) {
                    player.deltaMovement = player.deltaMovement.withStrafe(strength = 0.734)
                } else {
                    player.jumpFromGround()
                    player.deltaMovement = player.deltaMovement.withStrafe(strength = 0.346)
                }
            }
        }
        return false
    }
}
