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
package com.github.eedcd.solidanchor.utils.aiming.features.processors.anglesmooth

import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.utils.aiming.data.Rotation
import com.github.eedcd.solidanchor.utils.aiming.features.processors.RotationProcessor

/**
 * An [AngleSmooth]'er, but as choice
 */
abstract class AngleSmooth(
    name: String,
    override val parent: ModeValueGroup<*>,
    aliases: List<String> = emptyList()
) : Mode(name, aliases), RotationProcessor {
    abstract fun calculateTicks(
        currentRotation: Rotation,
        targetRotation: Rotation
    ): Int
}
