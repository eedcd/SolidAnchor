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
package net.solidanchor.features.module.modules.world.packetmine.tool

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.module.MinecraftShortcuts
import net.solidanchor.features.module.modules.world.ModuleAutoTool
import net.solidanchor.features.module.modules.world.packetmine.MineTarget
import net.solidanchor.features.module.modules.world.packetmine.ModulePacketMine
import net.solidanchor.utils.client.SilentHotbar
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.markAsError
import net.solidanchor.utils.client.usesViaFabricPlus
import net.solidanchor.utils.client.warning
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.solidanchor.utils.inventory.InventoryAction
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.network.PickFromInventoryPacket
import net.solidanchor.utils.network.sendPacket

enum class SwitchMethod(override val tag: String, val shouldSync: Boolean) : Tagged, MinecraftShortcuts {

    NORMAL("Normal", true) {

        override fun switch(slot: HotbarItemSlot, mineTarget: MineTarget) {
            if (ModuleAutoTool.running) {
                ModuleAutoTool.switchToBreakBlock(mineTarget.targetPos)
                return
            }

            SilentHotbar.selectSlotSilently(ModulePacketMine, slot.inventorySlot, 1)
        }

        override fun switchBack() {
            // nothing, handled by SilentHotbar
        }

    },

    @Suppress("unused")
    SWAP("Swap", false) {

        override fun switch(slot: HotbarItemSlot, mineTarget: MineTarget) {
            val selectedSlot = SilentHotbar.serversideSlot
            val desiredSlot = slot.inventorySlot
            if (selectedSlot == desiredSlot) {
                return
            }

            exchanged = desiredSlot
            InventoryAction.Click.performSwap(
                from = Slots.Hotbar[desiredSlot],
                to = Slots.Hotbar[selectedSlot]
            ).performAction()
        }

        override fun switchBack() {
            val desiredSlot = exchanged ?: return
            val selectedSlot = SilentHotbar.serversideSlot
            exchanged = null
            InventoryAction.Click.performSwap(
                from = Slots.Hotbar[desiredSlot],
                to = Slots.Hotbar[selectedSlot]
            ).performAction()
        }

    },

    /**
     * Only works before 1.21.3.
     */
    @Suppress("unused")
    PICK("Pick", false) {

        override fun switch(slot: HotbarItemSlot, mineTarget: MineTarget) {
            if (!usesViaFabricPlus) {
                chat(warning(ModulePacketMine.message("noVfp")))
                ModulePacketMine.onDisabled()
                return
            }

            exchanged = slot.hotbarIndex ?: return
            network.sendPacket(
                PickFromInventoryPacket(slot.hotbarIndex),
                onFailure = {
                    chat(
                        markAsError(
                            "Failed to pick an item from your inventory using ViaFabricPlus, report to developers!"
                        )
                    )
                    exchanged = null
                }
            )
        }

        override fun switchBack() {
            if (!usesViaFabricPlus) {
                return
            }

            // TODO make it not mess up the hotbar
            exchanged?.let { network.sendPacket(PickFromInventoryPacket(it)) }
            exchanged = null
        }

    };

    var exchanged: Int? = null

    abstract fun switch(slot: HotbarItemSlot, mineTarget: MineTarget)

    abstract fun switchBack()

    fun reset() {
        exchanged = null
    }

}
