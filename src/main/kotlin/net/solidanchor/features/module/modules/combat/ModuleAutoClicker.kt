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
package net.solidanchor.features.module.modules.combat

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.ccbluex.fastutil.enumSetOf
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.SprintEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.tickUntil
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.combat.criticals.ModuleCriticals.CriticalsSelectionMode
import net.solidanchor.utils.clicking.Clicker
import net.solidanchor.utils.collection.blockSortedSetOf
import net.solidanchor.utils.collection.itemSortedSetOf
import net.solidanchor.utils.combat.shouldBeAttacked
import net.solidanchor.utils.input.InputTracker.isPressedOnAny
import net.solidanchor.utils.item.WeaponType
import net.solidanchor.utils.kotlin.matchesAny
import net.minecraft.client.KeyMapping
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

/**
 * AutoClicker module
 *
 * Clicks automatically when holding down a mouse button.
 */

object ModuleAutoClicker : ClientModule("AutoClicker", ModuleCategories.COMBAT, aliases = listOf("TriggerBot")) {

    object AttackButton : ToggleableValueGroup(this, "Attack", true) {

        val clicker = tree(Clicker(this, mc.options.keyAttack, simulateAttackKeyDown = true))

        internal val requiresNoInput by boolean("RequiresNoInput", false)
        internal val delayOnBroken by boolean("DelayOnBroken", true)
        private val objectiveType by enumChoice("Objective", ObjectiveType.ANY)
        private val onItemUse by enumChoice("OnItemUse", Use.WAIT)
        private val weapon by multiEnumChoice("Weapon", enumSetOf(WeaponType.ANY), canBeNone = false)
        internal val criticalsSelectionMode by enumChoice("Criticals", CriticalsSelectionMode.SMART)
        private val delayPostStopUse by int("DelayPostStopUse", 0, 0..20, "ticks")

        private enum class ObjectiveType(override val tag: String) : Tagged {
            ENEMY("Enemy"),
            ENTITY("Entity"),
            BLOCK("Block"),
            ANY("Any")
        }

        private enum class Use(override val tag: String) : Tagged {
            WAIT("Wait"),
            STOP("Stop"),
            IGNORE("Ignore")
        }

        fun isOnObjective(): Boolean {
            val crosshair = mc.hitResult

            return when (objectiveType) {
                ObjectiveType.ENEMY -> crosshair is EntityHitResult && crosshair.entity.shouldBeAttacked()
                ObjectiveType.ENTITY -> crosshair is EntityHitResult
                ObjectiveType.BLOCK -> crosshair is BlockHitResult
                ObjectiveType.ANY -> true
            }
        }

        fun isWeaponSelected(): Boolean = weapon.matchesAny(player.mainHandItem)

        suspend fun encounterItemUse(): Boolean {
            return when (onItemUse) {
                Use.WAIT -> {
                    tickUntil { !player.isUsingItem }
                    waitTicks(delayPostStopUse)
                    true
                }

                Use.STOP -> {
                    interaction.releaseUsingItem(player)
                    waitTicks(delayPostStopUse)
                    true
                }

                Use.IGNORE -> false
            }
        }

        @Suppress("unused")
        private val sprintHandler = handler<SprintEvent> { event ->
            if (event.source == SprintEvent.Source.MOVEMENT_TICK || event.source == SprintEvent.Source.INPUT) {
                if (!attack || !isOnObjective() || !isWeaponSelected()) {
                    return@handler
                }

                val target = mc.hitResult as? EntityHitResult ?: return@handler
                if (criticalsSelectionMode.shouldStopSprinting(clicker, target.entity)) {
                    event.sprint = false
                }
            }
        }

    }

    object UseButton : ToggleableValueGroup(this, "Use", false) {
        val clicker = tree(Clicker(this, mc.options.keyUse, null))
        internal val holdingItemsForIgnore by items(
            "HoldingItemsForIgnore",
            default = itemSortedSetOf(
                Items.WATER_BUCKET,
                Items.LAVA_BUCKET,
                Items.ENDER_PEARL,
                Items.ENDER_EYE,
                Items.PLAYER_HEAD,
            ),
        )
        internal val blocksForIgnore by blocks(
            "BlocksForIgnore",
            default = BuiltInRegistries.BLOCK.filterTo(blockSortedSetOf()) {
                it is DoorBlock || it is FenceGateBlock || it is TrapDoorBlock
            },
        )
        internal val delayStart by boolean("DelayStart", false)
        internal val onlyBlock by boolean("OnlyBlock", false)
        internal val requiresNoInput by boolean("RequiresNoInput", false)

        internal var needToWait = true
    }

    private val SPECIAL_ITEMS_FOR_IGNORE = ReferenceOpenHashSet.of(
        Items.BED.red,
        Items.PLAYER_HEAD,
        Items.COMPASS,
        Items.EMERALD,
        Items.LAPIS_LAZULI,
        Items.DYE.green,
        Items.DYE.gray,
        Items.DYE.pink,
        Items.SLIME_BALL,
    )

    init {
        tree(AttackButton)
        tree(UseButton)
    }

    val attack: Boolean
        get() = mc.options.keyAttack.isPressedOnAny || AttackButton.requiresNoInput

    val use: Boolean
        get() = mc.options.keyUse.isPressedOnAny || UseButton.requiresNoInput

    @Volatile
    private var lastFinishBreak = 0L

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet is ServerboundPlayerActionPacket
            && packet.action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK
        ) {
            lastFinishBreak = System.currentTimeMillis()
        }
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        AttackButton.run {
            if (!enabled || !attack || !isWeaponSelected() || !isOnObjective()) {
                return@run
            }

            // Check if the player is breaking a block, if so, return
            if (interaction.isDestroying) {
                return@run
            }

            if ((System.currentTimeMillis() - lastFinishBreak < 300L) && delayOnBroken) {
                return@run
            }

            val crosshairTarget = mc.hitResult
            if (crosshairTarget is EntityHitResult) {
                ModuleAutoWeapon.onTarget(crosshairTarget.entity)

                if (!criticalsSelectionMode.isCriticalHit()) {
                    return@run
                }
            }

            if (player.isUsingItem) {
                val encounterItemUse = encounterItemUse()

                if (encounterItemUse) {
                    return@tickHandler
                }
            }

            clicker.click {
                KeyMapping.click(mc.options.keyAttack.key)
                true
            }
        }

        UseButton.run {
            if (!enabled) return@run

            if (!use) {
                needToWait = true
                return@run
            }

            val mainHandStack = player.mainHandItem
            val offHandStack = player.offhandItem
            if (mainHandStack.item in SPECIAL_ITEMS_FOR_IGNORE && mainHandStack.customName != null) {
                return@run
            }

            if (mainHandStack.item in holdingItemsForIgnore || offHandStack.item in holdingItemsForIgnore) {
                return@run
            }

            if (onlyBlock && mainHandStack.item !is BlockItem && offHandStack.item !is BlockItem) {
                return@run
            }

            val crosshairTarget = mc.hitResult
            if (crosshairTarget is BlockHitResult) {
                val blockState = mc.level?.getBlockState(crosshairTarget.blockPos)
                if (blockState?.block in blocksForIgnore) {
                    return@run
                }
            }

            if (delayStart && needToWait) {
                needToWait = false
                waitTicks(2)
                return@run
            }

            clicker.click {
                KeyMapping.click(mc.options.keyUse.key)
                true
            }
        }
    }
}
