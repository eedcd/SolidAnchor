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
package com.github.eedcd.solidanchor.features.module.modules.combat.velocity

import com.github.eedcd.solidanchor.event.EventManager
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.events.TransferOrigin
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.sequenceHandler
import com.github.eedcd.solidanchor.event.tickUntil
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityAAC442
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityBlocksMC
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityDexland
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityGrim2344
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityGrim2371
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityHylex
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityHypixel
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityIntave
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityJumpReset
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityLag
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityModify
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityReduce
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityReversal
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.mode.VelocityStrafe
import com.github.eedcd.solidanchor.utils.network.isLocalPlayerVelocity
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * Velocity module
 *
 * Modifies the amount of velocity you take.
 */

object ModuleVelocity : ClientModule("Velocity", ModuleCategories.COMBAT, aliases = listOf("AntiKnockBack")) {

    val modes = choices(
        "Mode", VelocityModify, arrayOf(
            // Generic modes
            VelocityModify,
            VelocityReversal,
            VelocityStrafe,
            VelocityJumpReset,
            VelocityLag,
            VelocityReduce,

            // Server modes
            VelocityHypixel,
            VelocityDexland,
            VelocityHylex,
            VelocityBlocksMC,

            // Anti cheat modes
            VelocityGrim2371,
            VelocityGrim2344,
            VelocityAAC442,
            VelocityIntave,
        )
    ).apply(::tagBy)

    private val delay by intRange("Delay", 0..0, 0..40, "ticks")
    private val pauseOnFlag by int("PauseOnFlag", 0, 0..20, "ticks")

    internal var pause = 0

    @Suppress("unused")
    private val pauseHandler = handler<GameTickEvent> {
        if (pause > 0) {
            pause--
        }
    }

    @Suppress("unused")
    private val packetHandler = sequenceHandler<PacketEvent>(priority = 1) { event ->
        val packet = event.packet

        if (!event.original || pause > 0) {
            return@sequenceHandler
        }

        if (packet.isLocalPlayerVelocity()) {
            // When delay is above 0, we will delay the velocity update
            if (delay.last > 0) {
                event.cancelEvent()

                delay.random().let { ticks ->
                    if (ticks > 0) {
                        val timeToWait = System.currentTimeMillis() + (ticks * 50L)

                        tickUntil { System.currentTimeMillis() >= timeToWait }
                    }
                }

                val packetEvent = PacketEvent(TransferOrigin.INCOMING, packet, false)
                EventManager.callEvent(packetEvent)

                if (!packetEvent.isCancelled) {
                    @Suppress("UNCHECKED_CAST")
                    (packet as Packet<ClientGamePacketListener>).handle(network)
                }
            }
        } else if (packet is ClientboundPlayerPositionPacket) {
            pause = pauseOnFlag
        }
    }

}
