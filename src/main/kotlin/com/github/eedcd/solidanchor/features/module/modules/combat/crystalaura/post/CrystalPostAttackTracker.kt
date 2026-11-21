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
package net.solidanchor.features.module.modules.combat.crystalaura.post

import it.unimi.dsi.fastutil.ints.Int2LongLinkedOpenHashMap
import net.ccbluex.fastutil.forEachInt
import net.ccbluex.fastutil.synchronized
import net.solidanchor.event.EventListener
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.WorldChangeEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket
import net.minecraft.sounds.SoundEvents

/**
 * Can be implemented to handle actions after crystals got attacked.
 */
abstract class CrystalPostAttackTracker : EventListener {

    protected val attackedIds = Int2LongLinkedOpenHashMap().synchronized()

    @Suppress("unused")
    private val repeatable = handler<GameTickEvent>(FIRST_PRIORITY) {
        val currentTime = System.currentTimeMillis()
        val attackTime = currentTime - timeOutAfter()
        attackedIds.int2LongEntrySet().removeIf { entry ->
            if (entry.longValue < attackTime) {
                timedOut(entry.intKey)
                true
            } else {
                false
            }
        }
    }

    @Suppress("unused")
    private val worldChangeHandler = handler<WorldChangeEvent> {
        attackedIds.clear()
        cleared()
    }

    @Suppress("unused")
    private val explodeListener = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is ClientboundSoundEntityPacket -> {
                if (packet.sound != SoundEvents.GENERIC_EXPLODE) {
                    return@handler
                }

                val id = packet.id
                // Default (non-existing) value = 0
                if (attackedIds.remove(id) != 0L) {
                    confirmed(id)
                }
            }

            is ClientboundRemoveEntitiesPacket -> {
                packet.entityIds.forEachInt { id ->
                    if (attackedIds.remove(id) != 0L) {
                        confirmed(id)
                    }
                }
            }
        }
    }

    fun onToggle() {
        attackedIds.clear()
        cleared()
    }

    /**
     * Gets called when we are sure the crystal got destroyed.
     *
     * @param id The id of the attacked entity.
     */
    open fun confirmed(id: Int) {}

    /**
     * Gets called when the crystal was not confirmed in the time defined by [timeOutAfter] in ms.
     *
     * @param id The id of the attacked entity.
     */
    open fun timedOut(id: Int) {}

    /**
     * Gets called when the attacked id list gets cleared.
     */
    open fun cleared() {}

    /**
     * Show be called when the crystal aura attacks.
     *
     * @param id The id of the attacked entity.
     */
    open fun attacked(id: Int) {
        if (!running) {
            return
        }

        attackedIds.put(id, System.currentTimeMillis())
    }

    /**
     * After how many ms attacks are not tracked anymore.
     */
    open fun timeOutAfter(): Long = 5000L // 5 seconds

}
