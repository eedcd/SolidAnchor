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

import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.markAsError
import net.solidanchor.utils.entity.withStrafe
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * NoClip module
 *
 * Allows you to fly through blocks.
 */
object ModuleNoClip : ClientModule("NoClip", ModuleCategories.MOVEMENT) {

    private val speed by float("Speed", 0.32f, 0.1f..0.4f)
    private val onlyInVehicle by boolean("OnlyInVehicle", false)
    private val disableOnSetback by boolean("DisableOnSetback", true)

    private var noClipSet = false

    @Suppress("unused")
    private val handleGameTick = handler<GameTickEvent> {
        if (paused()) {
            if (noClipSet) {
                onDisabled()
            }

            return@handler
        }

        noClipSet = true
        player.noPhysics = true
        player.fallDistance = 0.0
        player.setOnGround(false)

        val speed = speed.toDouble()
        player.controlledVehicle?.let {
            it.noPhysics = true

            if (!ModuleVehicleControl.running) {
                it.deltaMovement = it.deltaMovement.withStrafe(speed = speed)
                it.deltaMovement.y = when {
                    mc.options.keyJump.isDown -> speed
                    mc.options.keyShift.isDown -> -speed
                    else -> 0.0
                }
            }
        } ?: run {
            player.deltaMovement = player.deltaMovement.withStrafe(speed = speed)

            player.deltaMovement.y = when {
                mc.options.keyJump.isDown -> speed
                mc.options.keyShift.isDown -> -speed
                else -> 0.0
            }
        }
    }

    val packetHandler = handler<PacketEvent> { event ->
        // Setback detection
        if (event.packet is ClientboundPlayerPositionPacket && disableOnSetback && !paused()) {
            chat(markAsError(this.message("setbackDetected")))
            enabled = false
        }
    }

    override fun onDisabled() {
        noClipSet = false
        player.noPhysics = false
        player.controlledVehicle?.let { it.noPhysics = false }
    }

    fun paused() = onlyInVehicle && mc.player?.controlledVehicle == null

}
