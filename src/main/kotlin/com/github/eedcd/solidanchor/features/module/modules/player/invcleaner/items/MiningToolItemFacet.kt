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
import net.solidanchor.utils.item.isAxe
import net.solidanchor.utils.item.isHoe
import net.solidanchor.utils.item.isPickaxe
import net.solidanchor.utils.item.isShovel
import net.solidanchor.utils.item.toolComponent
import net.solidanchor.utils.sorting.ComparatorChain
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.enchantment.Enchantments

class MiningToolItemFacet(itemSlot: ItemSlot) : ItemFacet(itemSlot) {
    companion object {
        const val MASK_AXE = 1 shl 0
        const val MASK_PICKAXE = 1 shl 1
        const val MASK_SHOVEL = 1 shl 2
        const val MASK_HOE = 1 shl 3

        private val VALUE_ESTIMATOR =
            EnchantmentValueEstimator(
                EnchantmentValueEstimator.WeightedEnchantment(Enchantments.SILK_TOUCH, 1.0f),
                EnchantmentValueEstimator.WeightedEnchantment(Enchantments.UNBREAKING, 0.2f),
                EnchantmentValueEstimator.WeightedEnchantment(Enchantments.FORTUNE, 0.33f),
            )
        private val COMPARATOR =
            /**
             * @see ToolMaterial.applyToolProperties
             * @see Tool.Rule.minesAndDrops
             */
            ComparatorChain<MiningToolItemFacet>(
                compareBy {
                    val toolComponent = it.itemStack.toolComponent ?: return@compareBy 0f
                    toolComponent.rules.firstOrNull { rule ->
                        rule.correctForDrops.orElse(false)
                    }?.speed?.orElse(null) ?: toolComponent.defaultMiningSpeed
                },
                VALUE_ESTIMATOR.asHolderComparator(),
                PREFER_BETTER_DURABILITY,
                DEFAULT_TIE_BREAK,
            )

        // TODO: compare multi tool item
        private val ItemStack.miningToolType: Int
            get() {
                var bits = 0
                if (isAxe) bits = bits or MASK_AXE
                if (isPickaxe) bits = bits or MASK_PICKAXE
                if (isShovel) bits = bits or MASK_SHOVEL
                if (isHoe) bits = bits or MASK_HOE
                if (bits == 0) error("Item ${this.item} is not a mining tool")
                return bits
            }
    }

    override val category = ItemCategory(ItemType.TOOL, this.itemStack.miningToolType)

    override fun compareTo(other: ItemFacet): Int {
        return COMPARATOR.compare(this, other as MiningToolItemFacet)
    }
}
