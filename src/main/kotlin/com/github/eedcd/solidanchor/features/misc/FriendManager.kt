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
package net.solidanchor.features.misc

import net.solidanchor.config.ConfigSystem
import net.solidanchor.config.types.Config
import net.solidanchor.config.types.ValueType
import net.solidanchor.event.EventListener
import net.solidanchor.event.events.AttackEntityEvent
import net.solidanchor.event.events.TagEntityEvent
import net.solidanchor.event.handler
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import java.util.TreeSet
import net.solidanchor.event.EventManager
import net.solidanchor.event.events.FriendChangeEvent
import net.solidanchor.features.addon.AddonApi

object FriendManager : Config("Friends"), EventListener {

    val friends by list(name, TreeSet<Friend>(), valueType = ValueType.FRIEND)

    private val cancelAttack by boolean("CancelAttack", false)

    @Suppress("unused")
    private val tagEntityEvent = handler<TagEntityEvent> {
        if (isFriend(it.entity)) {
            it.assumeFriend()
        }
    }

    @Suppress("unused")
    private val onAttack = handler<AttackEntityEvent> {
        if (cancelAttack && isFriend(it.entity)) {
            it.cancelEvent()
        }
    }

    init {
        ConfigSystem.root(this)
    }

    class Friend(val name: String, var alias: String?) : Comparable<Friend> {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Friend

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun compareTo(other: Friend): Int = this.name.compareTo(other.name)

        fun getDefaultName(id: Int): String = "Friend $id"

    }

    fun isFriend(name: String): Boolean = friends.contains(Friend(name, null))
    fun isFriend(entity: Entity): Boolean = entity is Player && isFriend(entity.gameProfile.name)

    @AddonApi
    fun add(friend: Friend): Boolean = friends.add(friend).also { added ->
        if (added) {
            EventManager.callEvent(FriendChangeEvent(friend.name, true))
        }
    }

    @AddonApi
    fun remove(name: String): Boolean = friends.remove(Friend(name, null)).also { removed ->
        if (removed) {
            EventManager.callEvent(FriendChangeEvent(name, false))
        }
    }

    @AddonApi
    fun clear() {
        val names = friends.map(Friend::name)
        friends.clear()
        names.forEach { EventManager.callEvent(FriendChangeEvent(it, false)) }
    }

}
