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

package com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.hypixel

import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.tickHandler
import com.github.eedcd.solidanchor.event.tickUntil
import com.github.eedcd.solidanchor.event.waitTicks
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.ModuleFly
import com.github.eedcd.solidanchor.utils.client.Timer
import com.github.eedcd.solidanchor.utils.entity.horizontalSpeed
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import com.github.eedcd.solidanchor.utils.kotlin.Priority
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
