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

package com.github.eedcd.solidanchor.utils.block.placer

import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.utils.block.immutable
import com.github.eedcd.solidanchor.utils.block.isUnobstructed
import com.github.eedcd.solidanchor.utils.block.isInteractable
import com.github.eedcd.solidanchor.utils.block.state
import com.github.eedcd.solidanchor.utils.block.targetfinding.BlockOffsetOptions
import com.github.eedcd.solidanchor.utils.block.targetfinding.BlockPlacementTargetFindingOptions
import com.github.eedcd.solidanchor.utils.block.targetfinding.ClickableCenterTargetPositionFactory
import com.github.eedcd.solidanchor.utils.block.targetfinding.FaceHandlingOptions
import com.github.eedcd.solidanchor.utils.block.targetfinding.PlayerLocationOnPlacement
import com.github.eedcd.solidanchor.utils.block.targetfinding.findBestBlockPlacementTarget
import com.github.eedcd.solidanchor.utils.client.network
import com.github.eedcd.solidanchor.utils.client.player
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.BlockState

fun BlockPlacer.placeInstantOnBlockUpdate(event: PacketEvent) {
    when (val packet = event.packet) {
        is ClientboundBlockUpdatePacket -> placeInstant(packet.pos, packet.blockState)
        is ClientboundSectionBlocksUpdatePacket -> {
            packet.runUpdates { pos, state -> placeInstant(pos, state) }
        }
    }
}

private fun BlockPlacer.placeInstant(pos: BlockPos, state: BlockState) {
    val posAsLong = pos.asLong()
    val irrelevantPacket = !state.canBeReplaced() || posAsLong !in blocks

    val rotationMode = rotationMode.activeMode
    if (irrelevantPacket || rotationMode !is NoRotationMode || !pos.isUnobstructed()) {
        return
    }

    val searchOptions = BlockPlacementTargetFindingOptions(
        BlockOffsetOptions.Default,
        FaceHandlingOptions(ClickableCenterTargetPositionFactory, considerFacingAwayFaces = wallRange > 0),
        stackToPlaceWith = Items.SANDSTONE.defaultInstance,
        PlayerLocationOnPlacement(position = player.position(), pose = player.pose),
    )

    val placementTarget = findBestBlockPlacementTarget(pos, searchOptions) ?: return

    // Check if we can reach the target
    if (!canReach(placementTarget.interactedBlockPos, placementTarget.rotation)) {
        return
    }

    if (placementTarget.interactedBlockPos.state.isInteractable) {
        return
    }

    if (rotationMode.send) {
        val rotation = placementTarget.rotation.normalize()
        network.send(
            ServerboundMovePlayerPacket.Rot(rotation.yaw, rotation.pitch, player.onGround(),
                player.horizontalCollision)
        )
    }

    // A support position must be replaced with a support block again, not with the module's own item.
    doPlacement(blocks[posAsLong], pos.immutable, placementTarget)
}
