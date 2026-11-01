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
package net.solidanchor.features.command.commands.module.teleport

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.features.command.CommandException
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.features.command.arguments.PlayerInfoArgumentType
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.CmdI18n
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.module.MinecraftShortcuts
import net.solidanchor.features.module.modules.movement.ModuleTeleport
import kotlin.math.floor

/**
 * Teleport Command
 *
 * Allows you to teleport.
 *
 * Module: [ModuleTeleport]
 */
object CommandPlayerTeleport : MinecraftShortcuts, CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("playerteleport", aliases = listOf("playertp", "ptp")) {
            requires { it.isIngame }
            argument("player", PlayerInfoArgumentType) { player ->
                optional("copy", ClientStringArgumentType.word(), default = null) { copy ->
                    exec { ctx ->
                        teleportTo(ctx.get(player).profile.name, copy = ctx.get(copy) == "copy")
                        1
                    }
                }
            }
        }
    }

    private fun CmdI18n.teleportTo(playerName: String, copy: Boolean) {
        val target = world.players().find { it.gameProfile.name.equals(playerName, true) }
            ?: throw CommandException(t("playerNotFound"))

        val y = if (ModuleTeleport.highTp) {
            ModuleTeleport.highTpAmount
        } else {
            target.y
        }

        if (copy) {
            val clipboard = ".teleport ${floor(target.x).toInt()} " +
                "${floor(y.toDouble()).toInt()} ${floor(target.z).toInt()}"

            mc.keyboardHandler.clipboard = clipboard
            return
        }

        ModuleTeleport.indicateTeleport(target.x, y.toDouble(), target.z)
    }

}
