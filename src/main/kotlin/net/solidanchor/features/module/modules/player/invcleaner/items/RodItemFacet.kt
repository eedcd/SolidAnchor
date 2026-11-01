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
import net.solidanchor.utils.item.EnchantmentValueEstimator
import net.solidanchor.utils.item.asHolderComparator
import net.solidanchor.utils.sorting.ComparatorChain
import net.minecraft.world.item.enchantment.Enchantments

class RodItemFacet(itemSlot: ItemSlot) : ItemFacet(itemSlot) {
    companion object {
        private val VALUE_ESTIMATOR =
            EnchantmentValueEstimator(
                EnchantmentValueEstimator.WeightedEnchantment(Enchantments.UNBREAKING, 0.4f),
            )
        private val COMPARATOR =
            ComparatorChain<RodItemFacet>(
                VALUE_ESTIMATOR.asHolderComparator(),
                DEFAULT_TIE_BREAK,
            )
    }

    override val category: ItemCategory
        get() = ItemType.ROD.defaultCategory

    override fun compareTo(other: ItemFacet): Int {
        return COMPARATOR.compare(this, other as RodItemFacet)
    }
}
