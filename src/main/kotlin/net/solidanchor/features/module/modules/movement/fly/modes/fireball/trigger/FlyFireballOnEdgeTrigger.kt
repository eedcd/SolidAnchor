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

package net.solidanchor.features.module.modules.movement.fly.modes.fireball.trigger

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.fly.modes.fireball.FlyFireball
import net.solidanchor.utils.entity.isCloseToEdge
import net.solidanchor.utils.kotlin.EventPriorityConvention

object FlyFireballOnEdgeTrigger : Mode("OnEdge") {

    override val parent: ModeValueGroup<Mode>
        get() = FlyFireball.trigger

    private val edgeDistance by float("EdgeDistance", 0.01f, 0.01f..0.5f)

    @Suppress("unused")
    val inputHandler = handler<MovementInputEvent>(
        priority = EventPriorityConvention.OBJECTION_AGAINST_EVERYTHING
    ) { event ->
        val shouldBeActive = player.onGround() && !player.isShiftKeyDown

        if (shouldBeActive && player.isCloseToEdge(event.directionalInput, edgeDistance.toDouble())) {
            FlyFireball.wasTriggered = true
        }
    }

}
