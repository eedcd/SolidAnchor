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
package net.solidanchor.features.module.modules.movement.elytrafly.modes

import net.solidanchor.additions.shooter
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.ScheduleInventoryActionEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.solidanchor.utils.inventory.InventoryAction
import net.solidanchor.utils.inventory.PlayerInventoryConstraints
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.inventory.useHotbarSlotOrOffhand
import net.solidanchor.utils.world.entityGetter
import net.solidanchor.utils.world.none
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.Items

internal object ElytraFlyModeFirework : ElytraFlyMode("Firework") {

    private object ConsiderInventory : ToggleableValueGroup(this, "ConsiderInventory", enabled = false) {
        val constraints = tree(PlayerInventoryConstraints())
    }

    init {
        tree(ConsiderInventory)
    }

    private val cooldown by intRange("Cooldown", 20..20, 0..300, "ticks")

    private val ALL_WITHOUT_ARMOR = Slots.OffhandWithHotbar + Slots.Inventory
    private val slotsToSearch get() = if (ConsiderInventory.enabled) ALL_WITHOUT_ARMOR else Slots.OffhandWithHotbar

    private fun shouldUseFirework(): Boolean {
        return if (!player.isFallFlying || player.isUsingItem) {
            false
        } else {
            world.entityGetter.none(EntityTypes.FIREWORK_ROCKET) { it.shooter === player }
        }
    }

    private var skipTicks = 0

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        skipTicks--
    }

    @Suppress("unused")
    private val scheduleInventoryActionHandler = handler<ScheduleInventoryActionEvent> { event ->
        if (skipTicks > 0 || !shouldUseFirework()) return@handler

        val fireworkSlot = slotsToSearch.findSlot(Items.FIREWORK_ROCKET) ?: return@handler
        if (fireworkSlot is HotbarItemSlot) {
            useHotbarSlotOrOffhand(fireworkSlot)
        } else {
            val targetSlot = if (HotbarItemSlot.OFFHAND.canBeSwapTarget) {
                HotbarItemSlot.OFFHAND
            } else {
                HotbarItemSlot(player.inventory.selectedSlot)
            }

            val actions = listOf<InventoryAction>(
                InventoryAction.Click.performSwap(from = fireworkSlot, to = targetSlot),
                InventoryAction.UseItem(targetSlot, this),
                InventoryAction.Click.performSwap(from = fireworkSlot, to = targetSlot),
            )
            event.schedule(ConsiderInventory.constraints, actions)
        }

        skipTicks = cooldown.random()
    }
}
