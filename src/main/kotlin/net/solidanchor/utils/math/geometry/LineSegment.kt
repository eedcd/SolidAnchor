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
package net.solidanchor.utils.math.geometry

import net.solidanchor.utils.math.isLikelyZero
import net.minecraft.world.phys.Vec3

data class LineSegment(
    val start: Vec3,
    val end: Vec3,
) : LinearGeometry3 {

    override val direction: Vec3 = end.subtract(start)

    init {
        require(!direction.isLikelyZero) {
            "Line segment must not have zero length, actual: $start -> $end"
        }
    }

    override val anchor: Vec3
        get() = start

    val length: Double
        get() = direction.length()

}
