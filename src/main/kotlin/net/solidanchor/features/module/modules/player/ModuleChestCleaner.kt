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

package net.solidanchor.features.module.modules.player

import net.solidanchor.event.events.ScheduleInventoryActionEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.collection.Filter
import net.solidanchor.utils.collection.itemSortedSetOf
import net.solidanchor.utils.inventory.CheckScreenHandlerTypeValueGroup
import net.solidanchor.utils.inventory.CheckScreenTitleValueGroup
import net.solidanchor.utils.inventory.InventoryAction
import net.solidanchor.utils.inventory.PlayerInventoryConstraints
import net.solidanchor.utils.inventory.findItemsInContainer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen

/**
 * ChestCleaner module
 *
 * Automatically drops unwanted items from a chest.
 *
 */
object ModuleChestCleaner : ClientModule(
    "ChestCleaner", ModuleCategories.PLAYER,
    aliases = listOf("ContainerCleaner")
) {
    private val filter by enumChoice("Filter", Filter.WHITELIST)
    private val itemsList by items("Items", itemSortedSetOf())
    private val autoClose by boolean("AutoClose", true)

    private val inventoryConstraints = tree(PlayerInventoryConstraints())
    private val checkScreenHandlerType = tree(CheckScreenHandlerTypeValueGroup(this))
    private val checkScreenTitle = tree(CheckScreenTitleValueGroup(this))

    @Suppress("unused")
    private val scheduleInventoryAction = handler<ScheduleInventoryActionEvent> { event ->
        val screen = mc.gui.screen() as? AbstractContainerScreen<*> ?: return@handler
        if (screen is InventoryScreen) return@handler
        if (!checkScreenHandlerType.isValid(screen) || !checkScreenTitle.isValid(screen)) return@handler

        val slots = screen.findItemsInContainer()
        val selectedSlots = slots.filter { !it.itemStack.isEmpty && filter(it.itemStack.item, itemsList) }

        if (selectedSlots.isEmpty()) {
            if (autoClose) {
                event.schedule(inventoryConstraints, InventoryAction.CloseScreen(screen))
            }
        } else {
            val actions = selectedSlots.map { slot -> InventoryAction.Click.performThrow(screen, slot) }
            event.schedule(inventoryConstraints, actions)
        }
    }
}
