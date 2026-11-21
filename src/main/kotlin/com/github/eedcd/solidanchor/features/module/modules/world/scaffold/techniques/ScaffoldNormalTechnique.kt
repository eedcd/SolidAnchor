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

import net.solidanchor.features.module.modules.movement.ModuleFreeze
import net.solidanchor.features.module.modules.world.scaffold.ModuleScaffold.getTargetedPosition
import net.solidanchor.features.module.modules.world.scaffold.features.ScaffoldCeilingFeature
import net.solidanchor.features.module.modules.world.scaffold.features.ScaffoldHeadHitterFeature
import net.solidanchor.features.module.modules.world.scaffold.techniques.normal.ScaffoldDownFeature
import net.solidanchor.features.module.modules.world.scaffold.techniques.normal.ScaffoldEagleFeature
import net.solidanchor.features.module.modules.world.scaffold.techniques.normal.ScaffoldStabilizeMovementFeature
import net.solidanchor.features.module.modules.world.scaffold.techniques.normal.ScaffoldTellyFeature
import net.solidanchor.features.module.modules.world.scaffold.techniques.normal.ScaffoldTellyFeature.Mode
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.block.targetfinding.AimMode
import net.solidanchor.utils.block.targetfinding.AngleYawTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.BlockOffsetOptions
import net.solidanchor.utils.block.targetfinding.BlockPlacementTarget
import net.solidanchor.utils.block.targetfinding.BlockPlacementTargetFindingOptions
import net.solidanchor.utils.block.targetfinding.BlockPosOffsets
import net.solidanchor.utils.block.targetfinding.CenterTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.DiagonalYawTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.EdgePointTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.FaceHandlingOptions
import net.solidanchor.utils.block.targetfinding.FaceTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.FailedClick
import net.solidanchor.utils.block.targetfinding.NearestRotationTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.PlayerLocationOnPlacement
import net.solidanchor.utils.block.targetfinding.RandomTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.ReverseYawTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.StabilizedRotationTargetPositionFactory
import net.solidanchor.utils.block.targetfinding.findBestBlockPlacementTarget
import net.solidanchor.utils.block.targetfinding.verifyClick
import net.solidanchor.utils.entity.rotation
import net.solidanchor.utils.math.geometry.Line
import net.solidanchor.utils.math.toBlockPos
import net.solidanchor.utils.raytracing.traceFromPlayer
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.round

/**
 * Normal technique, which is basically just normal scaffold.
 */
object ScaffoldNormalTechnique : ScaffoldTechnique("Normal") {

    private val aimMode by enumChoice("RotationMode", AimMode.STABILIZED)
    private val requiresSight by boolean("RequiresSight", false)

    init {
        tree(ScaffoldEagleFeature)
        tree(ScaffoldTellyFeature)
        tree(ScaffoldDownFeature)
        tree(ScaffoldStabilizeMovementFeature)
        tree(ScaffoldCeilingFeature)
        tree(ScaffoldHeadHitterFeature)
    }

    override fun findPlacementTarget(
        predictedPos: Vec3,
        predictedPose: Pose,
        optimalLine: Line?,
        bestStack: ItemStack
    ): BlockPlacementTarget? {
        val priorityComparator = priorityComparator(predictedPos, optimalLine)

        val offsets = if (ModuleFreeze.running) {
            BlockPosOffsets.FULL.offsets
        } else if (ScaffoldDownFeature.shouldGoDown) {
            BlockPosOffsets.DOWN.offsets
        } else {
            BlockPosOffsets.NORMAL.offsets
        }

        // Face position factory for current config
        val facePositionFactory = getFacePositionFactory(optimalLine)

        val searchOptions = BlockPlacementTargetFindingOptions(
            BlockOffsetOptions(
                offsets,
                priorityComparator,
            ),
            FaceHandlingOptions(
                facePositionFactory,
                considerFacingAwayFaces = ScaffoldDownFeature.shouldGoDown
            ),
            stackToPlaceWith = bestStack,
            PlayerLocationOnPlacement(position = predictedPos, pose = predictedPose),
        )
        return findBestBlockPlacementTarget(getTargetedPosition(predictedPos.toBlockPos()), searchOptions)
    }

    override fun getRotations(target: BlockPlacementTarget?): Rotation? {
        if (ScaffoldTellyFeature.enabled && ScaffoldTellyFeature.doNotAim) {
            return when (ScaffoldTellyFeature.resetMode) {
                Mode.REVERSE -> Rotation(
                    round(player.rotation.yaw / 45) * 45,
                    if (player.xRot < 45f) 45f else player.xRot
                )

                Mode.RESET -> null
            }
        }

        if (requiresSight) {
            val target = target ?: return null
            val raycast = traceFromPlayer(rotation = target.rotation)

            if (raycast.type == HitResult.Type.BLOCK && raycast.blockPos == target.interactedBlockPos) {
                return target.rotation
            }
        }

        return super.getRotations(target)
    }

    override fun getCrosshairTarget(target: BlockPlacementTarget?, rotation: Rotation): BlockHitResult? =
        target?.verifyClick(
            rotation,
            // Going down allows a non-visible hit result
            onFailure = if (ScaffoldDownFeature.shouldGoDown) FailedClick.PLANNED_HIT else FailedClick.NOTHING,
        )

    private fun getFacePositionFactory(optimalLine: Line?): FaceTargetPositionFactory = when (aimMode) {
        AimMode.CENTER -> CenterTargetPositionFactory
        AimMode.RANDOM -> RandomTargetPositionFactory
        AimMode.STABILIZED -> StabilizedRotationTargetPositionFactory(optimalLine)
        AimMode.NEAREST_ROTATION -> NearestRotationTargetPositionFactory
        AimMode.REVERSE_YAW -> ReverseYawTargetPositionFactory
        AimMode.DIAGONAL_YAW -> DiagonalYawTargetPositionFactory
        AimMode.ANGLE_YAW -> AngleYawTargetPositionFactory
        AimMode.EDGE_POINT -> EdgePointTargetPositionFactory
    }

}
