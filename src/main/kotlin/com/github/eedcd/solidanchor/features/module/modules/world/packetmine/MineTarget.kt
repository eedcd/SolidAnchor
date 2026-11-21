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
package net.solidanchor.features.module.modules.world.packetmine

import net.solidanchor.event.TickLoopTaskExecutor
import net.solidanchor.render.EMPTY_BOX
import net.solidanchor.utils.block.stateOrEmpty
import net.solidanchor.utils.client.network
import net.solidanchor.utils.client.player
import net.solidanchor.utils.client.world
import net.solidanchor.utils.math.distanceToSqr
import net.solidanchor.utils.math.sq
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.level.block.state.BlockState

class MineTarget(val targetPos: BlockPos) {

    var finished = false
    var progress = 0f
    var started = false
    var finishReadyTick: Long? = null
    var direction: Direction? = null
    var blockState = targetPos.stateOrEmpty
        private set

    fun init() {
        with(ModulePacketMine) {
            targetRenderer.addBlock(targetPos, box = EMPTY_BOX.inflate(1e-5))
        }
    }

    fun cleanUp() {
        with(ModulePacketMine) {
            targetRenderer.removeBlock(targetPos)
            if (!finished && mode.activeMode.canAbort) {
                abort(true)
            }
        }
    }

    fun updateBlockState() {
        blockState = targetPos.stateOrEmpty
    }

    fun isInvalidOrOutOfRange(): Boolean {
        val state = targetPos.stateOrEmpty
        val invalid = ModulePacketMine.mode.activeMode.isInvalid(this, state)
        return invalid || isOutOfRange(targetPos, state)
    }

    private fun isOutOfRange(pos: BlockPos, state: BlockState): Boolean {
        val outlineShape = state.getShape(world, pos).move(pos)
        return outlineShape.distanceToSqr(player.eyePosition) > ModulePacketMine.keepRange.sq()
    }

    fun abort(force: Boolean = false) {
        val notPossible = !started || finished || !ModulePacketMine.mode.activeMode.canAbort
        if (notPossible || !force && !isOutOfRange(targetPos, targetPos.stateOrEmpty)) {
            return
        }

        val dir = if (ModulePacketMine.abortAlwaysDown) {
            Direction.DOWN
        } else {
            direction ?: Direction.DOWN
        }

        TickLoopTaskExecutor.executeInTickLoop {
            network.send(
                ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                    targetPos,
                    dir,
                )
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        return targetPos == (other as MineTarget).targetPos
    }

    override fun hashCode(): Int {
        return targetPos.hashCode()
    }

    fun copy() = MineTarget(targetPos)

}
