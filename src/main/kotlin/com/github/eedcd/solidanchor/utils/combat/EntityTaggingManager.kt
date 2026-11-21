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

package com.github.eedcd.solidanchor.utils.combat

import com.github.eedcd.solidanchor.event.EventListener
import com.github.eedcd.solidanchor.event.EventManager
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.TagEntityEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.render.engine.type.Color4b
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
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
