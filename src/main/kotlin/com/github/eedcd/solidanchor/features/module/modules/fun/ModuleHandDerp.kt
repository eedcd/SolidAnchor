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
package net.solidanchor.features.module.modules.`fun`

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.sequenceHandler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ServerboundPunchPacket
import net.minecraft.server.level.ClientInformation


/**
 * Hand derp module
 *
 * Switches your main hand.
 */
object ModuleHandDerp : ClientModule("HandDerp", ModuleCategories.FUN) {


    private val silent by boolean("Silent", false)
    private val mode = choices("Mode", Delay, arrayOf(Delay, Swing))


    private val originalHand = mc.options.mainHand().get()
    private var currentHand = mc.options.mainHand().get()

    private fun calculatePlayerPartValue(): Int {
        var value = 0
        for (part in mc.options.modelParts) {
            value = value or (1 shl part.ordinal)
        }
        return value
    }

    private fun switchHand() {

        currentHand = currentHand.opposite
        network.send(
            ServerboundClientInformationPacket(
                ClientInformation(
                    mc.options.languageCode,
                    mc.options.renderDistance().get(),
                    mc.options.chatVisibility().get(),
                    mc.options.chatColors().get(),
                    calculatePlayerPartValue(),
                    currentHand,
                    mc.isTextFilteringEnabled,
                    mc.options.allowServerListing().get(),
                    mc.options.particles().get()
                )
            )
        )

    }

    val packetHandler = sequenceHandler<PacketEvent>(priority = 1) {
        val packet = it.packet
        if (silent && packet is ClientboundSetEntityDataPacket &&
            packet.packedItems.any { data ->
                data.id == mc.options.mainHand().get().id }) {
            it.cancelEvent()
        }
    }

    override fun onDisabled() {
        if (mc.options.mainHand().get() != originalHand) {
            switchHand()
        }
    }

    private object Delay : Mode("Delay") {
        override val parent: ModeValueGroup<Mode>
            get() = mode

        val delayValue by int("Delay", 1, 0..20, "ticks")

        @Suppress("unused")
        val repeatable = tickHandler {
            waitTicks(delayValue)
            switchHand()
        }
    }

    private object Swing : Mode("Swing") {
        override val parent: ModeValueGroup<Mode>
            get() = mode

        @Suppress("unused")
        val packetHandler = sequenceHandler<PacketEvent>(priority = 1) {
            val packet = it.packet
            if (packet is ServerboundPunchPacket) {
                switchHand()
            }
        }
    }

}
