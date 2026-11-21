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
package net.solidanchor.features.module.modules.movement

import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.item.isGlider
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot

/**
 * Elytra recast module
 *
 * Recasts elytra when holding the jump key
 *
 * @author Pivo1lovv
 */
object ModuleElytraRecast : ClientModule("ElytraRecast", ModuleCategories.MOVEMENT) {

    private val shouldRecast: Boolean
        get() {
            val itemStack = player.getItemBySlot(EquipmentSlot.CHEST)

            return !player.abilities.flying && !player.isPassenger && !player.onClimbable() &&
                !player.isInWater && !player.hasEffect(MobEffects.LEVITATION) && !player.isFallFlying &&
                itemStack.isGlider && !itemStack.nextDamageWillBreak() && mc.options.keyJump.isDown
        }

    /**
     * Recast elytra when [shouldRecast] says it should
     *
     * @return true if elytra was recast
     */
    fun recastElytra(): Boolean {
        if (shouldRecast) {
            player.startFallFlying()
            network.send(
                ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING)
            )
            return true
        }

        return false
    }

}
