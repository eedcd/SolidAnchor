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

import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.range.RangeValueGroup

/**
 * Reach module
 *
 * Increases your reach.
 *
 * @see net.solidanchor.injection.mixins.minecraft.entity.MixinPlayer
 * @see net.solidanchor.injection.mixins.minecraft.item.MixinAttackRange
 */
object ModuleReach : ClientModule("Reach", ModuleCategories.PLAYER) {
    val entity = tree(RangeValueGroup("Entity", 1f, 0f))
    val blockRangeIncrease by float("BlockRangeIncrease", 0.5f, 0f..64f)
}
