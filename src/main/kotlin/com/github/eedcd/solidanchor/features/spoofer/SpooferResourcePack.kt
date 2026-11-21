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
package net.solidanchor.features.spoofer

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.handler
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.ACCEPTED
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.DECLINED
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED

/**
 * ResourcePack Spoof
 *
 * Prevents servers from forcing you to download their resource pack.
 */
object SpooferResourcePack : ToggleableValueGroup(name = "ResourceSpoofer", enabled = false) {

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet
        val network = mc.connection ?: return@handler

        if (packet is ClientboundResourcePackPushPacket) {
            val id = packet.id
            network.send(ServerboundResourcePackPacket(id, ACCEPTED))
            network.send(ServerboundResourcePackPacket(id, SUCCESSFULLY_LOADED))
            event.cancelEvent()
        } else if (packet is ServerboundResourcePackPacket && (packet.action == DECLINED ||
                packet.action == FAILED_DOWNLOAD)) {
            event.cancelEvent()
        }
    }

}
