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

package net.solidanchor.features.module.modules.misc.debugrecorder.modes

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.fastutil.objectHashSetOf
import net.solidanchor.event.tickHandler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.modules.misc.debugrecorder.ModuleDebugRecorder
import net.solidanchor.utils.io.toJsonArray
import net.minecraft.world.entity.Entity
import java.util.concurrent.CopyOnWriteArraySet

object GenericDebugRecorder : ModuleDebugRecorder.DebugRecorderMode<JsonObject>("Generic") {

    private data class ScheduledEntityDebug(var ticksLeft: Int, val entityId: Int)

    private val waitingEntities = CopyOnWriteArraySet<ScheduledEntityDebug>()

    fun debugEntityIn(entity: Entity, ticks: Int) {
        waitingEntities.add(ScheduledEntityDebug(ticks, entity.id))
    }

    val repeatable = tickHandler {
        val due = waitingEntities.filterTo(objectHashSetOf()) {
            it.ticksLeft--
            it.ticksLeft <= 0
        }

        for ((_, entityId) in due) {
            val entity = world.getEntity(entityId)

            if (entity != null) {
                recordDebugInfo(ModuleDebugRecorder, "entity", debugObject(entity))
            }
        }

        waitingEntities.removeAll(due)
    }

    fun recordDebugInfo(module: ClientModule, packetName: String, packet: JsonElement) {
        recordPacket(JsonObject().apply {
            addProperty("module", module.name)
            addProperty("packet", packetName)
            addProperty("time", System.currentTimeMillis())
            add("data", packet)
        })
    }

    fun debugObject(entity: Entity): JsonElement {
        return JsonObject().apply {
            addProperty("id", entity.id)
            add("pos", entity.position().toJsonArray())
            add("velocity", entity.deltaMovement.toJsonArray())
        }
    }
}
