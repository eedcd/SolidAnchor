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
package net.solidanchor.features.module.modules.movement.speed.modes

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.events.PlayerAfterJumpEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.movement.speed.ModuleSpeed.doOptimizationsPreventJump
import net.solidanchor.features.module.modules.player.invcleaner.items.SpearItemFacet.Companion.COMPARING_LUNGE_AND_SPEED
import net.solidanchor.utils.client.SilentHotbar
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.withStrafe
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.item.attackSpeed
import net.solidanchor.utils.item.durability
import net.solidanchor.utils.item.getEnchantment
import net.solidanchor.utils.math.copy
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.enchantment.Enchantments
import kotlin.math.roundToInt

class SpeedSpeedYPort(parent: ModeValueGroup<*>) : SpeedBHopBase("YPort", parent) {

    private val speed by float("Speed", 0.4f, 0.1f..1f)

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (!player.onGround() && player.moving) {
            player.deltaMovement = player.deltaMovement.copy(y = -1.0)
        }
    }

    @Suppress("unused")
    private val afterJumpHandler = handler<PlayerAfterJumpEvent> {
        player.deltaMovement = player.deltaMovement.withStrafe(speed = speed.toDouble())
    }

}

class SpeedLegitHop(parent: ModeValueGroup<*>) : SpeedBHopBase("LegitHop", parent)

abstract class SpeedBHopBase(name: String, override val parent: ModeValueGroup<*>) : Mode(name) {

    @Suppress("unused")
    private val movementInputHandler = handler<MovementInputEvent> { event ->
        if (!player.onGround() || !event.directionalInput.isMoving) {
            return@handler
        }

        if (doOptimizationsPreventJump()) {
            return@handler
        }

        event.jump = true
    }

}

class SpeedPiercingAttack(parent: ModeValueGroup<*>) : SpeedBHopBase("PiercingAttack", parent) {

    private val holdTime by intRange("HoldTime", 1..1, 1..20, "ticks")
    private val onGround by boolean("OnGround", true)
    private val ignoreHunger by boolean("IgnoreHunger", false)
    private val waitForCooldown by boolean("WaitForCooldown", true)
    private val minimumDurability by int("MinimumDurability", 1, 0..20)

    /**
     * @see net.minecraft.client.Minecraft.startAttack
     * @see net.minecraft.world.entity.player.Player.getCurrentItemAttackStrengthDelay
     */
    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (!onGround && player.onGround()
            || interaction.isSpectator
            || !ignoreHunger && player.foodData.foodLevel < 6) {
            return@tickHandler
        }

        val slot = Slots.Hotbar
            .filter {
                val itemStack = it.itemStack
                itemStack[DataComponents.PIERCING_WEAPON] != null
                    && itemStack.isItemEnabled(world.enabledFeatures())
                    && !player.cannotAttackWithItem(itemStack, 0)
                    && itemStack.getEnchantment(Enchantments.LUNGE) > 0
                    && itemStack.durability >= minimumDurability
            }
            .maxWithOrNull(COMPARING_LUNGE_AND_SPEED) ?: return@tickHandler
        val piercingWeapon = slot.itemStack[DataComponents.PIERCING_WEAPON]!!

        SilentHotbar.selectSlotSilently(this, slot, ticksUntilReset = holdTime.random())
        interaction.piercingAttack(slot.itemStack.attackAnimation, piercingWeapon)

        if (waitForCooldown) {
            waitTicks((20.0 / slot.itemStack.attackSpeed).roundToInt())
        }
    }

    override fun disable() {
        SilentHotbar.resetSlot(this)
        super.disable()
    }

}
