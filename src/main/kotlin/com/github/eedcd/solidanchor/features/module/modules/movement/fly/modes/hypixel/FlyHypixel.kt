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

package net.solidanchor.features.module.modules.movement.fly.modes.hypixel

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.PlayerMoveEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.tickUntil
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.movement.fly.ModuleFly
import net.solidanchor.utils.client.Timer
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.kotlin.Priority
import net.minecraft.network.protocol.game.ClientboundExplodePacket

/**
 * @anticheat Watchdog (NCP)
 * @anticheatVersion 21.01.25
 * @testedOn hypixel.net
 * @author @liquidsquid1
 */
object FlyHypixel : Mode("Hypixel") {

    override val parent: ModeValueGroup<*>
        get() = ModuleFly.modes

    private val timer by float("Timer", 1.0f, 0.1f..1.0f)

    private var isFlying = false

    override fun disable() {
        isFlying = false
        super.disable()
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        tickUntil { isFlying }

        player.deltaMovement.y = 0.8
        waitTicks(1)
        player.deltaMovement = player.deltaMovement.withStrafe(speed = 1.9)
        player.deltaMovement.y = 1.0
        waitTicks(1)
        player.deltaMovement = player.deltaMovement.multiply(
            1.05,
            1.0,
            1.05
        )
        waitTicks(19)
        player.deltaMovement.y += 0.42

        tickUntil { player.onGround() }
        ModuleFly.enabled = false
    }

    @Suppress("unused")
    private val timerHandler = handler<GameTickEvent> {
        Timer.requestTimerSpeed(timer, Priority.IMPORTANT_FOR_USAGE_1, ModuleFly)
    }

    @Suppress("unused")
    private val strafeHandler = handler<PlayerMoveEvent> { event ->
        event.movement = event.movement.withStrafe()
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is ClientboundExplodePacket) {
            isFlying = true
        }
    }

}
