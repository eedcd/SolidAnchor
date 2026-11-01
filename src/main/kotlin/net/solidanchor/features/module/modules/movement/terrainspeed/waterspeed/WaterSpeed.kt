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

package net.solidanchor.features.module.modules.movement.terrainspeed.waterspeed

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.events.PlayerTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.terrainspeed.ModuleTerrainSpeed
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.copy

internal object WaterSpeed : ToggleableValueGroup(ModuleTerrainSpeed, "WaterSpeed", true) {

    val autoSwim by boolean("AutoSwim", true)

    object BaseSpeed : ValueGroup("BaseSpeed") {
        val horizontalSpeed by float("Horizontal", 0.44f, 0.1f..10f)
        val verticalSpeed by float("Vertical", 0.44f, 0.1f..10f)
    }

    object SprintSpeed : ToggleableValueGroup(this, "SprintSpeed", true) {
        val horizontalSpeed by float("Horizontal", 1f, 0.1f..10f)
        val verticalSpeed by float("Vertical", 1f, 0.1f..10f)
    }

    init {
        tree(BaseSpeed)
        tree(SprintSpeed)
    }

    @Suppress("unused")
    private val inputHandler = handler<MovementInputEvent> { event ->
        if (autoSwim && player.isInWater && !mc.options.keyShift.isDown) {
            event.jump = true
        }
    }

    @Suppress("unused")
    private val tickHandler = handler<PlayerTickEvent> {
        if (!player.isInWater) return@handler

        val useSprintSpeed = mc.options.keySprint.isDown && SprintSpeed.enabled
        val horizontalSpeed = if (useSprintSpeed) SprintSpeed.horizontalSpeed else BaseSpeed.horizontalSpeed
        val verticalSpeed = if (useSprintSpeed) SprintSpeed.verticalSpeed else BaseSpeed.verticalSpeed

        if (player.moving) {
            player.deltaMovement = player.deltaMovement.withStrafe(
                speed = horizontalSpeed.toDouble()
            )
        }

        player.deltaMovement = if (mc.options.keyJump.isDown) {
            player.deltaMovement.copy(y = verticalSpeed.toDouble())
        } else if (mc.options.keyShift.isDown) {
            player.deltaMovement.copy(y = -verticalSpeed.toDouble())
        } else {
            return@handler
        }
    }
}
