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
package net.solidanchor.features.module.modules.movement.noslow.modes.sneaking

import net.solidanchor.config.types.group.NoneMode
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.PlayerSneakMultiplier
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.noslow.ModuleNoSlow
import net.solidanchor.utils.client.inGame
import kotlin.math.max

internal object NoSlowSneaking : ToggleableValueGroup(ModuleNoSlow, "Sneaking", true) {

    private val minMultiplier by float("MinMultiplier", 1f, 0.3f..1f)

    @Suppress("unused")
    private val modes = modes(this, "Mode") {
        arrayOf(
            NoneMode(it),
            NoSlowSneakingSwitch(it),
            NoSlowSneakingAAC5(it),
        )
    }

    @Suppress("unused")
    val multiplierHandler = handler<PlayerSneakMultiplier> { event ->
        event.multiplier = max(event.multiplier, minMultiplier.toDouble())
    }

    override val running: Boolean
        get() = super.running && inGame && player.isShiftKeyDown
}
