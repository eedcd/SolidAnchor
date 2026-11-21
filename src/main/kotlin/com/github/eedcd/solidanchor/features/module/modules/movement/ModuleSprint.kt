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
package com.github.eedcd.solidanchor.features.module.modules.movement

import com.github.eedcd.solidanchor.config.types.list.Tagged
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.PlayerJumpEvent
import com.github.eedcd.solidanchor.event.events.SprintEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.features.ScaffoldSprintControlFeature
import com.github.eedcd.solidanchor.utils.aiming.RotationManager
import com.github.eedcd.solidanchor.utils.aiming.RotationsValueGroup
import com.github.eedcd.solidanchor.utils.aiming.data.Rotation
import com.github.eedcd.solidanchor.utils.aiming.features.MovementCorrection
import com.github.eedcd.solidanchor.utils.math.fastCos
import com.github.eedcd.solidanchor.utils.math.fastSin
import com.github.eedcd.solidanchor.utils.math.toRadians
import com.github.eedcd.solidanchor.utils.entity.getMovementDirectionOfInput
import com.github.eedcd.solidanchor.utils.entity.isSlowDueToUsingItem
import com.github.eedcd.solidanchor.utils.entity.movementForward
import com.github.eedcd.solidanchor.utils.entity.movementSideways
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention.CRITICAL_MODIFICATION
import com.github.eedcd.solidanchor.utils.kotlin.Priority

/**
 * Sprint module
 *
 * Sprints automatically.
 */

object ModuleSprint : ClientModule("Sprint", ModuleCategories.MOVEMENT) {

    private enum class SprintMode(override val tag: String) : Tagged {
        LEGIT("Legit"),
        OMNIDIRECTIONAL("Omnidirectional"),
        OMNIROTATIONAL("Omnirotational"),
    }

    private val sprintMode by enumChoice("Mode", SprintMode.LEGIT)

    private val ignore by multiEnumChoice<Ignore>("Ignore")

    /**
     * This is used to stop sprinting when the player is not moving forward
     * without a velocity fix enabled.
     */
    private val stopOn by multiEnumChoice("StopOn", StopOn.entries)

    val shouldSprintOmnidirectional: Boolean
        get() = running && sprintMode == SprintMode.OMNIDIRECTIONAL ||
            ScaffoldSprintControlFeature.allowOmnidirectionalSprint

    val shouldIgnoreBlindness
        get() = running && Ignore.BLINDNESS in ignore

    val shouldIgnoreHunger
        get() = running && Ignore.HUNGER in ignore

    val shouldIgnoreCollision
        get() = running && Ignore.COLLISION in ignore

    @Suppress("unused")
    private val sprintHandler = handler<SprintEvent>(priority = CRITICAL_MODIFICATION) { event ->
        if (!event.directionalInput.isMoving) {
            return@handler
        }

        if (event.source == SprintEvent.Source.MOVEMENT_TICK || event.source == SprintEvent.Source.INPUT) {
            event.sprint = true
        }
    }

    @Suppress("unused")
    private val sprintPreventionHandler = handler<SprintEvent> { event ->
        // In this case we want to prevent sprinting on movement tick only,
        // because otherwise you could guess from the input change that this is automated.
        if (event.source == SprintEvent.Source.MOVEMENT_TICK && shouldPreventSprint()) {
            event.sprint = false
        }
    }

    @Suppress("unused")
    private val jumpHandler = handler<PlayerJumpEvent> { event ->
        if (sprintMode == SprintMode.OMNIDIRECTIONAL && shouldSprintOmnidirectional) {
            // Allows us to sprint boost in every direction
            event.yaw = player.getMovementDirectionOfInput()
        }
    }

    // DO NOT USE TREE TO MAKE SURE THAT THE ROTATIONS ARE NOT CHANGED
    private val rotations = RotationsValueGroup(this)

    @Suppress("unused")
    private val omniRotationalHandler = handler<GameTickEvent> {
        // Check if omnirotational sprint is enabled
        if (sprintMode != SprintMode.OMNIROTATIONAL) {
            return@handler
        }

        val yaw = player.getMovementDirectionOfInput()

        // todo: unhook pitch - AimPlan needs support for only yaw or pitch operation
        val rotation = Rotation(yaw, player.xRot)

        RotationManager.setRotationTarget(rotations.toRotationTarget(rotation), Priority.NOT_IMPORTANT,
            this@ModuleSprint)
    }

    private fun shouldPreventSprint(): Boolean {
        if (StopOn.USING_ITEM in stopOn && player.isSlowDueToUsingItem ||
            StopOn.SNEAKING in stopOn && player.isShiftKeyDown) {
            return true
        }

        val deltaYawRad = (player.yRot - (RotationManager.currentRotation ?: return false).yaw).toRadians()
        val forward = player.input.movementForward
        val sideways = player.input.movementSideways

        val hasForwardMovement = forward * deltaYawRad.fastCos() + sideways * deltaYawRad.fastSin() > 1.0E-5

        return (if (player.onGround()) StopOn.GROUND in stopOn else StopOn.AIR in stopOn)
            && !shouldSprintOmnidirectional
            && RotationManager.activeRotationTarget?.movementCorrection == MovementCorrection.OFF
            && !hasForwardMovement
    }

    private enum class Ignore(override val tag: String) : Tagged {
        BLINDNESS("Blindness"),
        HUNGER("Hunger"),
        COLLISION("Collision"),
    }

    private enum class StopOn(override val tag: String) : Tagged {
        GROUND("Ground"),
        AIR("Air"),
        SNEAKING("Sneaking"),
        USING_ITEM("UsingItem"),
    }
}
