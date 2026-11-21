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
package com.github.eedcd.solidanchor.features.module.modules.misc

import com.github.eedcd.solidanchor.event.events.ScheduleInventoryActionEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.utils.inventory.ArmorItemSlot
import com.github.eedcd.solidanchor.utils.inventory.HotbarItemSlot
import com.github.eedcd.solidanchor.utils.inventory.InventoryAction
import com.github.eedcd.solidanchor.utils.inventory.ItemSlot
import com.github.eedcd.solidanchor.utils.inventory.PlayerInventoryConstraints
import com.github.eedcd.solidanchor.utils.inventory.Slots
import com.github.eedcd.solidanchor.utils.item.isChestArmor
import com.github.eedcd.solidanchor.utils.item.isGlider
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention

/**
 * ModuleElytraSwap
 *
 * Allows you to quickly replace your chestplate with an elytra and vice versa.
 *
 * @author sqlerrorthing
 * @since 2/13/2025
 **/
object ModuleElytraSwap : ClientModule(
    "ElytraSwap",
    ModuleCategories.PLAYER,
    aliases = listOf("ChestSwap"),
    disableOnQuit = true
) {

    private val constraints = tree(PlayerInventoryConstraints())

    private val slotsToSearch = Slots.Hotbar + Slots.Inventory + HotbarItemSlot.OFFHAND

    @Suppress("unused")
    private val scheduleInventoryActionHandler = handler<ScheduleInventoryActionEvent>(
        EventPriorityConvention.CRITICAL_MODIFICATION
    ) { event ->
        val elytraItem = slotsToSearch.findSlot { it.isGlider && !it.nextDamageWillBreak() }
        val chestplateItem = slotsToSearch.findSlot { it.isChestArmor }

        val chestplateStack = ArmorItemSlot.CHEST.itemStack
        when {
            // put on elytra
            chestplateStack.isEmpty && elytraItem != null -> event.doSwap(elytraItem)

            // replacing of elytra with a chestplate
            chestplateStack.isGlider && chestplateItem != null -> event.doSwap(chestplateItem)

            // replacing the chestplate with elytra
            chestplateStack.isChestArmor && elytraItem != null -> event.doSwap(elytraItem)
        }

        enabled = false
    }

    private fun ScheduleInventoryActionEvent.doSwap(slot: ItemSlot) {
        var exchange: InventoryAction? = null
        if (!ArmorItemSlot.CHEST.itemStack.isEmpty) {
            exchange = InventoryAction.Click.performPickup(slot = slot)
        }

        val actions = listOfNotNull(
            InventoryAction.Click.performPickup(slot = slot),
            InventoryAction.Click.performPickup(slot = ArmorItemSlot.CHEST),
            exchange
        )

        schedule(constraints, actions)
    }

}
