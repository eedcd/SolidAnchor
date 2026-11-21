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

package com.github.eedcd.solidanchor.features.module.modules.world.scaffold.tower

import com.github.eedcd.solidanchor.event.events.PlayerJumpEvent
import com.github.eedcd.solidanchor.event.sequenceHandler
import com.github.eedcd.solidanchor.event.tickUntil
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.ModuleScaffold
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.ModuleScaffold.isBlockBelow
import com.github.eedcd.solidanchor.utils.client.Timer
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention.READ_FINAL_STATE
import com.github.eedcd.solidanchor.utils.kotlin.Priority

object ScaffoldTowerKarhu : ScaffoldTower("Karhu") {

    private val timerSpeed by float("Timer", 5f, 0.1f..10f)
    private val triggerMotion by float("Trigger", 0.06f, 0.0f..0.2f, "Y/v")
    private val pulldown by boolean("Pulldown", true)

    @Suppress("unused")
    private val jumpHandler = sequenceHandler<PlayerJumpEvent>(priority = READ_FINAL_STATE) { event ->
        if (event.motion == 0f || event.isCancelled) {
            return@sequenceHandler
        }

        tickUntil { !player.onGround() }
        Timer.requestTimerSpeed(timerSpeed, Priority.IMPORTANT_FOR_USAGE_1, ModuleScaffold)

        if (pulldown) {
            tickUntil { !player.onGround() && player.deltaMovement.y < triggerMotion }

            if (!isBlockBelow) {
                return@sequenceHandler
            }

            player.deltaMovement.y -= 1f
        }
    }

}
