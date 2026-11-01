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

package net.solidanchor.features.module.modules.combat.aimbot.autobow

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.combat.aimbot.ModuleAutoBow
import net.solidanchor.utils.entity.moving
import net.solidanchor.utils.entity.usingItemOrNull
import net.solidanchor.utils.network.MovePacketType
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.BowItem

/**
 * @desc Fast charge options (like FastBow) can be used to charge the bow faster.
 * @warning Should only be used on vanilla minecraft. Most anti cheats patch these kinds of exploits
 *
 * TODO: Add version specific options
 */
object AutoBowFastChargeFeature : ToggleableValueGroup(ModuleAutoBow, "FastCharge", false) {

    private val speed by int("Speed", 20, 3..20)

    private val notInTheAir by boolean("NotInTheAir", true)
    private val notDuringMove by boolean("NotDuringMove", false)
    private val notDuringRegeneration by boolean("NotDuringRegeneration", false)

    private val packetType by enumChoice("PacketType", MovePacketType.FULL)

    @Suppress("unused")
    private val tickRepeatable = handler<GameTickEvent> {
        val currentItem = player.usingItemOrNull ?: return@handler

        // Should speed up game ticks when using bow
        if (currentItem.item is BowItem) {
            if (notInTheAir && !player.onGround()) {
                return@handler
            }

            if (notDuringMove && player.moving) {
                return@handler
            }

            if (notDuringRegeneration && player.hasEffect(MobEffects.REGENERATION)) {
                return@handler
            }

            repeat(speed) {
                if (!player.isUsingItem) {
                    return@repeat
                }

                // Speed up ticks (MC 1.8)
                network.send(packetType.generatePacket())

                // Show visual effect (not required to work - but looks better)
                player.updatingUsingItem()
            }
        }
    }
}
