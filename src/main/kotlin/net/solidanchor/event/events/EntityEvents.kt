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

package net.solidanchor.event.events

import net.solidanchor.annotations.Tag
import net.solidanchor.event.CancellableEvent
import net.solidanchor.event.Event
import net.solidanchor.features.addon.AddonApi
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.utils.combat.EntityTargetClassification
import net.solidanchor.utils.combat.EntityTargetingInfo
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.kotlin.PriorityField
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

@AddonApi
@Tag("attack")
class AttackEntityEvent(
    val entity: Entity
) : CancellableEvent()

@Tag("entityMargin")
class EntityMarginEvent(val entity: Entity, var margin: Float) : Event()

@Tag("entityHealthUpdate")
class EntityHealthUpdateEvent(val entity: LivingEntity, val old: Float, val new: Float, val max: Float) : Event()

@AddonApi
@Tag("tagEntityEvent")
class TagEntityEvent(val entity: Entity, var targetingInfo: EntityTargetingInfo) : Event() {
    val color: PriorityField<Color4b?> = PriorityField(null, Priority.NOT_IMPORTANT)

    /**
     * Don't start combat this target
     */
    fun dontTarget() {
        if (this.targetingInfo.classification == EntityTargetClassification.TARGET) {
            this.targetingInfo = this.targetingInfo.copy(classification = EntityTargetClassification.INTERESTING)
        }
    }

    /**
     * Fully ignore that target
     */
    fun ignore() {
        this.targetingInfo = targetingInfo.copy(classification = EntityTargetClassification.IGNORED)
    }

    fun assumeFriend() {
        this.targetingInfo = targetingInfo.copy(isFriend = true)
    }

    fun color(col: Color4b, priority: Priority) {
        this.color.trySet(col, priority)
    }
}
