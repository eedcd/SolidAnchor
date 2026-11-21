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
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

/**
 * Bypassing Grim 2.3.71
 * @from https://github.com/GrimAnticheat/Grim/issues/2216
 */
internal class NoSlowSharedGrim2371(override val parent: ModeValueGroup<*>) : Mode("Grim2371") {

    companion {
        var shouldPreventNoSlow = false
            private set
    }

    @Suppress("unused")
    private val tickHandler = tickHandler(onCancellation = { shouldPreventNoSlow = false }) {
        repeat(2) {
            waitTicks(1)
            shouldPreventNoSlow = false
            interaction.startPrediction(world) { sequence ->
                ServerboundUseItemPacket(
                    player.usedItemHand, sequence,
                    player.yRot, player.xRot
                )
            }
        }

        shouldPreventNoSlow = true
    }

    override fun disable() {
        shouldPreventNoSlow = false
        super.disable()
    }

}
