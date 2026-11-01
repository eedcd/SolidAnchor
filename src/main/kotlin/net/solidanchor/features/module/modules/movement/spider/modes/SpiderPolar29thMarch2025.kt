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
package net.solidanchor.features.module.modules.movement.spider.modes

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.BlockShapeEvent
import net.solidanchor.event.events.PlayerJumpEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.movement.spider.ModuleSpider
import net.solidanchor.utils.math.shrink

/**
 * Shrinks the block collision shape and allows you to walk on it.
 * Might not work on every surface.
 *
 * @testedOn pika.host
 * @anticheat Polar
 */
internal object SpiderPolar29thMarch2025 : Mode("Polar-29.03.2025") {

    override val parent: ModeValueGroup<Mode>
        get() = ModuleSpider.modes

    /**
     * Polar allows jumping 0.6 high, but it's faster to use 0.55 to hit the
     * block collision shape.
     */
    private val jumpHeight by float("JumpHeight", 0.55f, 0.42f..0.6f)

    @Suppress("unused")
    private val boxHandler = handler<BlockShapeEvent> { event ->
        if (event.pos.y >= player.position().y || player.isShiftKeyDown && player.onGround()) {
            event.shape = event.shape.shrink(
                x = 0.0001,
                z = 0.0001
            )
        }
    }

    @Suppress("unused")
    private val jumpHandler = handler<PlayerJumpEvent> { event ->
        var highJump = jumpHeight
        if (player.horizontalCollision && highJump > 0.42f) {
            event.motion = highJump
        }
    }

}
