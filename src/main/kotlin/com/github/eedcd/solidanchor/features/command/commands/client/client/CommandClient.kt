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

package net.solidanchor.features.command.commands.client.client

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.register

/**
 * Client Command
 *
 * Provides subcommands for client management.
 */
object CommandClient : CommandRegistrar {

    /**
     * Registers the client command with a variety of subcommands.
     */
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("client") {
            with(CommandClientInfoSubcommand) { info() }
            with(CommandClientLanguageSubcommand) { language() }
            with(CommandClientPrefixSubcommand) { prefix() }
            with(CommandClientDestructSubcommand) { destruct() }
            with(CommandClientConfigSubcommand) { config() }
        }
    }

}
