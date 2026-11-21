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
package net.solidanchor.features.command.commands.ingame

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.event.EventListener
import net.solidanchor.event.EventState
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.handler
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.utils.client.inGame
import net.solidanchor.utils.client.player
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.math.center

/**
 * Center command
 *
 * Centers you at your current position.
 */
object CommandCenter : EventListener, CommandRegistrar {

    var state = CenterHandlerState.INACTIVE

    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("center") {
            requires { it.isIngame }
            exec {
                state = CenterHandlerState.APPLY_ON_NEXT_EVENT
                1
            }
        }
    }

    @Suppress("unused")
    private val moveHandler =
        handler<PlayerNetworkMovementTickEvent>(priority = EventPriorityConvention.SAFETY_FEATURE) {
            if (it.state == EventState.POST) {
                return@handler
            }

            val pos = player.blockPosition().center
            val delta = player.position().subtract(pos)
            it.x = delta.x
            it.y = delta.y
            it.z = delta.z
            state = CenterHandlerState.INACTIVE
        }

    override val running: Boolean
        get() = super.running && inGame && state == CenterHandlerState.APPLY_ON_NEXT_EVENT

    enum class CenterHandlerState {
        INACTIVE,
        APPLY_ON_NEXT_EVENT
    }

}
