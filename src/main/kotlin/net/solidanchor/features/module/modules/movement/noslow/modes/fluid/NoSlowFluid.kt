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
package net.solidanchor.features.module.modules.movement.noslow.modes.fluid

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.FluidPushEvent
import net.solidanchor.event.events.PlayerFluidCollisionCheckEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.noslow.ModuleNoSlow

internal object NoSlowFluid : ToggleableValueGroup(ModuleNoSlow, "Fluid", true) {

    private var collision by boolean("Collision", true)

    @Suppress("unused")
    private val fluidPushHandler = handler<FluidPushEvent> {
        it.cancelEvent()
    }

    @Suppress("unused")
    private val fluidCollisionHandler = handler<PlayerFluidCollisionCheckEvent> {
        if (!collision) {
            it.cancelEvent()
        }
    }

}
