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
package net.solidanchor.features.module.modules.world.scaffold.techniques

import net.solidanchor.features.module.modules.world.scaffold.ModuleScaffold.getTargetedPosition
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.block.targetfinding.BlockOffsetOptions
import net.solidanchor.utils.block.targetfinding.BlockPlacementTarget
import net.solidanchor.utils.block.targetfinding.BlockPlacementTargetFindingOptions
import net.solidanchor.utils.block.targetfinding.CenterTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.FaceHandlingOptions
import net.solidanchor.utils.block.targetfinding.FailedClick
import net.solidanchor.utils.block.targetfinding.PlayerLocationOnPlacement
import net.solidanchor.utils.block.targetfinding.findBestBlockPlacementTarget
import net.solidanchor.utils.block.targetfinding.verifyClick
import net.solidanchor.utils.math.center
import net.solidanchor.utils.math.toRadians
import net.solidanchor.utils.math.geometry.Line
import net.solidanchor.utils.math.toBlockPos
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Normal technique, which is basically just normal scaffold.
 */
object ScaffoldExpandTechnique : ScaffoldTechnique("Expand") {

    private val expandLength by int("Length", 4, 1..10, "blocks")

    override fun findPlacementTarget(
        predictedPos: Vec3,
        predictedPose: Pose,
        optimalLine: Line?,
        bestStack: ItemStack
    ): BlockPlacementTarget? {
        val searchOptions = BlockPlacementTargetFindingOptions(
            BlockOffsetOptions.Default,
            FaceHandlingOptions(
                CenterTargetPositionFactory,
                considerFacingAwayFaces = true
            ),
            stackToPlaceWith = bestStack,
            PlayerLocationOnPlacement(position = predictedPos, pose = predictedPose)
        )

        for (i in 0..expandLength) {
            val position = getTargetedPosition(expandPos(predictedPos, i))

            return findBestBlockPlacementTarget(position, searchOptions) ?: continue
        }

        return null
    }

    override fun getRotations(target: BlockPlacementTarget?): Rotation? {
        val blockCenter = target?.placedBlock?.center ?: return null

        return Rotation.lookingAt(point = blockCenter, from = player.eyePosition)
    }

    override fun getCrosshairTarget(target: BlockPlacementTarget?, rotation: Rotation): BlockHitResult? =
        // Expanding must be able to place behind blocks, so a non-visible hit result is allowed
        target?.verifyClick(rotation, onFailure = FailedClick.PLANNED_HIT)

    private fun expandPos(position: Vec3, expand: Int, yaw: Float = player.yRot) = position.toBlockPos().offset(
        (-sin(yaw.toRadians()) * expand).toInt(),
        0,
        (cos(yaw.toRadians()) * expand).toInt()
    )

}
