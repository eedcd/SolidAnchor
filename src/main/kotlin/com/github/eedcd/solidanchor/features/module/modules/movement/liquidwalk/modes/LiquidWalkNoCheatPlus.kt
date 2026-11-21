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

package com.github.eedcd.solidanchor.features.module.modules.movement.liquidwalk.modes

import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.event.events.BlockShapeEvent
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.events.PlayerJumpEvent
import com.github.eedcd.solidanchor.event.events.TransferOrigin
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.movement.liquidwalk.ModuleLiquidWalk
import com.github.eedcd.solidanchor.features.module.modules.movement.liquidwalk.ModuleLiquidWalk.collidesWithAnythingElse
import com.github.eedcd.solidanchor.features.module.modules.movement.liquidwalk.ModuleLiquidWalk.standingOnWater
import com.github.eedcd.solidanchor.utils.block.isBlockAtPosition
import com.github.eedcd.solidanchor.utils.entity.box
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.phys.shapes.Shapes

/**
 * @anticheat NoCheatPlus
 * @anticheatVersion 3.16.1-SNAPSHOT-sMD5NET-b115s
 * @testedOn eu.loyisa.cn and poke.sexy
 */
internal object LiquidWalkNoCheatPlus : Mode("NoCheatPlus") {

    override val parent: ModeValueGroup<Mode>
        get() = ModuleLiquidWalk.modes

    private var shiftDown = false

    @Suppress("unused")
    val shapeHandler = handler<BlockShapeEvent> { event ->
        if (mc.options.keyShift.isDown || player.fallDistance > 3.0f || player.isOnFire) {
            return@handler
        }

        val block = event.state.block

        if (block is LiquidBlock && !player.box.isBlockAtPosition { it is LiquidBlock }) {
            event.shape = Shapes.block()
        }
    }

    val repeatable = handler<GameTickEvent> {
        if (player.box.isBlockAtPosition { it is LiquidBlock } && !mc.options.keyShift.isDown) {
            player.deltaMovement.y = 0.08
        }
    }

    val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (event.origin == TransferOrigin.OUTGOING && packet is ServerboundMovePlayerPacket) {
            if (!mc.options.keyShift.isDown &&
                !player.isInWater &&
                standingOnWater() &&
                !collidesWithAnythingElse()
                ) {
                if (shiftDown) {
                    packet.y -= 0.001
                }

                shiftDown = !shiftDown
            }
        }
    }

    @Suppress("unused")
    val jumpHandler = handler<PlayerJumpEvent> { event ->
        if (standingOnWater()) {
            event.cancelEvent()
        }
    }

}
