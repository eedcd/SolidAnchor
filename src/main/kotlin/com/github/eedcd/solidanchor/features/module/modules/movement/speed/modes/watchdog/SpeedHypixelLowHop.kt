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
package net.solidanchor.features.module.modules.movement.speed.modes.watchdog

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PlayerJumpEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.entity.airTicks
import net.solidanchor.utils.entity.horizontalSpeed
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.anyNotEmpty
import net.minecraft.world.effect.MobEffects

/**
 * @anticheat Watchdog (NCP)
 * @anticheatVersion 25.01.25
 * @testedOn hypixel.net
 */
class SpeedHypixelLowHop(parent: ModeValueGroup<*>) : SpeedBHopBase("HypixelLowHop", parent) {

    companion {
        var shouldStrafe = false
    }

    private var glide by boolean("Glide", false)

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent> {
        shouldStrafe = false

        if (player.onGround()) {
            player.deltaMovement = player.deltaMovement.withStrafe()
            shouldStrafe = true
        } else {
            when (player.airTicks) {
                1 -> {
                    player.deltaMovement = player.deltaMovement.withStrafe()
                    shouldStrafe = true
                    player.deltaMovement.y += 0.0568
                }
                3 -> {
                    player.deltaMovement = player.deltaMovement.multiply(0.95, 1.0, 0.95)
                    player.deltaMovement.y -= 0.13
                }
                4 -> player.deltaMovement.y -= 0.2
                7 -> {
                    if (glide && isGroundExempt()) {
                        player.deltaMovement.y = 0.0
                    }
                }
            }

            if (isGroundExempt()) {
                player.deltaMovement = player.deltaMovement.withStrafe()
            }

            if (player.hurtTime == 9) {
                player.deltaMovement =
                    player.deltaMovement.withStrafe(speed = player.horizontalSpeed.coerceAtLeast(0.281))
            }

            if ((player.getEffect(MobEffects.SPEED)?.amplifier ?: 0) == 2) {
                when (player.airTicks) {
                    1, 2, 5, 6, 8 -> player.deltaMovement = player.deltaMovement.multiply(1.2, 1.0, 1.2)
                }
            }
        }
    }

    @Suppress("unused")
    private val jumpHandler = handler<PlayerJumpEvent> {
        val atLeast = 0.247 + 0.15 * (player.getEffect(MobEffects.SPEED)?.amplifier ?: 0)

        player.deltaMovement =
            player.deltaMovement.withStrafe(speed = player.horizontalSpeed.coerceAtLeast(atLeast))
        shouldStrafe = true
    }

    private fun isGroundExempt() =
        world.getBlockCollisions(player, player.boundingBox.move(0.0, -0.66, 0.0)).anyNotEmpty()
            && player.deltaMovement.y < 0

}
