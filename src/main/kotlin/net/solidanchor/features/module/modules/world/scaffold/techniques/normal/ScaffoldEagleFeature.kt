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
package net.solidanchor.features.module.modules.world.scaffold.techniques.normal

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.utils.asRefreshable
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.world.scaffold.techniques.ScaffoldNormalTechnique
import net.solidanchor.utils.entity.isCloseToEdge
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.movement.DirectionalInput

object ScaffoldEagleFeature : ToggleableValueGroup(ScaffoldNormalTechnique, "Eagle", false) {

    private val blocksToEagle = intRange("BlocksToEagle", 0..0, 0..10).asRefreshable()
    private val edgeDistance = floatRange("EdgeDistance", 0.01f..0.05f, 0.01f..1.3f).asRefreshable()
    private val onlyOnGround by boolean("OnlyOnGround", true)

    // Makes you sneak until first block placed, so with eagle enabled you won't fall off, when enabled
    private var placedBlocks = 0

    override fun onEnabled() {
        placedBlocks = 0
        super.onEnabled()
    }

    @Suppress("unused")
    private val stateUpdateHandler =
        handler<MovementInputEvent>(priority = EventPriorityConvention.SAFETY_FEATURE) {
            if (!it.sneak && shouldEagle(it.directionalInput)) {
                it.sneak = true
            }
        }

    fun shouldEagle(input: DirectionalInput): Boolean {
        if (ScaffoldDownFeature.shouldFallOffBlock()) {
            return false
        }

        if (!player.onGround() && onlyOnGround) {
            return false
        }

        val shouldBeActive = !player.abilities.flying && placedBlocks == 0

        return shouldBeActive && player.isCloseToEdge(input, edgeDistance.current.toDouble())
    }

    fun onBlockPlacement() {
        if (!enabled) {
            return
        }

        placedBlocks++

        if (placedBlocks > blocksToEagle.current) {
            placedBlocks = 0
            blocksToEagle.refresh()
            edgeDistance.refresh()
        }
    }

}
