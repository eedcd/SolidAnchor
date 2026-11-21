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

package net.solidanchor.features.module.modules.combat.elytratarget

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.utils.client.Chronometer
import net.solidanchor.utils.network.sendHeldItemChange
import net.solidanchor.utils.entity.squaredBoxedDistanceTo
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.inventory.useHotbarSlotOrOffhand
import net.solidanchor.utils.math.sq
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.world.item.Items
import net.solidanchor.features.module.modules.combat.killaura.ModuleKillAura as KillAura

internal object AutoFirework : ToggleableValueGroup(ModuleElytraTarget, "AutoFirework", true) {
    private val useMode by enumChoice("UseMode", FireworkUseMode.NORMAL)
    private val extraDistance by float("ExtraDistance", 50f, 5f..100f, suffix = "m")
    private val slotResetDelay by intRange("SlotResetDelay", 0..0, 0..20, "ticks")
    private val syncCooldownWithKillAura by boolean("SyncCooldownWithKillAura", false)
    private val cooldown by intRange("Cooldown", 8..10, 1..50, "ticks")

    override val running: Boolean
        get() = super.running && ModuleElytraTarget.target != null

    private const val MILLISECONDS_PER_TICK = 50

    /**
     * Initial firework cooldown
     */
    private var fireworkCooldown = 750

    private val fireworkChronometer = Chronometer()

    private val cooldownReached: Boolean
        get() = fireworkChronometer.hasElapsed((fireworkCooldown * MILLISECONDS_PER_TICK).toLong())

    @Suppress("ComplexCondition")
    private suspend fun canUseFirework(): Boolean {
        if (!KillAura.running
            || !syncCooldownWithKillAura
            || (
                KillAura.clicker.isClickTick
                && KillAura.targetTracker.target
                    ?.squaredBoxedDistanceTo(player)
                    ?.takeIf { it >= KillAura.range.interactionRange.sq() } != null
                )
        ) {
            return true
        }

        /*
         * The Killaura is ready to perform the click.
         * We can use the firework on the next tick.
         * After killaura performed the click
         */
        return if (KillAura.clicker.isClickTick) {
            waitTicks(1)
            true
        } else {
            false
        }
    }

    @Suppress("unused")
    private val autoFireworkHandler = tickHandler {
        val target = ModuleElytraTarget.target ?: return@tickHandler

        if (cooldownReached && canUseFirework()) {
            Slots.OffhandWithHotbar.findSlot(Items.FIREWORK_ROCKET)?.let {
                useMode.useFireworkSlot(it, slotResetDelay.random())
                fireworkChronometer.reset()
            }
        }

        fireworkCooldown = if (target.squaredBoxedDistanceTo(player) > extraDistance.sq()) {
            cooldown.last
        } else {
            cooldown.first
        }
    }


    @Suppress("unused")
    private enum class FireworkUseMode(override val tag: String) : Tagged {
        NORMAL("Normal") {
            override fun useFireworkSlot(slot: HotbarItemSlot, resetDelay: Int) {
                useHotbarSlotOrOffhand(slot, resetDelay)
            }
        },
        PACKET("Packet") {
            override fun useFireworkSlot(slot: HotbarItemSlot, resetDelay: Int) {
                val curSlot = player.inventory.selectedSlot
                val hotbarIndex = slot.hotbarIndex
                val slotUpdateFlag = hotbarIndex != null && hotbarIndex != curSlot

                if (slotUpdateFlag) {
                    player.inventory.selectedSlot = hotbarIndex!!
                    network.sendHeldItemChange(hotbarIndex)
                }

                interaction.startPrediction(world) { sequence ->
                    ServerboundUseItemPacket(slot.useHand, sequence, player.yRot, player.xRot)
                }

                if (slotUpdateFlag) {
                    player.inventory.selectedSlot = curSlot
                    network.sendHeldItemChange(curSlot)
                }
            }
        };

        abstract fun useFireworkSlot(slot: HotbarItemSlot, resetDelay: Int)
    }
}
