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
package net.solidanchor.features.module.modules.movement.highjump

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PlayerJumpEvent
import net.solidanchor.event.handler
import net.solidanchor.event.sequenceHandler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories

/**
 * HighJump module
 *
 * Allows you to jump higher.
 */
object ModuleHighJump : ClientModule("HighJump", ModuleCategories.MOVEMENT) {

    private val modes = choices(
        "Mode", Vanilla, arrayOf(
            Vanilla, Vulcan
        )
    ).apply { tagBy(this) }
    private val motion by float("Motion", 0.8f, 0.2f..10f)

    private object Vanilla : Mode("Vanilla") {

        override val parent: ModeValueGroup<Mode>
            get() = modes

        @Suppress("unused")
        val jumpEvent = sequenceHandler<PlayerJumpEvent> {
            it.motion = motion
        }
    }

    /**
     * @anticheat Vulcan
     * @anticheatVersion 2.7.5
     * @testedOn eu.loyisa.cn; eu.anticheat-test.com
     * @note this still flags a bit
     */
    private object Vulcan : Mode("Vulcan") {

        override val parent: ModeValueGroup<Mode>
            get() = modes

        private val glide by boolean("Glide", false)

        private var shouldGlide = false

        @Suppress("unused")
        val repeatable = handler<GameTickEvent> {
            if (glide && shouldGlide) { // if the variable is true, then glide
                if (player.onGround()) {
                    shouldGlide = false
                    return@handler
                }
                if (player.fallDistance > 0) {
                    if (player.tickCount % 2 == 0) {
                        player.deltaMovement.y = -0.155
                    }
                } else {
                    player.deltaMovement.y = -0.1
                }
            }
        }

        @Suppress("unused")
        val jumpEvent = sequenceHandler<PlayerJumpEvent> {
            it.motion = motion
            waitTicks(100)
            player.deltaMovement.y = 0.0
            shouldGlide = true
        }
    }
}
