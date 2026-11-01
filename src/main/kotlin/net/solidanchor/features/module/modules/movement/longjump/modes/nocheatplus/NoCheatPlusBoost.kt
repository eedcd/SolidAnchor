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

package net.solidanchor.features.module.modules.movement.longjump.modes.nocheatplus

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PlayerMoveEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.longjump.ModuleLongJump
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.math.multiply
import net.solidanchor.utils.movement.stopXZVelocity

/**
 * @anticheat NoCheatPlus
 * @anticheatVersion 3.16.1-SNAPSHOT-sMD5NET-b115s
 * @testedOn eu.loyisa.cn
 */
internal object NoCheatPlusBoost : Mode("NoCheatPlusBoost") {
    override val parent: ModeValueGroup<*>
        get() = ModuleLongJump.mode

    val ncpBoost by float("NCPBoost", 4.25f, 1f..10f)

    val repeatable = handler<GameTickEvent> {
        if (ModuleLongJump.canBoost) {
            player.deltaMovement =
                player.deltaMovement.multiply(factorX = ncpBoost, factorZ = ncpBoost)
            ModuleLongJump.boosted = true
        }
        ModuleLongJump.canBoost = false
    }

    val moveHandler = handler<PlayerMoveEvent> {
        if (!player.moving && ModuleLongJump.jumped) {
            player.stopXZVelocity()
        }
    }
}
