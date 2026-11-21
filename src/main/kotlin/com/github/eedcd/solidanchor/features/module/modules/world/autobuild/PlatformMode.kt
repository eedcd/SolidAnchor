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
package net.solidanchor.features.module.modules.world.autobuild

import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.RotationUpdateEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.world.autobuild.ModuleAutoBuild.placer
import net.solidanchor.utils.block.state
import net.solidanchor.utils.collection.Filter
import net.solidanchor.utils.collection.blockSortedSetOf
import net.solidanchor.utils.collection.getSlot
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

object PlatformMode : ModuleAutoBuild.AutoBuildMode("Platform") {

    private val disableOnYChange by boolean("DisableOnYChange", true)
    private val filter by enumChoice("Filter", Filter.WHITELIST)
    private val blocks by blocks("Blocks", blockSortedSetOf(Blocks.OBSIDIAN))
    private val platformSize by int("Size", 3, 1..6)

    private var startY = 0.0

    override fun enabled() {
        startY = player.position().y
    }

    @Suppress("unused")
    private val repeatable = handler<GameTickEvent> {
        if (disableOnYChange && player.position().y != startY) {
            ModuleAutoBuild.enabled = false
        }
    }

    @Suppress("unused")
    private val targetUpdater = handler<RotationUpdateEvent> {
        val blocks1 = hashSetOf<BlockPos>()
        val center = BlockPos.containing(player.position()).below()
        val pos = center.mutable()
        for (x in center.x - platformSize..center.x + platformSize) {
            for (z in center.z - platformSize..center.z + platformSize) {
                pos.x = x
                pos.z = z
                if (pos.state!!.canBeReplaced()) {
                    blocks1.add(pos.immutable())
                }
            }
        }

        placer.update(blocks1)
    }

    override fun getSlot(): HotbarItemSlot? = filter.getSlot(blocks)

}
