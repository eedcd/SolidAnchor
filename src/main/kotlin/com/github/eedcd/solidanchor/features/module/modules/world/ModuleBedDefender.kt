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
package com.github.eedcd.solidanchor.features.module.modules.world

import it.unimi.dsi.fastutil.ints.IntLongPair
import net.ccbluex.fastutil.component1
import net.ccbluex.fastutil.component2
import com.github.eedcd.solidanchor.event.events.RotationUpdateEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleDebug
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleDebug.debugGeometry
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.utils.block.UnbreakableFirst
import com.github.eedcd.solidanchor.utils.block.bed.isSelfBedChoices
import com.github.eedcd.solidanchor.utils.block.placer.BlockPlacer
import com.github.eedcd.solidanchor.utils.block.searchBedLayer
import com.github.eedcd.solidanchor.utils.block.searchBlocksInCuboid
import com.github.eedcd.solidanchor.utils.inventory.HotbarItemSlot
import com.github.eedcd.solidanchor.utils.inventory.ItemSlot
import com.github.eedcd.solidanchor.utils.inventory.Slots
import com.github.eedcd.solidanchor.utils.item.isAnyChest
import com.github.eedcd.solidanchor.utils.item.isFullBlock
import com.github.eedcd.solidanchor.utils.kotlin.Priority
import com.github.eedcd.solidanchor.utils.math.center
import com.github.eedcd.solidanchor.utils.math.distanceToCenterSqr
import com.github.eedcd.solidanchor.utils.math.sq
import com.github.eedcd.solidanchor.utils.sorting.ComparatorChain
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.BedBlock
import net.minecraft.world.phys.AABB
import kotlin.math.max

object ModuleBedDefender : ClientModule("BedDefender", category = ModuleCategories.WORLD) {

    private val maxLayers by int("MaxLayers", 1, 1..5)
    private val allowChests by boolean("AllowChests", false)

    private val isSelfBedMode = choices("SelfBed", 0, ::isSelfBedChoices)

    private val blockSlotComparator = ComparatorChain<HotbarItemSlot>(
        Comparator.comparing({
            (it.itemStack.item as BlockItem).block
        }, UnbreakableFirst),
        ItemSlot.PREFER_MORE_ITEM,
        HotbarItemSlot.PREFER_NEARBY,
    )

    // Layer(ASC) Center Distance(DESC)
    private val placementTargetComparator = Comparator
        .comparingInt(IntLongPair::leftInt)
        .thenComparingDouble {
            -player.eyePosition.distanceToCenterSqr(it.rightLong())
        }

    private fun findBestBlockSlot(): HotbarItemSlot? {
        return Slots.OffhandWithHotbar
            .filter {
                val itemStack = it.itemStack
                itemStack.isFullBlock() || allowChests && itemStack.isAnyChest
            }
            .minWithOrNull(blockSlotComparator)
    }

    private val placer = tree(BlockPlacer("Place", this, Priority.NOT_IMPORTANT, {
        findBestBlockSlot()
    }, false))

    private val requiresSneak by boolean("RequiresSneak", false)

    @Suppress("unused")
    private val targetUpdater = handler<RotationUpdateEvent> {
        if (!placer.canAct()) {
            return@handler
        }

        // The placer knows nothing about this option, so an empty queue is the only way to hold back
        // placements while the player is not sneaking.
        if (requiresSneak && !player.isShiftKeyDown) {
            placer.clear()
            return@handler
        }

        placer.slotFinder.apply(null) ?: return@handler

        val eyesPos = player.eyePosition
        val placementRange = max(placer.range, placer.wallRange)
        val rangeSq = placementRange.toDouble().sq()

        // The bed that need to be defended may be already covered, so we search further
        val bedBlocks = eyesPos.searchBlocksInCuboid(placementRange + maxLayers + 1) { pos, state ->
            val block = state.block
            when {
                block !is BedBlock -> false
                else -> isSelfBedMode.activeMode.shouldDefend(block, pos)
            }
        }

        // Get the closest bed block
        val (blockPos, state) = bedBlocks.minByOrNull {
            (blockPos, _) -> blockPos.distToCenterSqr(eyesPos)
        } ?: run {
            // No bed left to defend, so the previous queue must not keep being placed.
            placer.clear()
            return@handler
        }

        val placementPositions = blockPos.searchBedLayer(state, maxLayers)
            .filterTo(mutableListOf()) { (_, pos) ->
                AABB(BlockPos.of(pos)).distanceToSqr(eyesPos) <= rangeSq
            }

        if (placementPositions.isEmpty()) {
            placer.clear()
            return@handler
        }

        placementPositions.sortWith(placementTargetComparator)

        debugGeometry("PlacementPositions") {
            ModuleDebug.DebugCollection(
                placementPositions.map { (_, pos) ->
                    ModuleDebug.DebuggedPoint(BlockPos.of(pos).center, Color4b.RED.with(a = 100))
                }
            )
        }

        // Need ordered set (like TreeSet/LinkedHashSet)
        placer.update(placementPositions.mapTo(linkedSetOf()) { BlockPos.of(it.rightLong()) })
    }

    override fun onDisabled() {
        placer.disable()
    }

}
