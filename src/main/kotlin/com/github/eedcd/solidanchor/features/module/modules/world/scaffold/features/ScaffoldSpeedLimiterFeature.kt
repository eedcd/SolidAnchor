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
package com.github.eedcd.solidanchor.features.module.modules.world.scaffold.features

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.MovementInputEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.ModuleScaffold
import com.github.eedcd.solidanchor.utils.entity.horizontalSpeed
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention
import com.github.eedcd.solidanchor.utils.movement.DirectionalInput

object ScaffoldSpeedLimiterFeature : ToggleableValueGroup(ModuleScaffold, "SpeedLimiter", false) {

    private val speedLimit by float("SpeedLimit", 0.11f, 0.01f..0.4f)

    @Suppress("unused")
    private val moveEvent = handler<MovementInputEvent>(priority = EventPriorityConvention.SAFETY_FEATURE) {
        if (player.horizontalSpeed > speedLimit) {
            it.directionalInput = DirectionalInput.NONE
        }
    }

}
