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
package net.solidanchor.features.module.modules.movement.speed.modes.vulcan

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.PlayerAfterJumpEvent
import net.solidanchor.event.sequenceHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import net.solidanchor.utils.entity.movementSideways
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.copy
import net.minecraft.world.effect.MobEffects

/**
 * BHop Speed for Vulcan 286
 * Taken from InspectorBoat Vulcan Bypasses (He agreed to it)
 *
 * Tested on both anticheat-test.com and loyisa.cn
 */
class SpeedVulcan286(parent: ModeValueGroup<*>) : SpeedBHopBase("Vulcan286", parent) {

    private inline val goingSideways: Boolean
        get() = player.input.movementSideways != 0f

    @Suppress("unused")
    private val afterJumpHandler = sequenceHandler<PlayerAfterJumpEvent> {
        // We might lose the effect during runtime of the sequence,
        // but we don't care, since it is Vulcan.
        val speedLevel = (player.getEffect(MobEffects.SPEED)?.amplifier ?: 0)

        waitTicks(1)
        player.deltaMovement = player.deltaMovement.withStrafe(
            speed = if (goingSideways) 0.3345 else 0.3355 * (1 + speedLevel * 0.3819)
        )

        waitTicks(1)
        if (player.isSprinting) {
            player.deltaMovement = player.deltaMovement.withStrafe(
                speed = if (goingSideways) 0.3235 else 0.3284 * (1 + speedLevel * 0.355)
            )
        }

        waitTicks(2)
        player.deltaMovement = player.deltaMovement.copy(y = -0.376)

        waitTicks(2)
        if (player.flyDist > 0.298) {
            player.deltaMovement = player.deltaMovement.withStrafe(speed = 0.298)
        }
    }

}
