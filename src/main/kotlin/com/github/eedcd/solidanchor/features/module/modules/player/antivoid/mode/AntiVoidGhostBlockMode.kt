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
package net.solidanchor.features.module.modules.player.antivoid.mode

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.BlockShapeEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.player.antivoid.ModuleAntiVoid
import net.solidanchor.features.module.modules.player.antivoid.ModuleAntiVoid.isLikelyFalling
import net.solidanchor.features.module.modules.player.antivoid.ModuleAntiVoid.rescuePosition
import net.solidanchor.features.module.modules.player.antivoid.mode.AntiVoidGhostBlockMode.handleBlockShape
import net.minecraft.world.phys.shapes.Shapes
import kotlin.math.floor

object AntiVoidGhostBlockMode : AntiVoidMode("GhostBlock") {

    override val parent: ModeValueGroup<*>
        get() = ModuleAntiVoid.mode

    @Suppress("unused")
    private val handleBlockShape = handler<BlockShapeEvent> { event ->
        if (!isLikelyFalling || isExempt) {
            return@handler
        }

        // We only want to place a fake-block collision below the player if the collision shape is empty.
        var safePosition = rescuePosition
        if (!event.shape.isEmpty || safePosition == null || event.pos.y >= floor(safePosition.y)) {
            return@handler
        }

        event.shape = Shapes.block()
    }

    /**
     * We have [handleBlockShape] to fix our situation instead.
     */
    override fun rescue() = false

}
