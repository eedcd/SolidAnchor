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

package net.solidanchor.utils.combat

import net.solidanchor.event.EventListener
import net.solidanchor.event.EventManager
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.TagEntityEvent
import net.solidanchor.event.handler
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.minecraft.world.entity.Entity
import java.util.concurrent.ConcurrentHashMap

object EntityTaggingManager: EventListener {
    private val cache = ConcurrentHashMap<Entity, EntityTag>()

    @Suppress("unused")
    val tickHandler = handler<GameTickEvent>(priority = FIRST_PRIORITY) {
        cache.clear()
    }

    fun getTag(suspect: Entity): EntityTag {
        return this.cache.computeIfAbsent(suspect) {
            val targetingInfo = TagEntityEvent(it, EntityTargetingInfo.DEFAULT)

            EventManager.callEvent(targetingInfo)

            return@computeIfAbsent EntityTag(targetingInfo.targetingInfo, targetingInfo.color.value)
        }
    }

}

class EntityTag(
    val targetingInfo: EntityTargetingInfo,
    val color: Color4b?
)
