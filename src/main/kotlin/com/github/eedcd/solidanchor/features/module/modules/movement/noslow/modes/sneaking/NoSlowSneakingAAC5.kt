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

package net.solidanchor.features.module.modules.movement.noslow.modes.sneaking

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.EventState
import net.solidanchor.event.events.NotificationEvent
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.handler
import net.solidanchor.utils.client.isNewerThanOrEquals1_21_6
import net.solidanchor.utils.client.notification
import net.solidanchor.utils.network.send1_21_5StartSneaking
import net.solidanchor.utils.client.usesViaFabricPlus

internal class NoSlowSneakingAAC5(override val parent: ModeValueGroup<*>) : Mode("AAC5") {
    private val timingMode by enumChoice("Timing", TimingMode.PRE_POST)

    override fun enable() {
        if (!usesViaFabricPlus || isNewerThanOrEquals1_21_6) {
            notification(
                "Protocol Error",
                "This mode can only be used on server with version earlier than 1.21.6.",
                NotificationEvent.Severity.ERROR,
            )
        }
        super.enable()
    }

    @Suppress("unused", "ComplexCondition")
    private val networkTickHandler = handler<PlayerNetworkMovementTickEvent> { event ->
        if (timingMode == TimingMode.PRE_POST
            || event.state == EventState.PRE && timingMode == TimingMode.PRE_TICK
            || event.state == EventState.POST && timingMode == TimingMode.POST_TICK) {
            network.send1_21_5StartSneaking()
        }
    }

    private enum class TimingMode(override val tag: String) : Tagged {
        PRE_POST("PreAndPost"),
        PRE_TICK("Pre"),
        POST_TICK("Post")
    }
}
