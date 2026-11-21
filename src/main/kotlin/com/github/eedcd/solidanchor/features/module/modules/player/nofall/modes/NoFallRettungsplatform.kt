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
package net.solidanchor.features.module.modules.player.nofall.modes

import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.player.nofall.ModuleNoFall
import net.solidanchor.features.module.modules.render.ModuleDebug.debugParameter
import net.solidanchor.utils.block.getBlock
import net.solidanchor.utils.block.state
import net.solidanchor.utils.entity.FallingPlayer
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.inventory.findClosestSlot
import net.solidanchor.utils.inventory.useHotbarSlotOrOffhand
import net.minecraft.world.item.Items

/**
 * Uses an item called Rettungsplatform or Rettungskapsel to prevent fall damage.
 * This is an item of the game-mode BedWars on the server GommeHD.net
 *
 * https://www.gommehd.net/
 *
 * As such module is mostly used by German players, the name of the module is in German.
 * That is unusual for SolidAnchor, but it is the best name for this module.
 */
internal object NoFallRettungsplatform : NoFallMode("Rettungsplatform") {

    /**
     * The item used to create a platform.
     * This is either a blaze rod or a magma cream.
     * We are not checking for the item name, as there are different language options causing issues.
     */
    private val itemToPlatform
        get() = Slots.OffhandWithHotbar.findClosestSlot(Items.BLAZE_ROD, Items.MAGMA_CREAM)

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (player.fallDistance > 2f) {
            val itemToPlatform = itemToPlatform ?: return@tickHandler

            // Are we actually going to fall into the void?
            // todo: check if the fall damage is actually high enough to kill us
            val collision = FallingPlayer.fromPlayer(player).findCollision(90)?.pos
            ModuleNoFall.debugParameter("Collision") {
                collision?.getBlock()
            }
            if (collision != null && collision.state?.isAir == false) {
                return@tickHandler
            }

            useHotbarSlotOrOffhand(itemToPlatform)

            // Wait 5 seconds
            waitTicks(20 * 5)
        }
    }

}
