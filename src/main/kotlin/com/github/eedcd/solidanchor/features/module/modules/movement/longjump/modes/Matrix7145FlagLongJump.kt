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

package com.github.eedcd.solidanchor.features.module.modules.movement.longjump.modes

import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.tickHandler
import com.github.eedcd.solidanchor.event.tickUntil
import com.github.eedcd.solidanchor.event.waitTicks
import com.github.eedcd.solidanchor.features.module.modules.movement.longjump.ModuleLongJump
import com.github.eedcd.solidanchor.utils.entity.airTicks
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import com.github.eedcd.solidanchor.utils.math.copy
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * @anticheat Matrix
 * @anticheatVersion 7.14.5
 * @testedOn mc.loyisa.cn
 */
internal object Matrix7145FlagLongJump : Mode("Matrix-7.14.5-Flag") {

    override val parent: ModeValueGroup<*>
        get() = ModuleLongJump.mode

    private val boostSpeed by float("BoostSpeed", 1.97f, 0.1f..5f)
    private val motionY by float("MotionY", 0.42f, 0.0f..5.0f)
    private val delay by int("Delay", 0, 0..3)

    private var flagTicks = 0
    private const val ACCEPTED_AIR_TIME = 5

    @Suppress("unused")
    private val tickHandler = tickHandler(onCancellation = { flagTicks = 0 }) {
        if (!player.onGround()) {
            return@tickHandler
        }

        // Wait until we are not on ground and reached the delay
        tickUntil { !player.onGround() && player.airTicks >= delay }

        val yaw = player.yRot
        // Repeat the jump until we get at least 2 flags and have not floated for too long
        while (flagTicks < 2 && player.airTicks < ACCEPTED_AIR_TIME) {
            player.deltaMovement = player.deltaMovement
                .withStrafe(speed = boostSpeed.toDouble(), yaw = yaw, input = null)
                .copy(y = motionY.toDouble())

            // On the first flag, we wait for the player to be on ground
            if (flagTicks == 1) {
                tickUntil { player.onGround() || player.airTicks >= ACCEPTED_AIR_TIME }
            }
            waitTicks(1)
        }

        // Reset
        tickUntil { player.onGround() }
        flagTicks = 0
        if (ModuleLongJump.autoDisable) {
            ModuleLongJump.enabled = false
        }
    }

    override fun disable() {
        flagTicks = 0
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (event.packet is ClientboundPlayerPositionPacket) {
            flagTicks++
        }
    }

}
