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
import net.solidanchor.event.events.AttackEntityEvent
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.TargetChangeEvent
import net.solidanchor.event.handler
import net.solidanchor.features.addon.AddonApi
import net.solidanchor.features.module.modules.combat.killaura.ModuleKillAura
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

/**
 * A rotation manager
 */
object CombatManager : EventListener {

    // useful for something like autoSoup
    private var pauseCombat: Int = 0

    // useful for something like autopot
    private var pauseRotation: Int = 0

    // useful for autoblock
    private var pauseBlocking: Int = 0

    const val PAUSE_COMBAT = 40 // 40 ticks = 2 seconds
    var duringCombat: Int = 0

    private fun updatePauseRotation() {
        if (pauseRotation <= 0) return

        pauseRotation--
    }

    private fun updatePauseCombat() {
        if (pauseCombat <= 0) return

        pauseCombat--
    }

    private fun updatePauseBlocking() {
        if (pauseBlocking <= 0) return

        pauseBlocking--
    }

    private fun updateDuringCombat() {
        if (duringCombat <= 0) return

        duringCombat--
    }

    /**
     * Update current rotation to new rotation step
     */
    fun update() {
        updatePauseRotation()
        updatePauseCombat()
        // TODO: implement this for killaura autoblock and other
        updatePauseBlocking()
        updateDuringCombat()
    }

    val tickHandler = handler<GameTickEvent> {
        update()
    }

    @Suppress("unused")
    val attackHandler = handler<AttackEntityEvent> { event ->
        val entity = event.entity

        if (entity is LivingEntity && entity.shouldBeAttacked()) {
            duringCombat = PAUSE_COMBAT

            if (entity is Player) {
                EventManager.callEvent(TargetChangeEvent(entity))
            }
        }
    }

    val shouldPauseCombat: Boolean
        get() = pauseCombat > 0
    val shouldPauseRotation: Boolean
        get() = pauseRotation > 0
    val shouldPauseBlocking: Boolean
        get() = pauseBlocking > 0
    @AddonApi
    val isInCombat: Boolean
        get() = this.duringCombat > 0 ||
            (ModuleKillAura.running && ModuleKillAura.targetTracker.target != null)

    @AddonApi
    fun pauseCombatForAtLeast(pauseTime: Int) {
        pauseCombat = pauseCombat.coerceAtLeast(pauseTime)
    }

    @AddonApi
    fun pauseRotationForAtLeast(pauseTime: Int) {
        pauseRotation = pauseRotation.coerceAtLeast(pauseTime)
    }

    fun pauseBlockingForAtLeast(pauseTime: Int) {
        pauseBlocking = pauseBlocking.coerceAtLeast(pauseTime)
    }

}
