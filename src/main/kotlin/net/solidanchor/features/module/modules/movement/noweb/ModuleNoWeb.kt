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
package net.solidanchor.features.module.modules.movement.noweb

import net.solidanchor.event.events.NotificationEvent
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.movement.ModuleAvoidHazards
import net.solidanchor.features.module.modules.movement.noweb.modes.NoWebAir
import net.solidanchor.features.module.modules.movement.noweb.modes.NoWebGrimBreak
import net.solidanchor.features.module.modules.movement.noweb.modes.NoWebIntave14
import net.solidanchor.features.module.modules.movement.noweb.modes.NoWebPlaceWater
import net.solidanchor.features.module.modules.movement.noweb.modes.NoWebStrafe
import net.solidanchor.utils.client.notification
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.WebBlock

/**
 * NoWeb module
 *
 * Disables web slowdown.
 */
object ModuleNoWeb : ClientModule("NoWeb", ModuleCategories.MOVEMENT) {

    val modes = choices(
        "Mode", NoWebAir, arrayOf(
            NoWebAir,
            NoWebGrimBreak,
            NoWebIntave14,
            NoWebPlaceWater,
            NoWebStrafe
        )
    ).apply { tagBy(this) }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (ModuleAvoidHazards.enabled && ModuleAvoidHazards.cobWebs) {
            ModuleAvoidHazards.enabled = false

            notification(
                "Compatibility error", "NoWeb is incompatible with AvoidHazards",
                NotificationEvent.Severity.ERROR
            )
            waitTicks(40)
        }
    }

    /**
     * Handle cobweb collision
     *
     * @see WebBlock.entityInside
     * @return if we should cancel the slowdown effect
     */
    fun handleEntityCollision(pos: BlockPos): Boolean {
        if (!running) {
            return false
        }

        return modes.activeMode.handleEntityCollision(pos)
    }
}
