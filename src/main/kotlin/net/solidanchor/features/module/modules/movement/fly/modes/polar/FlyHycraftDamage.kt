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

package net.solidanchor.features.module.modules.movement.fly.modes.polar

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.BlinkPacketEvent
import net.solidanchor.event.events.TransferOrigin
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.blink.BlinkManager
import net.solidanchor.features.module.modules.movement.fly.ModuleFly
import net.solidanchor.features.module.modules.movement.fly.ModuleFly.modes
import net.solidanchor.utils.network.handlePacket
import net.minecraft.network.protocol.common.ClientboundPingPacket
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket

/**
 * @anticheat Hycraft (Polar)
 * @anticheat Version 15.05.2024
 * @testedOn mc.hycraft.us
 *
 * @note Tested in Bedwars, Skywars. Pretty much flagless
 */
internal object FlyHycraftDamage : Mode("HycraftDamage") {

    override val parent: ModeValueGroup<*>
        get() = modes

    private var damageTaken = false
    private var release = false
    private var ticks = 0

    override fun enable() {
        ticks = 0
        damageTaken = false
        release = false
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        waitTicks(1)

        if (ticks > 0) {
            ticks--
        }
    }

    /**
     * Used to works on different servers as well but now only Hycraft
     */
    @Suppress("unused")
    private val packetHandler = handler<BlinkPacketEvent> { event ->
        val packet = event.packet

        if (event.origin != TransferOrigin.INCOMING) {
            return@handler
        }

        event.action = when (packet) {
            is ClientboundDamageEventPacket if packet.entityId == player.id && ticks <= 0 -> {
                damageTaken = true
                ticks = 40
                handlePacket(packet)
                BlinkManager.Action.QUEUE
            }

            is ClientboundSetEntityMotionPacket if packet.id == player.id && damageTaken -> {
                damageTaken = false
                release = true
                handlePacket(packet)
                BlinkManager.Action.QUEUE
            }

            is ClientboundPingPacket -> {
                if (ticks <= 0) {
                    if (release) {
                        ModuleFly.enabled = false
                    }
                    return@handler
                }

                ticks--
                BlinkManager.Action.QUEUE
            }

            // Prevent [PacketQueueManager] from flushing queued packets
            else -> BlinkManager.Action.PASS
        }

    }

}
