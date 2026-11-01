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
package net.solidanchor.features.module.modules.movement.speed.modes.vulcan

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.PlayerJumpEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.entity.movementSideways
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.anyNotEmpty
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.effect.MobEffects

/**
 * @anticheat Vulcan
 * @anticheatVersion V2.8.6
 * @testedOn anticheat-test.com, eu.loyisa.cn
 * @note flags on specific blocks such as fences
 */
class SpeedVulcanGround286(parent: ModeValueGroup<*>) : SpeedBHopBase("VulcanGround286", parent) {

    @Suppress("unused")
    private val afterJumpHandler = handler<GameTickEvent> {
        if (player.moving && collidesBottomVertical() && !mc.options.keyJump.isDown) {
            val speedEffect = player.getEffect(MobEffects.SPEED)
            val isAffectedBySpeed = speedEffect != null && speedEffect.amplifier > 0
            val isMovingSideways = player.input.movementSideways != 0f

            val strafe = when {
                isAffectedBySpeed -> 0.59
                isMovingSideways -> 0.41
                else -> 0.42
            }

            player.deltaMovement = player.deltaMovement.withStrafe(speed = strafe)
            player.deltaMovement.y = 0.005
        }
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is ServerboundMovePlayerPacket && collidesBottomVertical() && !mc.options.keyJump.isDown) {
            event.packet.y += 0.005
        }
    }

    private fun collidesBottomVertical() =
        world.getBlockCollisions(player, player.boundingBox.move(0.0, -0.005, 0.0)).anyNotEmpty()

    @Suppress("unused")
    private val jumpEvent = handler<PlayerJumpEvent> { event ->
        if (!mc.options.keyJump.isDown) {
            event.cancelEvent()
        }
    }

}

