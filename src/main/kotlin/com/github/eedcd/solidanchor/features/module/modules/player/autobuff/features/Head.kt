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

package net.solidanchor.features.module.modules.player.autobuff.features

import net.solidanchor.features.module.modules.player.autobuff.HealthBasedBuff
import net.solidanchor.utils.client.Chronometer
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.solidanchor.utils.inventory.useHotbarSlotOrOffhand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

internal object Head : HealthBasedBuff("Head") {

    private val maxAbsorption by float("MaxAbsorption", 1f, 0f..8f)
    private val cooldown by float("Cooldown", 0f, 0f..120f, "s")
    private val chronometer = Chronometer()

    override val passesRequirements: Boolean
        get() = super.passesRequirements
            && passesHealthRequirements
            && chronometer.hasElapsed((cooldown * 1000).toLong())
            && player.absorptionAmount <= maxAbsorption

    override fun isValidItem(stack: ItemStack, forUse: Boolean): Boolean {
        return stack.`is`(Items.PLAYER_HEAD)
    }

    override suspend fun execute(slot: HotbarItemSlot) {
        useHotbarSlotOrOffhand(slot)
        chronometer.reset()
    }

}
