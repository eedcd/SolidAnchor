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
package com.github.eedcd.solidanchor.utils.block.liquid

import com.github.eedcd.solidanchor.utils.block.targetfinding.BlockOffsetOptions
import com.github.eedcd.solidanchor.utils.block.targetfinding.BlockPlacementTargetFindingOptions
import com.github.eedcd.solidanchor.utils.block.targetfinding.CenterTargetPositionFactory
import com.github.eedcd.solidanchor.utils.block.targetfinding.FaceHandlingOptions
import com.github.eedcd.solidanchor.utils.block.targetfinding.PlacementPlan
import com.github.eedcd.solidanchor.utils.block.targetfinding.PlayerLocationOnPlacement
import com.github.eedcd.solidanchor.utils.block.targetfinding.findBestBlockPlacementTarget
import com.github.eedcd.solidanchor.utils.client.player
import com.github.eedcd.solidanchor.utils.inventory.HotbarItemSlot
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

internal fun planPlacementAtPos(
    pos: BlockPos,
    slot: HotbarItemSlot,
    placementPlayerPos: Vec3 = player.position(),
): PlacementPlan? {
    val options = BlockPlacementTargetFindingOptions(
        BlockOffsetOptions.Default,
        FaceHandlingOptions(CenterTargetPositionFactory),
        stackToPlaceWith = slot.itemStack,
        PlayerLocationOnPlacement(position = placementPlayerPos),
    )

    val bestPlacementPlan = findBestBlockPlacementTarget(pos, options) ?: return null

    return PlacementPlan(pos, bestPlacementPlan, slot)
}
