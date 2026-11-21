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

package net.solidanchor.features.module.modules.player.autoqueue.actions

import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.player.autoqueue.ModuleAutoQueue
import net.solidanchor.utils.client.SilentHotbar
import net.solidanchor.utils.inventory.SingleItemStackPickMode
import net.solidanchor.utils.inventory.Slots

object AutoQueueActionUseItem : AutoQueueAction("UseItem") {

    private val mode = modes("Mode", 0) {
        arrayOf(SingleItemStackPickMode.ByName(it), SingleItemStackPickMode.ByItem(it))
    }

    override suspend fun execute() {
        val slot = Slots.OffhandWithHotbar.findSlot(mode.activeMode) ?: return

        SilentHotbar.selectSlotSilently(ModuleAutoQueue, slot, 20)
        waitTicks(1)
        interaction.useItem(player, slot.useHand)
    }

}
