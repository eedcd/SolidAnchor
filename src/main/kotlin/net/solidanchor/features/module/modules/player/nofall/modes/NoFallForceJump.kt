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

import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.math.anyNotEmpty
import net.solidanchor.utils.math.copy
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * NoFallForceJump mode for the NoFall module.
 * This mode forces the player to jump just when his about to land,
 * preventing fall damage.
 */
internal object NoFallForceJump : NoFallMode("ForceJump") {

    private val blockDistance by float("BlockDistance", 1f, 0.1f..5.0f)
    private val fallDistance by float("FallDistance", 3.35f, 3.35f..10.0f)
    private val jumpHeight by float("JumpHeight", 0.42f, 0.1f..0.42f)

    private var jumpTriggered = false

    /**
     * Handles the packet event to check if a force jump should be triggered.
     * This is done by checking if the player's fall distance is higher than the specific (fallDistance)
     * and if the player is above a nonair block by the specific block distance.
     */
    val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet is ServerboundMovePlayerPacket && player.fallDistance > fallDistance) {
            if (!jumpTriggered && collidesBottomVertical()) {
                forceJump()
            }
        }

        if (player.onGround()) {
            jumpTriggered = false
        }
    }

    private fun collidesBottomVertical() =
        world.getBlockCollisions(player, player.boundingBox.move(0.0, (-blockDistance).toDouble(), 0.0)).anyNotEmpty()

    /**
     * Forces the player to jump by setting their velocity.
     */
    private fun forceJump() {
        player.jumpFromGround()
        player.deltaMovement = player.deltaMovement.copy(y = jumpHeight.toDouble())
        jumpTriggered = true
    }
}
