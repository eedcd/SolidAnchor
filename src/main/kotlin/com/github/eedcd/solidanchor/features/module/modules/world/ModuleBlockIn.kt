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

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.ccbluex.fastutil.enumSetAllOf
import com.github.eedcd.solidanchor.config.types.list.Tagged
import com.github.eedcd.solidanchor.event.events.NotificationEvent
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.events.PlayerMovementTickEvent
import com.github.eedcd.solidanchor.event.events.TransferOrigin
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.tickHandler
import com.github.eedcd.solidanchor.event.tickUntil
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleDebug.debugParameter
import com.github.eedcd.solidanchor.utils.block.UnbreakableFirst
import com.github.eedcd.solidanchor.utils.block.placer.BlockPlacer
import com.github.eedcd.solidanchor.utils.block.stateOrEmpty
import com.github.eedcd.solidanchor.utils.client.notification
import com.github.eedcd.solidanchor.utils.collection.Filter
import com.github.eedcd.solidanchor.utils.collection.blockSortedSetOf
import com.github.eedcd.solidanchor.utils.inventory.HotbarItemSlot
import com.github.eedcd.solidanchor.utils.item.getBlock
import com.github.eedcd.solidanchor.utils.math.BY_Y
import com.github.eedcd.solidanchor.utils.math.BY_Y_DESC
import com.github.eedcd.solidanchor.utils.kotlin.Priority
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.util.Mth
import net.minecraft.world.level.block.Block
import kotlin.random.Random

/**
 * BlockIn module
 *
 * Builds blocks to cover yourself.
 */
object ModuleBlockIn : ClientModule("BlockIn", ModuleCategories.WORLD, disableOnQuit = true) {

    private val blockPlacer = tree(BlockPlacer("Placer", this, Priority.NORMAL, ::slotFinder))
    private val disableOn by multiEnumChoice("DisableOn", enumSetAllOf<DisableOn>())
    private val avoidBrokenBlocks by boolean("AvoidBrokenBlocks", true)
    private val brokenBlockWindow by int("BrokenBlockWindow", 1000, 0..10000, "ms")
    private val placeOrder by enumChoice("PlaceOrder", Order.Normal)
    private val filter by enumChoice("Filter", Filter.BLACKLIST)
    private val blocks by blocks("Blocks", blockSortedSetOf())

    private enum class DisableOn(override val tag: String) : Tagged {
        FINISH("Finish"),
        MOVE("Move"),
    }

    private enum class Order(override val tag: String) : Tagged {

        Normal("Normal") {
            override fun positions(): Array<BlockPos> {
                val playerHeight = Mth.ceil(player.bbHeight)
                val result = ObjectArraySet<BlockPos>(10)
                result += startPos.below()
                rotateSurroundings {
                    val value = startPos.relative(it)
                    repeat(playerHeight) { i ->
                        result += value.above(i)
                    }
                }
                result += startPos.above(playerHeight)

                return result.toTypedArray()
            }
        },

        Random("Random") {
            override fun positions(): Array<BlockPos> {
                val array = Normal.positions()
                array.shuffle()
                return array
            }
        },

        BottomTop("BottomTop") {
            override fun positions(): Array<BlockPos> {
                val array = Normal.positions()
                array.sortWith(Vec3i.BY_Y)
                return array
            }
        },

        TopBottom("TopBottom") {
            override fun positions(): Array<BlockPos> {
                val array = Normal.positions()
                array.sortWith(Vec3i.BY_Y_DESC)
                return array
            }
        };

        abstract fun positions(): Array<BlockPos>
    }

    private val startPos = BlockPos.MutableBlockPos()
    private var rotateClockwise = false
    private var blockList = emptyList<BlockPos>()
    private var moved = false
    private val recentlyBrokenBlocks = Long2LongOpenHashMap()

    override fun onDisabled() {
        startPos.set(BlockPos.ZERO)
        blockList = emptyList()
        moved = false
        recentlyBrokenBlocks.clear()
        blockPlacer.disable()
    }

    override fun onEnabled() {
        startPos.set(player.blockPosition())
        rotateClockwise = Random.nextBoolean()
        getPositions()
    }

    private inline fun rotateSurroundings(action: (Direction) -> Unit) {
        var direction = player.direction
        repeat(4) {
            action(direction)
            // Next direction
            direction = if (rotateClockwise) {
                direction.clockWise
            } else {
                direction.counterClockWise
            }
        }
    }

    private fun getPositions() {
        val now = System.currentTimeMillis()
        recentlyBrokenBlocks.long2LongEntrySet().removeIf { it.longValue + brokenBlockWindow <= now }
        blockList = placeOrder.positions().filter {
            it.stateOrEmpty.canBeReplaced() &&
                (!avoidBrokenBlocks || !recentlyBrokenBlocks.containsKey(it.asLong()))
        }
        debugParameter("Place Count") { blockList.size }
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> {
        if (!avoidBrokenBlocks || it.origin != TransferOrigin.OUTGOING) {
            return@handler
        }

        val packet = it.packet
        if (packet is ServerboundPlayerActionPacket &&
            packet.action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK
        ) {
            val pos = packet.pos.asLong()
            mc.execute { recentlyBrokenBlocks.put(pos, System.currentTimeMillis()) }
        }
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        blockPlacer.update(blockList)
        tickUntil { blockPlacer.isDone() || moved }

        if (disableOn.contains(DisableOn.FINISH)) {
            notification(name, message("filled"), NotificationEvent.Severity.SUCCESS)
            enabled = false
        }
        getPositions()
    }

    @Suppress("unused")
    private val movementHandler = handler<PlayerMovementTickEvent> {
        val currentPos = player.blockPosition()

        if (currentPos != startPos && currentPos != startPos.above()) {
            if (disableOn.contains(DisableOn.MOVE)) {
                notification(name, message("positionChanged"), NotificationEvent.Severity.ERROR)
                enabled = false
            } else {
                moved = true
            }
        }
    }

    private val COMPARATOR: Comparator<Pair<*, Block>> =
        Comparator.comparing({ it.second }, UnbreakableFirst)

    @JvmStatic
    private fun slotFinder(pos: BlockPos?): HotbarItemSlot? {
        val blockSlots = HotbarItemSlot.entries.mapNotNull {
            it to (it.itemStack.getBlock()?.takeIf { b -> filter(b, blocks) } ?: return@mapNotNull null)
        }

        return if (pos in blockList) {
            blockSlots.minWithOrNull(COMPARATOR)
        } else {
            blockSlots.maxWithOrNull(COMPARATOR)
        }?.first
    }

}
