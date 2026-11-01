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
package net.solidanchor.features.module.modules.world.packetmine.mode

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.features.module.modules.world.packetmine.MineTarget
import net.solidanchor.features.module.modules.world.packetmine.ModulePacketMine
import net.solidanchor.utils.block.isBreakable
import net.solidanchor.utils.block.isNotBreakable
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

sealed class MineMode(
    name: String,
    val canManuallyChange: Boolean = true,
    val canAbort: Boolean = true,
    val stopOnStateChange: Boolean = true
) : Mode(name) {

    open fun isInvalid(mineTarget: MineTarget, state: BlockState): Boolean {
        return state.isNotBreakable(mineTarget.targetPos) && !player.isCreative || state.isAir
    }

    open fun shouldTarget(blockPos: BlockPos, state: BlockState): Boolean {
        return state.isBreakable(blockPos)
    }

    open fun onCannotLookAtTarget(mineTarget: MineTarget) {}

    open fun shouldPreventTargetChange(mineTarget: MineTarget): Boolean {
        return false
    }

    abstract fun start(mineTarget: MineTarget)

    abstract fun finish(mineTarget: MineTarget)

    abstract fun shouldUpdate(
        mineTarget: MineTarget,
        slot: HotbarItemSlot?
    ): Boolean

    final override val parent: ModeValueGroup<*>
        get() = ModulePacketMine.mode

}
