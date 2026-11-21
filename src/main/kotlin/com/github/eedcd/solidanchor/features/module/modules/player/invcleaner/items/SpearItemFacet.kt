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

package net.solidanchor.features.module.modules.player.invcleaner.items

import net.solidanchor.features.module.modules.player.invcleaner.ItemCategory
import net.solidanchor.features.module.modules.player.invcleaner.ItemType
import net.solidanchor.utils.inventory.ItemSlot
import net.solidanchor.utils.item.asHolderComparator
import net.solidanchor.utils.item.attackSpeed
import net.solidanchor.utils.item.comparingEnchantmentLevel
import net.solidanchor.utils.sorting.ComparatorChain
import net.minecraft.world.item.enchantment.Enchantments

/**
 * Specialization of weapon type. Used in order to allow the user to specify that they want a sword and not an axe
 * or something.
 */
class SpearItemFacet(itemSlot: ItemSlot) : WeaponItemFacet(itemSlot) {
    companion object {
        internal val COMPARING_LUNGE_AND_SPEED = comparingEnchantmentLevel(Enchantments.LUNGE).asHolderComparator()
            .thenComparingDouble { it.itemStack.attackSpeed }

        private val COMPARATOR_FOR_PIERCING_ATTACK =
            ComparatorChain<SpearItemFacet>(
                COMPARING_LUNGE_AND_SPEED.reversed(),
                SECONDARY_VALUE_ESTIMATOR.asHolderComparator(),
                PREFER_BETTER_DURABILITY,
                PREFER_ENCHANTABLE,
                DEFAULT_TIE_BREAK,
            )
    }

    override val category: ItemCategory
        get() = ItemType.SPEAR.defaultCategory

}
