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
package net.solidanchor.features.module.modules.movement.noslow.modes.shared

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.TransferOrigin
import net.solidanchor.event.handler
import net.solidanchor.utils.client.NullableBypass
import net.solidanchor.utils.network.sendPacketSilently
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal class NoSlowSharedInvalidHand(override val parent: ModeValueGroup<*>) : Mode("InvalidHand") {

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent>(priority = EventPriorityConvention.READ_FINAL_STATE) { event ->
        val packet = event.packet

        if (!event.isCancelled && event.origin == TransferOrigin.OUTGOING && packet is ServerboundUseItemPacket) {
            event.cancelEvent()
            sendPacketSilently(NullableBypass.createWithNullHand(packet))
        }
    }

}
