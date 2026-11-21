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
package net.solidanchor.features.module.modules.movement.noslow.modes.consume

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.EventState
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.handler
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket

/**
 * tested on mineblaze.net
 */

internal class NoSlowConsumeIntave14(override val parent: ModeValueGroup<*>) : Mode("Intave14") {
    private val mode by enumChoice("Mode", Mode.RELEASE)

    private fun releasePacket() {
        network.send(
            ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                player.blockPosition(),
                Direction.UP
            )
        )
    }

    @Suppress("unused")
    private val onNetworkTick = handler<PlayerNetworkMovementTickEvent> { event ->
        if (event.state == EventState.PRE) {
            when (mode) {
                Mode.RELEASE -> {
                    if (player.isUsingItem) {
                        releasePacket()
                    }

                    if (player.ticksUsingItem == 3) {
                        player.releaseUsingItem()
                        releasePacket()
                    }
                }

                Mode.NEW -> {
                    if (player.ticksUsingItem <= 2 || player.useItemRemainingTicks == 0) {
                        releasePacket()
                    }
                }
            }
        }
    }

    private enum class Mode(override val tag: String) : Tagged {
        RELEASE("Release"),
        NEW("New")
    }
}
