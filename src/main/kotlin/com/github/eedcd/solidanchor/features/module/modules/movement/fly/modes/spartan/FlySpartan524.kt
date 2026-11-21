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

package net.solidanchor.features.module.modules.movement.fly.modes.spartan

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.PlayerMoveEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.fly.ModuleFly.modes
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * @anticheat Spartan
 * @anticheatVersion phase 524
 * @testedOn minecraft.vagdedes.com
 * @note spartan flags less if your motion is stable, that's why we use PlayerMoveEvent
 */
internal object FlySpartan524 : Mode("Spartan524") {

    override val parent: ModeValueGroup<*>
        get() = modes

    val moveHandler = handler<PlayerMoveEvent> { event ->
        val yaw = Math.toRadians(player.yRot.toDouble())
        event.movement = Vec3(
            -sin(yaw) * 0.28,
            0.0,
            cos(yaw) * 0.28
        )
    }

}
