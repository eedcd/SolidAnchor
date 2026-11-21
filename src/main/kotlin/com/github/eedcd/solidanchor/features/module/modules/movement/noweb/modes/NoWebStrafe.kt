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

package com.github.eedcd.solidanchor.features.module.modules.movement.noweb.modes

import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.features.module.modules.movement.noweb.ModuleNoWeb.modes
import com.github.eedcd.solidanchor.features.module.modules.movement.noweb.NoWebMode
import com.github.eedcd.solidanchor.utils.entity.moving
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import net.minecraft.core.BlockPos

/**
 * Bypassing Vulcan anticheat (6/27/2025)
 * Bypassing Grim anticheat (7/28/2025)
 *
 * @author XeContrast
 */
object NoWebStrafe : NoWebMode("Strafe") {
    override val parent: ModeValueGroup<NoWebMode>
        get() = modes

    private val strength by float("Strength", 0.23f, 0.01f..0.8f)
    private val motionY = tree(Motion())
    private val onlyGround by boolean("OnlyOnGround", false)

    private class Motion : ToggleableValueGroup(this@NoWebStrafe, "MotionY", false) {
        val motionStrength by float("MotionYStrength", 0.6f, -2.00f..2.00f)
    }

    // TODO: make motionY more flexible based on player input
    override fun handleEntityCollision(pos: BlockPos): Boolean {
        if (player.moving) {
            if (player.onGround() || !onlyGround) {
                player.deltaMovement = player.deltaMovement.withStrafe(strength.toDouble())
            }

            if (motionY.enabled) {
                player.deltaMovement.y = motionY.motionStrength.toDouble()
            }
        }
        return false
    }
}
