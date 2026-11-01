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
package net.solidanchor.features.module.modules.player.nofall.modes

import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.player.nofall.ModuleNoFall
import net.solidanchor.utils.client.Timer
import net.solidanchor.utils.entity.doesNotCollideBelow
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.network.MovePacketType

internal object NoFallHypixelPacket : NoFallMode("HypixelPacket") {

    private val void by boolean("OverVoid", false)

    private fun voidCheck(): Boolean {
        return (!player.doesNotCollideBelow() && !void || void)
    }

    val repeatable = tickHandler {
        if (player.fallDistance - player.deltaMovement.y >= 3.3 && voidCheck()) {
            Timer.requestTimerSpeed(0.5f, Priority.IMPORTANT_FOR_PLAYER_LIFE, ModuleNoFall)
            network.send(MovePacketType.ON_GROUND_ONLY.generatePacket().apply {
                onGround = true
            })
            player.fallDistance = 0.0
            waitTicks(1)
            Timer.requestTimerSpeed(1f, Priority.NORMAL, ModuleNoFall)
        }
    }

}
