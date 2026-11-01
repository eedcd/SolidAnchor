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
package net.solidanchor.features.module.modules.combat

import net.solidanchor.event.events.EntityMarginEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.combat.shouldBeAttacked

/**
 * Hitbox module
 *
 * Enlarges the hitbox of other entities.
 */
object ModuleHitbox : ClientModule("Hitbox", ModuleCategories.COMBAT) {

    val size by float("Size", 0.1f, 0f..1f).apply { tagBy(this) }

    val applyToDebugHitbox by boolean("ApplyToDebugHitbox", true)

    /**
     * Apply to [net.minecraft.world.item.component.AttackRange.hitboxMargin]
     */
    val applyToComponent by boolean("ApplyToComponent", true)

    @Suppress("unused")
    private val marginHandler = handler<EntityMarginEvent> { event ->
        if (event.entity.shouldBeAttacked()) {
            event.margin = size
        }
    }

}
