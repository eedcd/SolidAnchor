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
package net.solidanchor.utils.aiming.features.processors

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.utils.percentageChance
import net.solidanchor.event.EventListener
import net.solidanchor.utils.aiming.RotationTarget
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.kotlin.random

/**
 * Short stop temporarily halts aiming at the target based on a specified rate.
 */
class ShortStopRotationProcessor(owner: EventListener? = null)
    : ToggleableValueGroup(owner, "ShortStop", false), RotationProcessor {

    private val rate = percentageChance("Rate", 3, 1..25)
    private var stopDuration by intRange("Duration", 1..2, 1..5,
        "ticks")

    private var ticksElapsed = 0
    private var currentTransitionInDuration = stopDuration.random()

    override fun process(
        rotationTarget: RotationTarget,
        currentRotation: Rotation,
        targetRotation: Rotation
    ): Rotation {
        if (!this.running) {
            return targetRotation
        }

        // If the rate is met, we will stop the rotation for a random duration
        if (rate.asBoolean) {
            currentTransitionInDuration = stopDuration.random()
            ticksElapsed = 0
        }

        return if (ticksElapsed < currentTransitionInDuration) {
            ticksElapsed++
            currentRotation.towardsLinear(targetRotation, (0.0f..0.1f).random(), (0.0f..0.1f).random())
        } else {
            targetRotation
        }
    }

}
