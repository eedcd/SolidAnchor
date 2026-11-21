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
package com.github.eedcd.solidanchor.features.module.modules.render

import com.github.eedcd.solidanchor.config.types.list.Tagged
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.utils.render.placement.PlacementRenderer
import com.github.eedcd.solidanchor.utils.world.entityGetter
import com.github.eedcd.solidanchor.utils.world.forEach
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.world.entity.EntityTypes

object ModuleProphuntESP : ClientModule("ProphuntESP", ModuleCategories.RENDER,
    aliases = listOf("BlockUpdateDetector", "FallingBlockESP")
) {

    private val renderer = PlacementRenderer("RenderBlockUpdates", true, this,
        defaultColor = Color4b(255, 179, 72, 90), keep = false
    )

    private val tracking by multiEnumChoice("Tracking", Tracking.entries, canBeNone = false)

    private enum class Tracking(override val tag: String): Tagged {
        FALLING_BLOCKS("FallingBlocks"),
        BLOCK_UPDATES("BlockUpdates"),
        CHUNK_DELTA_UPDATES("ChunkDeltaUpdates"),
    }

    init {
        tree(renderer)
    }

    override fun onDisabled() {
        renderer.clearSilently()
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (Tracking.FALLING_BLOCKS in tracking) {
            world.entityGetter.forEach(EntityTypes.FALLING_BLOCK) {
                renderer.addBlock(it.blockPosition(), update = false)
            }
        }
        renderer.updateAll()
    }

    @Suppress("unused")
    private val networkHandler = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is ClientboundBlockUpdatePacket if Tracking.BLOCK_UPDATES in tracking -> mc.execute {
                renderer.addBlock(packet.pos, update = false)
            }

            is ClientboundSectionBlocksUpdatePacket if Tracking.CHUNK_DELTA_UPDATES in tracking -> mc.execute {
                packet.runUpdates { pos, _ -> renderer.addBlock(pos, update = false) }
            }
        }
    }
}
