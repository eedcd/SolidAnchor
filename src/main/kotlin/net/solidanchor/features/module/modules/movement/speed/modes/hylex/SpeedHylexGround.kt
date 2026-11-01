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
package net.solidanchor.features.module.modules.movement.speed.modes.hylex

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.minecraft.world.effect.MobEffects

/**
 * Hylex Ground
 *
 * Works because of a silly exemption from Hylex
 * @author @liquidsquid1
 */
class SpeedHylexGround(override val parent: ModeValueGroup<*>) : Mode("HylexGround") {

    private var groundTicks = 0

    override fun enable() {
        groundTicks = 0
        super.enable()
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (!player.onGround()) {
            groundTicks = 0
            return@handler
        }

        groundTicks++

        if (groundTicks <= 5) {
            return@handler
        }

        if (player.hurtTime >= 1) {
            return@handler
        }

        if ((player.getEffect(MobEffects.SPEED)?.amplifier ?: 0) >= 1) {
            return@handler
        }

        if (!(mc.options.keyLeft.isDown || mc.options.keyRight.isDown)) {
            player.deltaMovement = player.deltaMovement.multiply(
                1.2174,
                1.0,
                1.2174
            )
            return@handler
        }

        player.deltaMovement = player.deltaMovement.multiply(
            1.214,
            1.0,
            1.214
        )
    }
}
