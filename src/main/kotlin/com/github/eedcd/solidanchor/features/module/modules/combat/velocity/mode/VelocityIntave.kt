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
package net.solidanchor.features.module.modules.combat.velocity.mode

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.EventListener
import net.solidanchor.event.events.AttackEntityEvent
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.math.multiply
import net.solidanchor.utils.network.LocalPlayerFallDamageTracker
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import kotlin.random.Random

object VelocityIntave : VelocityMode("Intave") {

    private class ReduceOnAttack(parent: EventListener?) : ToggleableValueGroup(
        parent, "ReduceOnAttack",
        true
    ) {
        private val reduceFactor by float("Factor", 0.6f, 0.6f..1f)
        private val hurtTime by intRange("HurtTime", 5..7, 1..10)
        private val lastAttackTimeToReduce by int("LastAttackTimeToReduce", 2000, 1..10000)
        var lastAttackTime = 0L

        @Suppress("unused")
        private val attackHandler = handler<AttackEntityEvent> { event ->
            if (player.hurtTime in hurtTime && System.currentTimeMillis() - lastAttackTime <= lastAttackTimeToReduce) {
                player.deltaMovement = player.deltaMovement.multiply(
                    factorX = reduceFactor,
                    factorZ = reduceFactor,
                )
            }
            lastAttackTime = System.currentTimeMillis()
        }
    }

    init {
        tree(ReduceOnAttack(this))
    }

    private class JumpReset(parent: EventListener?) : ToggleableValueGroup(
        parent, "JumpReset",
        true
    ) {

        private val chance by float("Chance", 50f, 0f..100f, "%")

        private inner class Randomize : ToggleableValueGroup(this, "Randomize", false) {
            val delayTicks by intRange("DelayTicks", 0..5, 0..10)
        }

        private val randomize = tree(Randomize())
        private var currentDelay = 0
        private var delayCounter = 0

        @Suppress("unused")
        private val tickJumpHandler = handler<MovementInputEvent> {
            val shouldJump = Random.nextInt(100) < chance
                && player.hurtTime > 5
                && !LocalPlayerFallDamageTracker.isCurrentFallDamage
            val canJump = player.onGround() && mc.gui.screen() !is InventoryScreen
            val shouldFinallyJump = shouldJump && canJump

            if (randomize.enabled) {
                delayCounter++

                if (delayCounter >= currentDelay) {
                    if (shouldFinallyJump) it.jump = true
                    delayCounter = 0
                    currentDelay = randomize.delayTicks.random()
                }
            } else {
                if (shouldFinallyJump) it.jump = true
            }
        }

    }

    init {
        tree(JumpReset(this))
    }
}
