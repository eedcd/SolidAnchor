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

package com.github.eedcd.solidanchor.features.module.modules.combat.aimbot

import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.utils.aiming.RotationManager
import com.github.eedcd.solidanchor.utils.aiming.RotationsValueGroup
import com.github.eedcd.solidanchor.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import com.github.eedcd.solidanchor.utils.combat.TargetSelector
import com.github.eedcd.solidanchor.utils.entity.handItems
import com.github.eedcd.solidanchor.utils.kotlin.Priority
import com.github.eedcd.solidanchor.utils.render.trajectory.HeldItemTrajectoryResolver

object ModuleProjectileAimbot : ClientModule("ProjectileAimbot", ModuleCategories.COMBAT) {

    private val targetSelector = TargetSelector()
    private val rotations = RotationsValueGroup(this)

    init {
        tree(targetSelector)
        tree(rotations)
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        val target = targetSelector.targets().firstOrNull() ?: return@handler

        val rotation = player.handItems.firstNotNullOfOrNull {
            val trajectoryDescriptor = HeldItemTrajectoryResolver.resolveHeldItemPrimaryShot(
                player,
                it,
                true
            ) ?: return@firstNotNullOfOrNull null

            SituationalProjectileAngleCalculator.calculateAngleForEntity(
                trajectoryDescriptor.trajectoryInfo,
                target
            )
        } ?: return@handler

        RotationManager.setRotationTarget(
            rotation,
            considerInventory = false,
            rotations,
            Priority.IMPORTANT_FOR_USAGE_1,
            ModuleProjectileAimbot
        )
    }



}
