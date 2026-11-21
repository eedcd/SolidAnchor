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
package com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.events.PlayerMoveEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.sequenceHandler
import com.github.eedcd.solidanchor.event.waitTicks
import com.github.eedcd.solidanchor.utils.aiming.RotationManager
import com.github.eedcd.solidanchor.utils.combat.findEnemy
import com.github.eedcd.solidanchor.utils.entity.horizontalSpeed
import com.github.eedcd.solidanchor.utils.entity.rotation
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import com.github.eedcd.solidanchor.utils.network.isLocalPlayerVelocity
import com.github.eedcd.solidanchor.utils.raytracing.isLookingAtEntity

/**
 * Strafe velocity
 */
internal object VelocityStrafe : VelocityMode("Strafe") {

    private val delay by int("Delay", 2, 0..10, "ticks")
    private val strength by float("Strength", 1f, 0.1f..2f)

    object OnlyFacing: ToggleableValueGroup(this, "OnlyFacing", false) {
        val range by float("Range", 3.5f, 0.1f..6f)
    }

    init {
        tree(OnlyFacing)
    }

    private val untilGround by boolean("UntilGround", false)

    private var applyStrafe = false
    private var shouldStrafe = false

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (!OnlyFacing.enabled) return@handler
        val target = world.findEnemy(0f, OnlyFacing.range) ?: return@handler

        shouldStrafe = isLookingAtEntity(
            target,
            OnlyFacing.range.toDouble(),
            RotationManager.currentRotation ?: player.rotation
        ) != null
    }

    @Suppress("unused")
    private val packetHandler = sequenceHandler<PacketEvent> { event ->
        val packet = event.packet

        // Check if this is a regular velocity update
        if (packet.isLocalPlayerVelocity()) {
            if (OnlyFacing.enabled && !shouldStrafe) {
                return@sequenceHandler
            }

            // A few anti-cheats can be easily tricked by applying the velocity a few ticks after being damaged
            waitTicks(delay)

            // Apply strafe
            player.deltaMovement = player.deltaMovement.withStrafe(speed = player.horizontalSpeed * strength)

            if (untilGround) {
                applyStrafe = true
            }
        }
    }

    @Suppress("unused")
    private val moveHandler = handler<PlayerMoveEvent> { event ->
        if (player.onGround()) {
            applyStrafe = false
        } else if (applyStrafe) {
            event.movement = event.movement.withStrafe(speed = player.horizontalSpeed * strength)
        }
    }

}
