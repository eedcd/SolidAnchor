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
package net.solidanchor.features.module.modules.movement.speed

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.RotationUpdateEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.kotlin.Priority

/**
 * Makes you go faster by strategically strafing
 */
object SpeedYawOffset : ToggleableValueGroup(ModuleSpeed, "YawOffset", false) {

    private val yawOffsetMode by enumChoice("YawOffsetMode", YawOffsetMode.AIR)
    private val rotations = RotationsValueGroup(this)

    private var yaw = 0f

    @Suppress("unused")
    private val yawOffsetHandler = handler<RotationUpdateEvent> {
        when (yawOffsetMode) {
            YawOffsetMode.GROUND -> groundYawOffset() // makes you strafe more on ground
            YawOffsetMode.AIR -> airYawOffset() // 45deg strafe on air
            YawOffsetMode.CONSTANT -> constantYawOffset()
        }

        val rotation = Rotation(player.yRot - yaw, player.xRot)

        RotationManager.setRotationTarget(
            rotations.toRotationTarget(rotation),
            Priority.NOT_IMPORTANT,
            ModuleSpeed
        )
    }

    private fun groundYawOffset(): Float {
        yaw = if (player.onGround()) {
            when {
                mc.options.keyUp.isDown && mc.options.keyLeft.isDown -> 45f
                mc.options.keyUp.isDown && mc.options.keyRight.isDown -> -45f
                mc.options.keyDown.isDown && mc.options.keyLeft.isDown -> 135f
                mc.options.keyDown.isDown && mc.options.keyRight.isDown -> -135f
                mc.options.keyDown.isDown -> 180f
                mc.options.keyLeft.isDown -> 90f
                mc.options.keyRight.isDown -> -90f
                else -> 0f
            }
        } else {
            0f
        }
        return 0f
    }

    private fun constantYawOffset(): Float {
        yaw = when {
            mc.options.keyUp.isDown && mc.options.keyLeft.isDown -> 45f
            mc.options.keyUp.isDown && mc.options.keyRight.isDown -> -45f
            mc.options.keyDown.isDown && mc.options.keyLeft.isDown -> 135f
            mc.options.keyDown.isDown && mc.options.keyRight.isDown -> -135f
            mc.options.keyDown.isDown -> 180f
            mc.options.keyLeft.isDown -> 90f
            mc.options.keyRight.isDown -> -90f
            else -> 0f
        }

        return 0f
    }

    private fun airYawOffset(): Float {
        yaw = when {
            !player.onGround() &&
                mc.options.keyUp.isDown &&
                !mc.options.keyLeft.isDown &&
                !mc.options.keyRight.isDown
                -> -45f

            else -> 0f
        }
        return 0f
    }

    private enum class YawOffsetMode(override val tag: String) : Tagged {
        GROUND("Ground"),
        AIR("Air"),
        CONSTANT("Constant")
    }

}
