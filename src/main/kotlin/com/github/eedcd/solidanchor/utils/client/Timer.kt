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
package com.github.eedcd.solidanchor.utils.client

import com.github.eedcd.solidanchor.event.EventListener
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.addon.AddonApi
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.utils.client.Timer.requestTimerSpeed
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import com.github.eedcd.solidanchor.utils.kotlin.Priority

/** Global minecraft timer */
@AddonApi
object Timer : EventListener {
    private val requestHandler = RequestHandler<Float>()

    /**
     * You cannot set this manually. Use [requestTimerSpeed] instead.
     */
    val timerSpeed: Float
        get() = requestHandler.getActiveRequestValue() ?: 1.0f

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent>(priority = FIRST_PRIORITY) {
        requestHandler.tick()
    }

    /**
     * Requests a timer speed change. If another module requests with a higher priority,
     * the other module is prioritized.
     */
    @JvmOverloads
    fun requestTimerSpeed(timerSpeed: Float, priority: Priority, provider: ClientModule, resetAfterTicks: Int = 1) {
        requestHandler.request(
            RequestHandler.Request(
                // this prevents requests from being instantly removed
                resetAfterTicks + 1,
                priority.priority,
                provider,
                timerSpeed
            )
        )
    }
}
