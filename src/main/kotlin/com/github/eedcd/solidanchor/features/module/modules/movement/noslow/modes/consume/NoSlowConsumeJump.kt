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
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.entity.moving

/**
 * jumps while consuming, useful for incorrect anticheat configurations
 */

internal class NoSlowConsumeJump(override val parent: ModeValueGroup<*>) : Mode("Jump") {

    @Suppress("unused")
    private val movementInputHandler = handler<MovementInputEvent> { event ->
        if (player.isUsingItem && player.moving) {
            event.jump = true
        }
    }

}
