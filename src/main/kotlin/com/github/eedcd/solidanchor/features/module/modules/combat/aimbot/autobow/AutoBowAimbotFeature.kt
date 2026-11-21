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

package net.solidanchor.features.module.modules.combat.aimbot.autobow

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.combat.aimbot.ModuleAutoBow
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.solidanchor.utils.combat.TargetPriority
import net.solidanchor.utils.combat.TargetTracker
import net.solidanchor.utils.entity.handItems
import net.solidanchor.utils.entity.usingItemOrNull
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.render.TargetRenderer
import net.solidanchor.utils.render.trajectory.HeldItemTrajectoryResolver
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.TridentItem

/**
 * Automatically shoots with your bow when you aim correctly at an enemy or when the bow is fully charged.
 */
object AutoBowAimbotFeature : ToggleableValueGroup(ModuleAutoBow, "BowAimbot", true) {

    val targetTracker = TargetTracker(TargetPriority.DISTANCE)
    private val rotations = RotationsValueGroup(this)
    private val throughWalls by boolean("ThroughWalls", true)

    init {
        tree(targetTracker)
        tree(rotations)
        tree(TargetRenderer(AutoBowAimbotFeature, targetTracker))
    }

    @Suppress("unused")
    private val tickRepeatable = handler<GameTickEvent> {
        targetTracker.reset()

        // Should check if player is using bow
        val activeStack = player.usingItemOrNull ?: player.handItems.firstOrNull {
            it.item is CrossbowItem && CrossbowItem.isCharged(it)
        }
        val activeItem = activeStack?.item

        if (activeItem !is BowItem && activeItem !is TridentItem && activeItem !is CrossbowItem) {
            return@handler
        }

        val trajectoryDescriptor = HeldItemTrajectoryResolver.resolveHeldItemPrimaryShot(
            player,
            activeStack,
            true
        ) ?: return@handler

        var rotation: Rotation? = null
        val calculator = if (throughWalls) {
            SituationalProjectileAngleCalculator
        } else {
            SituationalProjectileAngleCalculator.VerifyHitResult
        }
        targetTracker.selectFirst { enemy ->
            rotation = calculator.calculateAngleForEntity(trajectoryDescriptor.trajectoryInfo, enemy)
            rotation != null
        } ?: return@handler

        RotationManager.setRotationTarget(
            rotation!!,
            priority = Priority.IMPORTANT_FOR_USAGE_1,
            provider = ModuleAutoBow,
            valueGroup = rotations
        )
    }

}
