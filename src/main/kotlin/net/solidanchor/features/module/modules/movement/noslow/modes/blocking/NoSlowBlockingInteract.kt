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

package net.solidanchor.features.module.modules.movement.noslow.modes.blocking

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.EventState
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.noslow.modes.blocking.NoSlowBlock.modes
import net.solidanchor.utils.client.InteractionTracker.blockingHand
import net.solidanchor.utils.client.InteractionTracker.untracked
import net.solidanchor.utils.network.sendHeldItemChange
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal object NoSlowBlockingInteract : Mode("Interact") {

    override val parent: ModeValueGroup<Mode>
        get() = modes

    @Suppress("unused")
    val onNetworkTick = handler<PlayerNetworkMovementTickEvent> { event ->
        blockingHand?.let { blockingHand ->
            if (event.state == EventState.POST) {
                untracked {
                    network.sendHeldItemChange(player.inventory.selectedSlot)
                    interaction.startPrediction(world) { sequence ->
                        ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                    }
                }
            }
        }
    }

}
