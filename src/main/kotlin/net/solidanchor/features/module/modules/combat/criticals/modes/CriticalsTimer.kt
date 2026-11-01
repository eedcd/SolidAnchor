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
package net.solidanchor.features.module.modules.combat.criticals.modes

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.combat.criticals.ModuleCriticals
import net.solidanchor.features.module.modules.combat.criticals.ModuleCriticals.wouldDoCriticalHit
import net.solidanchor.utils.client.Timer
import net.solidanchor.utils.combat.findEnemy
import net.solidanchor.utils.kotlin.Priority

object CriticalsTimer : Mode("Timer") {

    override val parent: ModeValueGroup<*>
        get() = ModuleCriticals.modes

    private val speed by float("Speed", 0.8f, 0.1f..1.0f)
    private val range by float("Range", 4.0f, 0.0f..10.0f)

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        world.findEnemy(0.0f, range) ?: return@handler

        if (wouldDoCriticalHit(ignoreSprint = true)) {
            Timer.requestTimerSpeed(speed, Priority.IMPORTANT_FOR_USAGE_1, ModuleCriticals)
        }

    }

}


