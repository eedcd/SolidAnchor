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
package net.solidanchor.features.module.modules.player

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.handler
import net.solidanchor.event.once
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.player.ModuleAntiAFK.CustomMode.Rotate.angle
import net.solidanchor.features.module.modules.player.ModuleAntiAFK.CustomMode.Rotate.ignoreOpenInventory
import net.solidanchor.features.module.modules.player.ModuleAntiAFK.CustomMode.Rotate.rotations
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.block.SwingMode
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.kotlin.random
import net.solidanchor.utils.movement.DirectionalInput
import net.minecraft.world.InteractionHand
import kotlin.random.Random

/**
 * AntiAFK module
 *
 * Prevents you from being kicked for AFK.
 */

object ModuleAntiAFK : ClientModule("AntiAFK", ModuleCategories.PLAYER) {
    private val modes = choices(
        "Mode", RandomInteraction, arrayOf(
            OldMode, RandomInteraction, CustomMode
        )
    )

    private object OldMode : Mode("Old") {
        override val parent: ModeValueGroup<Mode>
            get() = modes

        @Suppress("unused")
        val repeatable = tickHandler {
            waitTicks(10)
            player.yRot += 180f
        }

        @Suppress("unused")
        val movementInputEvent = handler<MovementInputEvent> {
            it.directionalInput = it.directionalInput.copy(
                forwards = true
            )
        }

    }

    private object RandomInteraction : Mode("RandomInteraction") {
        override val parent: ModeValueGroup<Mode>
            get() = modes

        var randomDirection = DirectionalInput.NONE

        private val interactions by multiEnumChoice("Interaction",
            Interaction.YAW,
            Interaction.PITCH,
            Interaction.SWING_HAND,
        )

        private val delay by intRange("Delay", 4..7, 0..20, suffix = "ticks")

        @Suppress("unused")
        val repeatable = tickHandler {
            interactions.randomOrNull()?.let {
                it.perform()
                waitTicks(delay.random())
            }
        }

        @Suppress("unused")
        val movementInputEvent = handler<MovementInputEvent> {
            it.directionalInput = randomDirection
        }

        @Suppress("unused", "MagicNumber")
        private enum class Interaction(
            override val tag: String,
            val perform: suspend () -> Unit,
        ): Tagged {
            JUMP("Jump", {
                once<MovementInputEvent> { event ->
                    event.jump = true
                }
            }),
            SWING_HAND("SwingHand", {
                if (!player.isSwinging) {
                    SwingMode.DO_NOT_HIDE.swing(InteractionHand.MAIN_HAND)
                }
            }),
            CHANGE_SLOT("ChangeSlot", {
                player.inventory.selectedSlot = Random.nextInt(0, 9)
            }),
            YAW("Yaw", {
                player.yRot += (-180f..180f).random()
            }),
            PITCH("Pitch", {
                player.xRot = ((-5f..5f).random() + player.xRot).coerceIn(-90f, 90f)
            }),
            RANDOM_DIRECTION("RandomDirection", {
                randomDirection = DirectionalInput(
                    Random.nextBoolean(),
                    Random.nextBoolean(),
                    Random.nextBoolean(),
                    Random.nextBoolean()
                )
                waitTicks(delay.random())
                randomDirection = DirectionalInput.NONE
            })
        }
    }

    private object CustomMode : Mode("Custom") {
        override val parent: ModeValueGroup<Mode>
            get() = modes


        private object Rotate : ToggleableValueGroup(ModuleAntiAFK, "Rotate", true) {
            val ignoreOpenInventory by boolean("IgnoreOpenInventory", true)
            val rotations = tree(RotationsValueGroup(this))
            val delay by int("Delay", 5, 0..20, "ticks")
            val angle by float("Angle", 1f, -180f..180f)
        }

        private object Swing : ToggleableValueGroup(ModuleAntiAFK, "Swing", true) {
            val delay by int("Delay", 5, 0..20, "ticks")
        }

        init {
            tree(Rotate)
            tree(Swing)
        }

        val jump by boolean("Jump", true)
        val move by boolean("Move", true)

        @Suppress("unused")
        val swingRepeatable = tickHandler {
            if (Swing.enabled && !player.isSwinging) {
                waitTicks(Swing.delay)
                SwingMode.DO_NOT_HIDE.swing(InteractionHand.MAIN_HAND)
            }
        }

        @Suppress("unused")
        val repeatable = tickHandler {
            if (move) {
                mc.options.keyUp.isDown = true
            }

            if (jump && player.onGround()) {
                once<MovementInputEvent> { event ->
                    event.jump = true
                }
            }

            if (Rotate.enabled) {
                waitTicks(Rotate.delay)
                val currentRotation = RotationManager.serverRotation
                val pitchRandomization = Random.nextDouble(-5.0, 5.0).toFloat()
                RotationManager.setRotationTarget(
                    Rotation(
                        currentRotation.yaw + angle, (currentRotation.pitch + pitchRandomization).coerceIn(-90f, 90f)
                    ), ignoreOpenInventory, rotations, Priority.IMPORTANT_FOR_USAGE_1, ModuleAntiAFK
                )
            }

        }
    }
}
