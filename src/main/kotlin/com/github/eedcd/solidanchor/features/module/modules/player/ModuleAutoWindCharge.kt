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

import com.mojang.blaze3d.platform.InputConstants
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.tickConditional
import net.solidanchor.event.tickHandler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.player.ModuleAutoWindCharge.Rotate.rotations
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.combat.CombatManager
import net.solidanchor.utils.entity.FallingPlayer
import net.solidanchor.utils.entity.getMovementDirectionOfInput
import net.solidanchor.utils.input.isPressed
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.inventory.useHotbarSlotOrOffhand
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.kotlin.random
import net.minecraft.world.item.Items

/**
 * Uses wind charges to boost yourself up when holding jump.
 */
object ModuleAutoWindCharge : ClientModule("AutoWindCharge", ModuleCategories.PLAYER) {

    private object Rotate : ToggleableValueGroup(this, "Rotate", true) {
        val rotations = tree(RotationsValueGroup(this))
    }

    private object HorizontalBoost : ToggleableValueGroup(this, "HorizontalBoost", true) {
        val pitch by floatRange("Pitch", 70f..70f, 0f..90f)
        val boostKey by key("Key", InputConstants.KEY_LCONTROL)
    }

    init {
        treeAll(HorizontalBoost, Rotate)
    }

    private val combatPauseTime by int("CombatPauseTime", 0, 0..40, "ticks")
    private val slotResetDelay by intRange("SlotResetDelay", 0..0, 0..40, "ticks")

    /**
     * 7 ticks is the perfect time to use a wind charge before hitting the ground,
     * and drastically boosts us higher.
     */
    private const val PREDICTION_TICKS = 7

    @Suppress("unused")
    private val autoWindChargeHandler = tickHandler {
        if (player.isFallFlying || player.isSwimming || player.isInLiquid || !mc.options.keyJump.isDown) {
            return@tickHandler
        }

        val collision = FallingPlayer
            .fromPlayer(player)
            .findCollision(PREDICTION_TICKS) ?: return@tickHandler

        val itemSlot = Slots.OffhandWithHotbar.findSlot(Items.WIND_CHARGE) ?: return@tickHandler

        val isHorizontalBoost = HorizontalBoost.enabled && HorizontalBoost.boostKey.isPressed
        val directionYaw = player.getMovementDirectionOfInput() - 180f
        val directionPitch = when {
            isHorizontalBoost -> HorizontalBoost.pitch.random()
            else -> 90f
        }

        val rotation = Rotation(directionYaw, directionPitch)

        if (Rotate.enabled) {
            fun isRotationSufficient(): Boolean {
                return RotationManager.serverRotation.directionAngleTo(rotation) <= 1.0f
            }

            tickConditional(20) {
                CombatManager.pauseCombatForAtLeast(combatPauseTime)
                RotationManager.setRotationTarget(
                    rotations.toRotationTarget(rotation),
                    Priority.IMPORTANT_FOR_USAGE_3,
                    this@ModuleAutoWindCharge
                )

                isRotationSufficient()
            }

            if (!isRotationSufficient()) {
                return@tickHandler
            }
        }

        val (yaw, pitch) = rotation.normalize()
        useHotbarSlotOrOffhand(itemSlot, slotResetDelay.random(), yaw, pitch)
    }

}
