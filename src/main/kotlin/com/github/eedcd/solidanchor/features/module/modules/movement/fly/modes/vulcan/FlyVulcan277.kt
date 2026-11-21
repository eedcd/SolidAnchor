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

package net.solidanchor.features.module.modules.movement.fly.modes.vulcan

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.fly.ModuleFly.modes

/**
 * @anticheat Vulcan
 * @anticheat Version 2.7.7
 * @testedOn anticheat-test.com
 * @note NA
 */
internal object FlyVulcan277 : Mode("Vulcan277") {

    override val parent: ModeValueGroup<*>
        get() = modes

    val repeatable = handler<GameTickEvent> {
        if (player.fallDistance > 0.1) {
            if (player.tickCount % 2 == 0) {
                player.deltaMovement.y = -0.155
            } else {
                player.deltaMovement.y = -0.1
            }
        }
    }

}

