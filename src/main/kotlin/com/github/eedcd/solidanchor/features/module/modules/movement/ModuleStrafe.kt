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
package net.solidanchor.features.module.modules.movement

import net.solidanchor.event.events.PlayerMoveEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.math.copy
import net.minecraft.world.entity.MoverType

/**
 * Strafe module
 *
 * Strafe into different directions while you're midair.
 */
object ModuleStrafe : ClientModule("Strafe", ModuleCategories.MOVEMENT) {

    private var strengthInAir by float("StrengthInAir", 1f, 0.0f..1f)
    private var strengthOnGround by float("StrengthOnGround", 1f, 0.0f..1f)

    private var strictMovement by boolean("StrictMovement", false)

    val moveHandler = handler<PlayerMoveEvent> { event ->
        // Might just strafe when player controls itself
        if (event.type == MoverType.SELF) {
            val strength = if (player.onGround()) strengthOnGround else strengthInAir

            // Don't strafe if strength is 0
            if (strength == 0f) {
                return@handler
            }

            if (player.moving) {
                event.movement = event.movement.withStrafe(strength = strength.toDouble())
            } else if (strictMovement) {
                event.movement = event.movement.copy(x = 0.0, z = 0.0)
            }
        }
    }

}
