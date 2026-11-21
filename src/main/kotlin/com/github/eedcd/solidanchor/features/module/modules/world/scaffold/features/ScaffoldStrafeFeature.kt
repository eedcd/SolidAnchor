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
package com.github.eedcd.solidanchor.features.module.modules.world.scaffold.features

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.ModuleScaffold
import com.github.eedcd.solidanchor.utils.entity.moving
import com.github.eedcd.solidanchor.utils.entity.withStrafe
import net.minecraft.world.effect.MobEffects

object ScaffoldStrafeFeature : ToggleableValueGroup(ModuleScaffold, "Strafe", false) {

    private val speed by float("Speed", 0.247f, 0.0f..5.0f)
    private val hypixel by boolean("Hypixel", false)
    private val onlyOnGround by boolean("OnlyOnGround", false)

    private var moveTicks = 0

    override fun onEnabled() {
        moveTicks = 0
        super.onEnabled()
    }

    override fun onDisabled() {
        if (!hypixel) {
            return
        }
        player.deltaMovement = player.deltaMovement.multiply(
            0.5,
            1.0,
            0.5
        )
        super.onDisabled()
    }

    @Suppress("unused")
    private val moveTickHandler = handler<GameTickEvent> {
        if (player.moving) {
            moveTicks++
            return@handler
        }
        moveTicks = 0
    }

    @Suppress("unused")
    private val strafeHandler = handler<GameTickEvent> {
        if (onlyOnGround && !player.onGround()) {
            return@handler
        }

        if (hypixel) {
            var speed = 0.207

            if ((player.getEffect(MobEffects.SPEED)?.amplifier ?: -1) >= 0) {
                speed = 0.295
            }

            if (player.tickCount % 20 == 0 || moveTicks <= 7) {
                speed = 0.09800000190734863
            }

            player.deltaMovement = player.deltaMovement.withStrafe(speed = speed)
        } else {
            player.deltaMovement = player.deltaMovement.withStrafe(speed = speed.toDouble())
        }
    }
}
