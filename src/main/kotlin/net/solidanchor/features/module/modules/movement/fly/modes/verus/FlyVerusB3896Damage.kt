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

package net.solidanchor.features.module.modules.movement.fly.modes.verus

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.movement.fly.ModuleFly
import net.solidanchor.features.module.modules.movement.fly.ModuleFly.modes
import net.solidanchor.utils.client.Timer
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.movement.stopXZVelocity
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * @anticheat Verus
 * @anticheatVersion b3896
 * @testedOn eu.loyisa.cn
 * @note it gives you ~2 flags for damage
 */
internal object FlyVerusB3896Damage : Mode("VerusB3896Damage") {

    override val parent: ModeValueGroup<*>
        get() = modes

    private var flyTicks = 0
    private var shouldStop = false
    private var gotDamage = false

    override fun enable() {
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false,
            player.horizontalCollision))
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y + 3.25, player.z, false,
            player.horizontalCollision))
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false,
            player.horizontalCollision))
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, true,
            player.horizontalCollision))
    }

    @Suppress("unused")
    val failRepeatable = tickHandler {
        if (!gotDamage) {
            waitTicks(20)
            if (!gotDamage) {
                chat("Failed to self-damage")
                shouldStop = true
            }
        }
    }
    val repeatable = tickHandler {
        if (player.hurtTime > 0) {
            gotDamage = true
        }

        if (!gotDamage) {
            return@tickHandler
        }

        if (++flyTicks > 20 || shouldStop) {
            ModuleFly.enabled = false
            return@tickHandler
        }

        player.deltaMovement = player.deltaMovement.withStrafe(speed = 9.95)
        player.deltaMovement.y = 0.0
        Timer.requestTimerSpeed(0.1f, Priority.IMPORTANT_FOR_USAGE_2, ModuleFly)
    }

    override fun disable() {
        flyTicks = 0
        player.stopXZVelocity()
    }
}
