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

package net.solidanchor.features.module.modules.combat

import net.solidanchor.config.utils.percentageChance
import net.solidanchor.event.events.PlayerPostTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.kotlin.random

/**
 * When hitting an entity, the player will keep sprinting
 */
object ModuleKeepSprint : ClientModule("KeepSprint", ModuleCategories.COMBAT) {
    private val motion by floatRange("Motion", 100f..100f, 0f..100f, "%")
    private val motionWhenHurt by floatRange("MotionWhenHurt", 100f..100f, 0f..100f, "%")
    private val hurtTime by intRange("HurtTime", 1..10, 1..10)
    private val chance = percentageChance("Chance", 100f)

    // prevents getting slowed multiple times in a tick (without knockback item)
    var sprinting = false

    @Suppress("unused")
    private val postTickHandler = handler<PlayerPostTickEvent> {
        sprinting = player.isSprinting
    }

    fun getMotion(): Double {
        if (!chance.asBoolean) {
            return 0.6
        }

        return when {
            player.hurtTime in hurtTime -> motionWhenHurt
            else -> motion
        }.random() / 100.0
    }
}
