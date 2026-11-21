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
package com.github.eedcd.solidanchor.features.module.modules.movement.speed.modes.verus

import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.event.events.PlayerAfterJumpEvent
import com.github.eedcd.solidanchor.event.events.PlayerMoveEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.tickHandler
import com.github.eedcd.solidanchor.event.waitTicks
import com.github.eedcd.solidanchor.features.module.modules.movement.speed.ModuleSpeed
import com.github.eedcd.solidanchor.features.module.modules.movement.speed.modes.SpeedBHopBase
import com.github.eedcd.solidanchor.utils.client.Timer
import com.github.eedcd.solidanchor.utils.entity.moving
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import com.github.eedcd.solidanchor.utils.kotlin.Priority
import com.github.eedcd.solidanchor.utils.math.multiply
import net.minecraft.world.entity.MoverType

/**
 * @anticheat Verus
 * @anticheatVersion b3882
 * @testedOn eu.anticheat-test.com
 */
class SpeedVerusB3882(parent: ModeValueGroup<*>) : SpeedBHopBase("VerusB3882", parent) {

    @Suppress("unused")
    private val afterJumpHandler = handler<PlayerAfterJumpEvent> {
        player.deltaMovement = player.deltaMovement.multiply(factorX = 1.1F, factorZ = 1.1F)
    }

    @Suppress("unused")
    private val moveHandler = handler<PlayerMoveEvent> { event ->
        // Might just strafe when player controls itself
        if (event.type == MoverType.SELF && player.moving) {
            event.movement = event.movement.withStrafe(strength = 1.0)
        }
    }

    @Suppress("unused")
    private val timerHandler = tickHandler {
        Timer.requestTimerSpeed(2.0F, Priority.IMPORTANT_FOR_USAGE_1, ModuleSpeed)
        waitTicks(101)
    }

}
