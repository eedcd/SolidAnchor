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
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.EventState
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.noslow.modes.blocking.NoSlowBlock.modes
import net.solidanchor.utils.client.InteractionTracker.blockingHand
import net.solidanchor.utils.client.InteractionTracker.untracked
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal object NoSlowBlockingReuse : Mode("Reuse") {

    override val parent: ModeValueGroup<Mode>
        get() = modes

    private val timingMode by enumChoice("Timing", TimingMode.PRE_POST)

    @Suppress("unused")
    private val networkTickHandler = handler<PlayerNetworkMovementTickEvent> { event ->
        blockingHand?.let { blockingHand ->
            when (timingMode) {
                TimingMode.PRE_TICK -> {
                    if (event.state == EventState.PRE) {
                        untracked {
                            network.send(
                                ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                            ))
                            interaction.startPrediction(world) { sequence ->
                                ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                            }
                        }
                    }
                }

                TimingMode.POST_TICK -> {
                    if (event.state == EventState.POST) {
                        untracked {
                            network.send(
                                ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                            ))
                            interaction.startPrediction(world) { sequence ->
                                ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                            }
                        }
                    }
                }

                TimingMode.PRE_POST -> {
                    when (event.state) {
                        EventState.PRE -> {
                            untracked {
                                network.send(
                                    ServerboundPlayerActionPacket(
                                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                                ))
                            }
                        }

                        EventState.POST -> {
                            untracked {
                                interaction.startPrediction(world) { sequence ->
                                    ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                                }
                            }
                        }
                    }
                }
            }
        }


        blockingHand?.let { blockingHand ->
            when (event.state) {
                EventState.PRE -> {

                }

                EventState.POST -> {
                    untracked {
                        interaction.startPrediction(world) { sequence ->
                            ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                        }
                    }
                }
            }
        }
    }

    private enum class TimingMode(override val tag: String) : Tagged {
        PRE_POST("PreAndPost"),
        PRE_TICK("Pre"),
        POST_TICK("Post")
    }


}
