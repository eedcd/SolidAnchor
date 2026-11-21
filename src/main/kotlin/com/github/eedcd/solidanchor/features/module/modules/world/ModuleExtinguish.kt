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
package com.github.eedcd.solidanchor.features.module.modules.world

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.RotationUpdateEvent
import com.github.eedcd.solidanchor.event.events.WorldChangeEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.utils.aiming.RotationManager
import com.github.eedcd.solidanchor.utils.aiming.RotationsValueGroup
import com.github.eedcd.solidanchor.utils.block.doPlacement
import com.github.eedcd.solidanchor.utils.block.liquid.TimedPickupTracker
import com.github.eedcd.solidanchor.utils.block.liquid.planPlacementAtPos
import com.github.eedcd.solidanchor.utils.block.targetfinding.PlacementPlan
import com.github.eedcd.solidanchor.utils.block.targetfinding.verifyClick
import com.github.eedcd.solidanchor.utils.client.Chronometer
import com.github.eedcd.solidanchor.utils.client.SilentHotbar
import com.github.eedcd.solidanchor.utils.combat.CombatManager
import com.github.eedcd.solidanchor.utils.entity.PlayerSimulationCache
import com.github.eedcd.solidanchor.utils.entity.rotation
import com.github.eedcd.solidanchor.utils.inventory.Slots
import com.github.eedcd.solidanchor.utils.inventory.findClosestSlot
import com.github.eedcd.solidanchor.utils.kotlin.Priority
import com.github.eedcd.solidanchor.utils.math.toBlockPos
import com.github.eedcd.solidanchor.utils.world.waterEvaporates
import net.minecraft.core.BlockPos
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items

/**
 * Module Extinguish
 *
 * Automatically extinguishes yourself when you're burning.
 */
object ModuleExtinguish: ClientModule("Extinguish", ModuleCategories.WORLD) {

    private val cooldown by float("Cooldown", 1.0F, 0.0F..20.0F, "s")
    private val notDuringCombat by boolean("NotDuringCombat", true)

    private object Pickup : ToggleableValueGroup(ModuleExtinguish, "Pickup", true) {
        val pickupSpan by floatRange("PickupSpan", 0.1F..10.0F, 0.0F..20.0F, "s")
    }

    init {
        tree(Pickup)
    }

    private var currentTarget: PlacementPlan? = null

    private val rotations = tree(RotationsValueGroup(this))

    private val cooldownTimer = Chronometer()
    private val pickupTracker = TimedPickupTracker(capacity = 1)

    override fun onEnabled() {
        currentTarget = null
        pickupTracker.clear()
    }

    override fun onDisabled() {
        SilentHotbar.resetSlot(this)
    }

    @Suppress("unused")
    private val rotationUpdateHandler = handler<RotationUpdateEvent> {
        // we can't place water in the nether
        if (world.waterEvaporates) {
            return@handler
        }

        this.currentTarget = null

        val target = findAction() ?: return@handler

        this.currentTarget = target

        RotationManager.setRotationTarget(
            target.placementTarget.rotation,
            valueGroup = rotations,
            priority = Priority.IMPORTANT_FOR_PLAYER_LIFE,
            provider = ModuleExtinguish
        )
    }

    @Suppress("unused")
    private val worldChangeHandler = handler<WorldChangeEvent> {
        currentTarget = null
    }

    private fun findAction(): PlacementPlan? {
        val pickupSpanStart = (Pickup.pickupSpan.start * 1000.0F).toLong()
        val pickupSpanEnd = (Pickup.pickupSpan.endInclusive * 1000.0F).toLong()

        pickupTracker.prune(pickupSpanEnd) { true }

        if (player.hasEffect(MobEffects.FIRE_RESISTANCE) || (notDuringCombat && CombatManager.isInCombat)) {
            return null
        }

        val pickupPos = pickupTracker.firstEligible(pickupSpanStart)

        if (pickupPos != null && Pickup.enabled) {
            planPickup(pickupPos)?.let {
                return it
            }
        }

        if (!player.isOnFire || !cooldownTimer.hasElapsed()) {
            return null
        }

        return planExtinguishing()
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        val target = currentTarget ?: return@handler

        val rotation = RotationManager.currentRotation ?: player.rotation
        val rayTraceResult = target.placementTarget.verifyClick(rotation) ?: return@handler

        if (!SilentHotbar.selectSlotSilently(this, target.hotbarItemSlot, 1)) {
            return@handler
        }

        val successFunction = {
            cooldownTimer.waitForAtLeast((cooldown * 1000.0F).toLong())
            pickupTracker.record(target.placementTarget.placedBlock)

            true
        }

        doPlacement(
            rayTraceResult,
            rotation,
            hand = target.hotbarItemSlot.useHand,
            onItemUseSuccess = successFunction,
            onPlacementSuccess = successFunction,
        )
    }

    private fun planExtinguishing(): PlacementPlan? {
        val waterBucketSlot = Slots.OffhandWithHotbar.findClosestSlot(Items.WATER_BUCKET) ?: return null

        val simulation = PlayerSimulationCache.getSimulationForLocalPlayer()

        val frameOnGround = simulation.simulateBetween(0..20).firstOrNull {
            it.onGround
        } ?: return null

        val playerPos = frameOnGround.pos.toBlockPos()
        return planPlacementAtPos(playerPos, waterBucketSlot, frameOnGround.pos)
    }

    private fun planPickup(blockPos: BlockPos): PlacementPlan? {
        val bucket = Slots.OffhandWithHotbar.findClosestSlot(Items.BUCKET) ?: return null
        return planPlacementAtPos(blockPos, bucket)
    }

}
