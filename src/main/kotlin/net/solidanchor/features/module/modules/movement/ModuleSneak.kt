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
package net.solidanchor.features.module.modules.movement

import net.solidanchor.additions.forceSneak
import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.EventState
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.events.NotificationEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.client.isNewerThanOrEquals1_21_6
import net.solidanchor.utils.client.notification
import net.solidanchor.utils.network.send1_21_5StartSneaking
import net.solidanchor.utils.network.send1_21_5StopSneaking
import net.solidanchor.utils.client.usesViaFabricPlus
import net.solidanchor.utils.entity.SimulatedPlayer
import net.solidanchor.utils.entity.immuneToMagmaBlocks
import net.solidanchor.utils.entity.isOnMagmaBlock
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.set
import net.solidanchor.utils.movement.DirectionalInput
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket

/**
 * Sneak module
 *
 * Automatically sneaks all the time.
 */
object ModuleSneak : ClientModule("Sneak", ModuleCategories.MOVEMENT) {

    private val modes = choices("Mode", Vanilla, arrayOf(Legit, Vanilla, Switch)).apply { tagBy(this) }
    private val notDuringMove by boolean("NotDuringMove", false)

    private object Legit : Mode("Legit") {

        private val onMagmaBlocksOnly by boolean("OnMagmaBlocksOnly", false)

        override val parent: ModeValueGroup<Mode>
            get() = modes

        @Suppress("unused")
        private val inputHandler = handler<MovementInputEvent> { event ->
            if (player.moving && notDuringMove) {
                return@handler
            }

            if (onMagmaBlocksOnly && (player.immuneToMagmaBlocks || !isOnMagmaBlock(event.directionalInput))) {
                return@handler
            }

            // Temporarily override sneaking
            event.sneak = true
        }

    }

    private object Vanilla : Mode("Vanilla") {

        private var networkSneaking = false

        override val parent: ModeValueGroup<Mode>
            get() = modes

        @Suppress("unused")
        private val networkTick = handler<PlayerNetworkMovementTickEvent> {
            if (!usesViaFabricPlus || isNewerThanOrEquals1_21_6) {
                return@handler
            }

            val shouldSneak = !player.moving || !notDuringMove
            if (shouldSneak && !networkSneaking) {
                network.send1_21_5StartSneaking()
                networkSneaking = true
            } else if (!shouldSneak && networkSneaking) {
                network.send1_21_5StopSneaking()
                networkSneaking = false
            }
        }

        @Suppress("unused")
        private val sneakNetworkHandler = handler<PacketEvent> { event ->
            if ((player.moving && notDuringMove) || event.packet !is ServerboundPlayerInputPacket) {
                return@handler
            }

            event.packet.forceSneak = true
        }

        override fun disable() {
            if (networkSneaking) {
                network.send1_21_5StopSneaking()
                networkSneaking = false
            }
        }

    }

    private object Switch : Mode("Switch") {

        private var networkSneaking = false

        override val parent: ModeValueGroup<Mode>
            get() = modes

        override fun enable() {
            if (!usesViaFabricPlus || isNewerThanOrEquals1_21_6) {
                notification(
                    "Protocol Error",
                    "This mode can only be used on server with version earlier than 1.21.6.",
                    NotificationEvent.Severity.ERROR,
                )
            }
            super.enable()
        }

        @Suppress("unused")
        private val networkTick = handler<PlayerNetworkMovementTickEvent> { event ->
            if (player.moving && notDuringMove) {
                disable()
                return@handler
            }

            when (event.state) {
                EventState.PRE -> {
                    if (networkSneaking) {
                        network.send1_21_5StopSneaking()
                        networkSneaking = false
                    }
                }

                EventState.POST -> {
                    if (!networkSneaking) {
                        network.send1_21_5StartSneaking()
                        networkSneaking = true
                    }
                }
            }
        }

        override fun disable() {
            if (networkSneaking) {
                network.send1_21_5StopSneaking()
                networkSneaking = false
            }
        }
    }

    private fun isOnMagmaBlock(directionalInput: DirectionalInput): Boolean {
        val simulatedInput = SimulatedPlayer.SimulatedPlayerInput.fromClientPlayer(directionalInput)
        simulatedInput.set(jump = false)

        // Doesn't keep the player stuck at the edge of a magma block while sneaking
        simulatedInput.ignoreClippingAtLedge = true

        val simulatedPlayer = SimulatedPlayer.fromClientPlayer(simulatedInput)
        simulatedPlayer.pos = player.position()

        simulatedPlayer.tick()
        val isOnMagmaBlockAfterOneTick = simulatedPlayer.boundingBox.isOnMagmaBlock()

        simulatedPlayer.tick()
        val isOnMagmaBlockAfterTwoTicks = simulatedPlayer.boundingBox.isOnMagmaBlock()

        return isOnMagmaBlockAfterOneTick || isOnMagmaBlockAfterTwoTicks
    }
}
