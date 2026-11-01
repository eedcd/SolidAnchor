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
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.tickUntil
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.movement.fly.ModuleFly
import net.solidanchor.utils.client.Timer
import net.solidanchor.utils.entity.horizontalSpeed
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.kotlin.Priority
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import kotlin.random.Random

/**
 * @anticheat Watchdog (NCP)
 * @anticheatVersion 21.01.25
 * @testedOn hypixel.net
 * @author @liquidsquid1
 */
object FlyHypixelFlat : Mode("HypixelFlat") {

    override val parent: ModeValueGroup<*>
        get() = ModuleFly.modes

    private val timer by float("Timer", 1.0f, 0.1f..1.0f)
    private val flySpeed by float("Speed", 1.66f, 0.8f..2.0f)

    private var flyTicks = 0
    private var isFlying = false

    override fun disable() {
        flyTicks = 0
        isFlying = false
        super.disable()
    }

    @Suppress("unused")
    private val speedHandler = tickHandler {
        tickUntil { isFlying }

        player.deltaMovement = player.deltaMovement.withStrafe(speed = 0.8)
        waitTicks(1)
        player.deltaMovement = player.deltaMovement.withStrafe(speed = flySpeed.toDouble())

        tickUntil { player.onGround() }
        ModuleFly.enabled = false
    }

    @Suppress("unused")
    private val velocityHandler = tickHandler {
        if (!isFlying) {
            return@tickHandler
        }

        flyTicks++
        if (flyTicks > 30) {
            return@tickHandler
        }

        Timer.requestTimerSpeed(timer, Priority.IMPORTANT_FOR_USAGE_1, ModuleFly)
        player.deltaMovement.y = 0.0314 + (Random.nextDouble() / 1000f)
        player.deltaMovement = player.deltaMovement.withStrafe(speed = player.horizontalSpeed)
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is ClientboundExplodePacket) {
            isFlying = true
        }
    }

}
