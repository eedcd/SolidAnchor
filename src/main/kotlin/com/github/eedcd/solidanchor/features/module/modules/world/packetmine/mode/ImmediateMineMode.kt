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

import net.solidanchor.features.module.modules.world.packetmine.MineTarget
import net.solidanchor.features.module.modules.world.packetmine.ModulePacketMine
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.InteractionHand

object ImmediateMineMode : MineMode("Immediate", canManuallyChange = false, canAbort = false) {

    private val waitForConfirm by boolean("WaitForConfirm", true)

    override fun start(mineTarget: MineTarget) {
        NormalMineMode.start(mineTarget)
        interaction.startPrediction(world) { sequence ->
            ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                mineTarget.targetPos,
                mineTarget.direction!!,
                sequence,
            )
        }
        ModulePacketMine.swingMode.swing(InteractionHand.MAIN_HAND)
    }

    override fun finish(mineTarget: MineTarget) {
        if (!waitForConfirm) {
            mineTarget.finished = true
            ModulePacketMine._resetTarget()
        }
    }

    override fun shouldUpdate(mineTarget: MineTarget, slot: HotbarItemSlot?): Boolean {
        return mineTarget.progress < ModulePacketMine.breakDamage
    }

}
