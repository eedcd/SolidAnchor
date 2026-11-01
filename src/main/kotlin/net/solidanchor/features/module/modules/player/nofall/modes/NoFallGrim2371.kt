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

import net.solidanchor.event.EventState
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.tickHandler
import net.solidanchor.event.tickUntil
import net.solidanchor.event.until
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * Bypassing GrimAC Anti Cheat (8/3/2025, Loyisa Server)
 * Minecraft Version 1.9+
 *
 * @author XeContrast
 */
internal object NoFallGrim2371 : NoFallMode("Grim2371-1.9+") {

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (player.onGround() || player.fallDistance < 2.5) {
            return@tickHandler
        }

        until<PlayerNetworkMovementTickEvent> { event ->
            if (!player.onGround() || event.state != EventState.PRE) {
                return@until false
            }

            event.cancelEvent()
            network.send(ServerboundMovePlayerPacket.StatusOnly(true, player.horizontalCollision))
            true
        }

        until<MovementInputEvent> { event ->
            event.jump = true
            player.onGround()
        }

        tickUntil { player.onGround() }
    }

}
