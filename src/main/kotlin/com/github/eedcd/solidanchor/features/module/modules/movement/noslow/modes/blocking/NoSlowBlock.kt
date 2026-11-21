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

import net.solidanchor.config.types.group.NoneMode
import net.solidanchor.features.module.modules.movement.noslow.NoSlowUseActionHandler
import net.solidanchor.features.module.modules.movement.noslow.modes.shared.NoSlowSharedGrim2360
import net.solidanchor.features.module.modules.movement.noslow.modes.shared.NoSlowSharedGrim2364MC18
import net.solidanchor.features.module.modules.movement.noslow.modes.shared.NoSlowSharedGrim2371
import net.solidanchor.features.module.modules.movement.noslow.modes.shared.NoSlowSharedInvalidHand
import net.solidanchor.utils.client.InteractionTracker.isBlocking
import net.solidanchor.utils.client.inGame
import net.solidanchor.utils.entity.isBlockingServerside
import net.minecraft.world.phys.Vec2

internal object NoSlowBlock : NoSlowUseActionHandler("Blocking") {

    private val onlySlowOnServerSide by boolean("OnlySlowOnServerSide", false)

    val modes = modes(this, "Choice") {
        arrayOf(
            NoneMode(it),
            NoSlowBlockingReuse,
            NoSlowBlockingSwitch,
            NoSlowBlockingBlink,
            NoSlowBlockingInteract,
            NoSlowSharedGrim2360(it),
            NoSlowSharedGrim2364MC18(it),
            NoSlowSharedGrim2371(it),
            NoSlowSharedInvalidHand(it),
            NoSlowBlockIntave14(it)
        )
    }

    override fun getMultiplier(forward: Float, sideways: Float): Vec2 {
        if (onlySlowOnServerSide && isBlocking) {
            return Vec2(forward, sideways)
        }

        return super.getMultiplier(forward, sideways)
    }

    override val running: Boolean
        get() {
            if (!super.running || !inGame) {
                return false
            }

            // Check if we are using a block item
            return player.isBlockingServerside || isBlocking
        }

}

