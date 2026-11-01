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

/**
 * Specialization of weapon type. Used in order to allow the user to specify that they want a sword and not an axe
 * or something.
 */
class SwordItemFacet(itemSlot: ItemSlot) : WeaponItemFacet(itemSlot) {
    override val category: ItemCategory
        get() = ItemType.SWORD.defaultCategory
}
