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

package net.solidanchor.utils.aiming.point.exempts

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.EventListener
import net.minecraft.world.phys.Vec3

internal class ExemptBestHitVector(parent: EventListener) :
    ToggleableValueGroup(parent, "ExemptBestHitVector", false), ExemptPoint {

    private val vertical by float("Vertical", 0.2f, 0.0f..1f)
    private val horizontal by float("Horizontal", 0.1f, 0.0f..1f)

    override fun predicate(context: ExemptContext, point: Vec3) = enabled &&
        point.closerThan(context.bestHitVector, horizontal.toDouble(), vertical.toDouble())

}
