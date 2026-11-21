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

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories

/**
 * NoPush module
 *
 * Disables pushing from other players and some other situations where someone/something can push.
 */
object ModuleNoPush : ClientModule("NoPush", ModuleCategories.MOVEMENT) {
    private val noPushBy = multiEnumChoice("PushBy",
        NoPushBy.ENTITIES,
        NoPushBy.LIQUIDS
    )

    @JvmStatic
    fun canPush(by: NoPushBy) = !running || by in noPushBy

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (NoPushBy.SINKING !in noPushBy) {
            return@handler
        }

        if (mc.options.keyJump.isDown || mc.options.keyShift.isDown) {
            return@handler
        }

        if (player.isInLiquid && player.deltaMovement.y < 0) {
            player.deltaMovement.y = 0.0
        }
    }
}

enum class NoPushBy(override val tag: String): Tagged {
    ENTITIES("Entities"),
    BLOCKS("Blocks"),
    FISHING_ROD("FishingRod"),
    LIQUIDS("Liquids"),
    SINKING("Sinking")
}
