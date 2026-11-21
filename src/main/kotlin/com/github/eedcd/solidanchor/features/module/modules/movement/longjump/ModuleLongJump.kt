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
package com.github.eedcd.solidanchor.features.module.modules.movement.longjump

import com.github.eedcd.solidanchor.event.events.MovementInputEvent
import com.github.eedcd.solidanchor.event.events.PlayerJumpEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.movement.longjump.modes.Matrix7145FlagLongJump
import com.github.eedcd.solidanchor.features.module.modules.movement.longjump.modes.VulcanLongJump
import com.github.eedcd.solidanchor.features.module.modules.movement.longjump.modes.nocheatplus.NoCheatPlusBoost
import com.github.eedcd.solidanchor.features.module.modules.movement.longjump.modes.nocheatplus.NoCheatPlusBow
import com.github.eedcd.solidanchor.utils.entity.moving

object ModuleLongJump : ClientModule("LongJump", ModuleCategories.MOVEMENT) {

    val mode = choices(
        "Mode", NoCheatPlusBoost, arrayOf(
            // NoCheatPlus
            NoCheatPlusBoost,
            NoCheatPlusBow,
            VulcanLongJump,
            Matrix7145FlagLongJump
        )
    ).apply { tagBy(this) }
    private val autoJump by boolean("AutoJump", false)
    val autoDisable by boolean("DisableAfterFinished", false)

    var jumped = false
    var canBoost = false
    var boosted = false

    val tickHandler = handler<MovementInputEvent> {
        if (jumped) {
            if (player.onGround() || player.abilities.flying) {
                if (autoDisable && boosted) {
                    enabled = false
                }

                jumped = false
            }
        }

        // AutoJump
        if (autoJump && player.onGround() && player.moving
            && mode.activeMode != NoCheatPlusBow) {
            player.jumpFromGround()
            jumped = true
        }
    }

    @Suppress("unused")
    val manualJumpHandler = handler<PlayerJumpEvent> {
        jumped = true
        canBoost = true
    }
}
