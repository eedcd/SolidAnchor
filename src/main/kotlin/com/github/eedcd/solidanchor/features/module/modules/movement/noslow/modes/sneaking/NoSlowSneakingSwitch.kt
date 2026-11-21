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

package com.github.eedcd.solidanchor.features.module.modules.movement.noslow.modes.sneaking

import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.config.types.list.Tagged
import com.github.eedcd.solidanchor.event.EventState
import com.github.eedcd.solidanchor.event.events.NotificationEvent
import com.github.eedcd.solidanchor.event.events.PlayerNetworkMovementTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.utils.client.isNewerThanOrEquals1_21_6
import com.github.eedcd.solidanchor.utils.client.notification
import com.github.eedcd.solidanchor.utils.network.send1_21_5StartSneaking
import com.github.eedcd.solidanchor.utils.network.send1_21_5StopSneaking
import com.github.eedcd.solidanchor.utils.client.usesViaFabricPlus

internal class NoSlowSneakingSwitch(override val parent: ModeValueGroup<*>) : Mode("Switch") {
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

    @Suppress("unused")
    private val networkTickHandler = handler<PlayerNetworkMovementTickEvent> { event ->
        when (timingMode) {
            TimingMode.PRE_POST -> when (event.state) {
                EventState.PRE -> network.send1_21_5StartSneaking()
                EventState.POST -> network.send1_21_5StopSneaking()
            }
            TimingMode.PRE_TICK -> if (event.state == EventState.PRE) {
                network.send1_21_5StartSneaking()
                network.send1_21_5StopSneaking()
            }
            TimingMode.POST_TICK -> if (event.state == EventState.POST) {
                network.send1_21_5StartSneaking()
                network.send1_21_5StopSneaking()
            }
        }
    }

    private enum class TimingMode(override val tag: String) : Tagged {
        PRE_POST("PreAndPost"),
        PRE_TICK("Pre"),
        POST_TICK("Post")
    }
}
