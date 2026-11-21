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
package com.github.eedcd.solidanchor.features.command.commands.module

import com.mojang.brigadier.CommandDispatcher
import com.github.eedcd.solidanchor.features.command.CommandException
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.arguments.PlayerInfoArgumentType
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.get
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleInventoryTracker
import com.github.eedcd.solidanchor.utils.client.mc
import com.github.eedcd.solidanchor.utils.client.world
import com.github.eedcd.solidanchor.utils.inventory.ViewedInventoryScreen
import java.util.UUID

/**
 * Command Invsee
 *
 * Lets you view another player's inventory.
 *
 * Module: [ModuleInventoryTracker]
 */
object CommandInvsee : CommandRegistrar {
    var viewedPlayer: UUID? = null

    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("invsee") {
            requires { it.isIngame }
            argument("name", PlayerInfoArgumentType) { name ->
                exec { ctx ->
                    val playerInfo = ctx.get(name)
                    val inputName = playerInfo.profile.name
                    val playerID = playerInfo.profile.id
                    val player = { world.getPlayerByUUID(playerID) ?: ModuleInventoryTracker.playerMap[playerID] }

                    if (player() == null) {
                        throw CommandException(t("playerNotFound", inputName))
                    }

                    mc.schedule {
                        mc.gui.setScreen(ViewedInventoryScreen(player))
                    }

                    viewedPlayer = playerID
                    1
                }
            }
        }
    }

}
