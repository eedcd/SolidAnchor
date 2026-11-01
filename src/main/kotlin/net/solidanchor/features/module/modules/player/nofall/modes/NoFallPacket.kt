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
package net.solidanchor.features.module.modules.player.nofall.modes

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.tickHandler
import net.solidanchor.utils.network.MovePacketType

internal object NoFallPacket : NoFallMode("Packet") {
    private val packetType by enumChoice("PacketType", MovePacketType.FULL)
    private val filter = modes("Filter", FallDistance, arrayOf(FallDistance, Always))

    val repeatable = tickHandler {
        if (filter.activeMode.isActive) {
            network.send(packetType.generatePacket().apply {
                onGround = true
            })

            if (filter.activeMode is FallDistance && FallDistance.resetFallDistance) {
                player.resetFallDistance()
            }
        }
    }

    private abstract class Filter(name: String) : Mode(name) {
        override val parent: ModeValueGroup<*>
            get() = filter

        abstract val isActive: Boolean
    }

    private object FallDistance : Filter("FallDistance") {
        override val isActive: Boolean
            get() = player.fallDistance - player.deltaMovement.y > distance.activeMode.value && player.tickCount > 20

        private val distance = modes("Distance", Smart, arrayOf(Smart, Constant))
        val resetFallDistance by boolean("ResetFallDistance", true)

        private abstract class DistanceMode(name: String) : Mode(name) {
            override val parent: ModeValueGroup<*>
                get() = distance

            abstract val value: Float
        }

        private object Smart : DistanceMode("Smart") {
            override val value: Float
                get() = playerSafeFallDistance.toFloat()
        }

        private object Constant : DistanceMode("Constant") {
            override val value by float("Value", 2f, 0f..5f)
        }
    }

    private object Always : Filter("Always") {
        override val isActive: Boolean
            get() = true
    }
}
