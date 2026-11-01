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
package net.solidanchor.features.module.modules.player.nofall.modes

import net.solidanchor.event.events.BlinkPacketEvent
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.events.NotificationEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.TransferOrigin
import net.solidanchor.event.handler
import net.solidanchor.features.blink.BlinkManager
import net.solidanchor.features.module.modules.player.nofall.ModuleNoFall
import net.solidanchor.features.module.modules.render.ModuleDebug
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.utils.client.notification
import net.solidanchor.utils.entity.SimulatedPlayer
import net.solidanchor.utils.movement.DirectionalInput
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * SpoofGround mode for the NoFall module.
 * This mode spoofs the 'onGround' flag in PlayerMoveC2SPacket to prevent fall damage.
 */
internal object NoFallBlink : NoFallMode("Blink") {

    private const val PEEK_TICKS = 2

    /**
     * Trigger fall distance should not go beyond the possible fall distance of maximum ticks
     */
    private const val MAXIMUM_TICKS = 10

    private var triggerFallDistance by float("TriggerFallDistance", 2.5f, 0.5f..3f)
    private var maximumFallDistance by float("MaximumFallDistance", 20f, 2f..50f)

    private var blinkFall = false
    var waitUntilGround = true

    private val inputHandler = handler<MovementInputEvent> { event ->
        // If we are invincible, we don't need to care about fall damage
        if (player.isCreative || player.abilities.mayfly || player.abilities.flying) {
            blinkFall = false
            return@handler
        }

        // If we are not on-ground, we do some checks in-case something goes wrong
        if (!player.onGround()) {
            if (waitUntilGround || player.fallDistance > maximumFallDistance) {
                if (blinkFall) {
                    BlinkManager.rewrite<ServerboundMovePlayerPacket> { packet ->
                        packet.onGround = false
                    }

                    blinkFall = false
                }
            }

            return@handler

        } else {
            // If we are on ground, we might not want to blink anymore
            blinkFall = false
            waitUntilGround = false
        }

        // Check if we fall off in the next 2 ticks
        val simulatedPlayer = SimulatedPlayer.fromClientPlayer(
            SimulatedPlayer.SimulatedPlayerInput(
                event.directionalInput,
                event.jump,
                player.isSprinting,
                true
            ))

        repeat(PEEK_TICKS) {
            simulatedPlayer.tick()
        }

        if (simulatedPlayer.onGround) {
            return@handler
        }

        simulatedPlayer.tick()

        // Continue with clean input
        simulatedPlayer.input = SimulatedPlayer.SimulatedPlayerInput(
            DirectionalInput.NONE,
            jumping = false,
            sprinting = false,
            sneaking = false
        )

        // Check if we collect fall distance above 2f in the next 10 ticks
        for (ignored in 0 until MAXIMUM_TICKS) {
            simulatedPlayer.tick()

            if (simulatedPlayer.fallDistance > triggerFallDistance) {
                notification("NoFall", "Detected possible fall damage, blinking...",
                    NotificationEvent.Severity.INFO)
                blinkFall = true

                ModuleDebug.debugGeometry(ModuleNoFall, "Ground", ModuleDebug.DebuggedPoint(player.position(),
                    Color4b(0, 0, 255, 255), size = 0.2))
                break
            }
        }
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet is ServerboundMovePlayerPacket && blinkFall) {
            packet.onGround = true
        }
    }

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        if (event.origin == TransferOrigin.OUTGOING && blinkFall) {
            event.action = BlinkManager.Action.QUEUE
        }
    }

    override fun disable() {
        blinkFall = false
        super.disable()
    }

}
