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
package net.solidanchor.features.module.modules.player.autoshop.serializable.conditions

import com.google.gson.JsonDeserializer
import com.google.gson.JsonParseException

data class ItemConditionNode(
    val id: String,
    val min: Int = 1,
    val max: Int = Int.MAX_VALUE
) : ConditionNode {
    companion object {
        @JvmField
        val Deserializer = JsonDeserializer<ItemConditionNode> { json, _, _ ->
            if (json == null || !json.isJsonObject) {
                throw JsonParseException("Invalid JSON: Expected a JsonObject")
            }

            val jsonObject = json.asJsonObject

            if (!jsonObject.has("id")) {
                throw JsonParseException("Invalid JSON: Missing 'id' property")
            }
            val id = jsonObject["id"].asString
            val min = jsonObject["min"]?.asInt ?: 1
            val max = jsonObject["max"]?.asInt ?: Int.MAX_VALUE

            ItemConditionNode(id, min, max)
        }
    }
}
