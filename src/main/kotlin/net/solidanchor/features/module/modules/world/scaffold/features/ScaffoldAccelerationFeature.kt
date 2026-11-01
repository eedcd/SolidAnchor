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
package net.solidanchor.features.module.modules.world.scaffold.features

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.world.scaffold.ModuleScaffold
import net.solidanchor.utils.math.multiply

object ScaffoldAccelerationFeature : ToggleableValueGroup(ModuleScaffold, "Acceleration", false) {
    private val speedMultiplier by float("SpeedMultiplier", 0.6f, 0.1f..3f)
    private val onlyOnGround by boolean("OnlyOnGround", false)

    @Suppress("unused")
    val stateUpdateHandler = handler<GameTickEvent> {
        if (onlyOnGround && !player.onGround()) {
            return@handler
        }

        player.deltaMovement = player.deltaMovement.multiply(factorX = speedMultiplier, factorZ = speedMultiplier)
    }
}
