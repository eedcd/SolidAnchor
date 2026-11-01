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

package net.solidanchor.features.module.modules.combat.aimbot

import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.solidanchor.utils.combat.TargetSelector
import net.solidanchor.utils.entity.handItems
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.render.trajectory.HeldItemTrajectoryResolver

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
