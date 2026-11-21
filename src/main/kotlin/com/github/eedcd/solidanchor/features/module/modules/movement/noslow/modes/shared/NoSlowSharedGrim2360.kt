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
package com.github.eedcd.solidanchor.features.module.modules.movement.noslow.modes.shared

import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.event.EventState
import com.github.eedcd.solidanchor.event.events.PlayerNetworkMovementTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.utils.client.InteractionTracker.untracked
import com.github.eedcd.solidanchor.utils.network.sendHeldItemChange
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.world.InteractionHand

/**
 * @anticheat Grim
 * @anticheatVersion 2.3.60
 */
internal class NoSlowSharedGrim2360(override val parent: ModeValueGroup<*>) : Mode("Grim2360") {

    @Suppress("unused")
    private val onNetworkTick = handler<PlayerNetworkMovementTickEvent> { event ->
        if (player.isUsingItem && event.state == EventState.PRE) {
            val hand = player.usingItemHand

            if (hand == InteractionHand.MAIN_HAND) {
                untracked {
                    // Send offhand interact packet
                    // so that grim focuses on offhand noslow checks that don't exist.
                    network.send(ServerboundUseItemPacket(InteractionHand.OFF_HAND, 0, player.yRot, player.xRot))
                }
            } else if (hand == InteractionHand.OFF_HAND) {
                // Switch slots (based on 1.8 grim switch noslow)
                untracked {
                    val slot = player.inventory.selectedSlot
                    network.sendHeldItemChange(slot % 8 + 1)
                    network.sendHeldItemChange(slot % 7 + 2)
                    network.sendHeldItemChange(slot)
                }
            }
        }
    }

}
