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
package com.github.eedcd.solidanchor.features.module.modules.movement.inventorymove.features

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.BlinkPacketEvent
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.NotificationEvent
import com.github.eedcd.solidanchor.event.events.ScreenEvent
import com.github.eedcd.solidanchor.event.events.TransferOrigin
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.blink.BlinkManager
import com.github.eedcd.solidanchor.features.module.modules.movement.inventorymove.ModuleInventoryMove
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleBlink
import com.github.eedcd.solidanchor.utils.client.Chronometer
import com.github.eedcd.solidanchor.utils.text.formatAsTime
import com.github.eedcd.solidanchor.utils.client.notification
import com.github.eedcd.solidanchor.utils.network.isC2SContainerPacket
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object InventoryMoveBlinkFeature : ToggleableValueGroup(ModuleInventoryMove, "Blink", false) {

    /**
     * After reaching this time, we will close the inventory and blink.
     */
    private val maximumTime by int("MaximumTime", 10000, 0..30000, "ms")

    private val chronometer = Chronometer()

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        val packet = event.packet

        if (mc.gui.screen() is AbstractContainerScreen<*> && event.origin == TransferOrigin.OUTGOING) {
            event.action = when {
                packet.isC2SContainerPacket() -> BlinkManager.Action.PASS
                else -> BlinkManager.Action.QUEUE
            }
        }
    }

    @Suppress("unused")
    val screenHandler = handler<ScreenEvent> { event ->
        if (event.screen is AbstractContainerScreen<*>) {
            chronometer.reset()

            notification(
                "InventoryMove",
                ModuleBlink.message("blinkStart", maximumTime.formatAsTime()),
                NotificationEvent.Severity.INFO
            )
        }
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (mc.gui.screen() is AbstractContainerScreen<*> && chronometer.hasElapsed(maximumTime.toLong())) {
            player.closeContainer()
            notification(
                "InventoryMove",
                ModuleBlink.message("blinkEnd"),
                NotificationEvent.Severity.INFO
            )
        }
    }

}
