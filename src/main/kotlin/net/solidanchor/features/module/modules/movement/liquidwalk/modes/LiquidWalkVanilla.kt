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

package net.solidanchor.features.module.modules.movement.liquidwalk.modes

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.BlockShapeEvent
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.liquidwalk.ModuleLiquidWalk
import net.solidanchor.utils.block.isBlockAtPosition
import net.solidanchor.utils.entity.box
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.phys.shapes.Shapes

internal object LiquidWalkVanilla : Mode("Vanilla") {

    override val parent: ModeValueGroup<Mode>
        get() = ModuleLiquidWalk.modes

    @Suppress("unused")
    val inputHandler = handler<MovementInputEvent> { event ->
        if (event.sneak || !player.box.isBlockAtPosition { it is LiquidBlock }) {
            return@handler
        }

        // Swims up
        event.jump = true
    }

    @Suppress("unused")
    val shapeHandler = handler<BlockShapeEvent> { event ->
        if (mc.options.keyShift.isDown || player.fallDistance > 3.0f || player.isOnFire) {
            return@handler
        }

        val block = event.state.block

        if (block is LiquidBlock && !player.box.isBlockAtPosition { it is LiquidBlock }) {
            event.shape = Shapes.block()
        }
    }

}
