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
package net.solidanchor.features.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.commands.ingame.CommandPing
import net.solidanchor.features.command.commands.ingame.CommandTps
import net.solidanchor.features.command.commands.ingame.CommandUsername
import net.solidanchor.features.command.commands.ingame.creative.CommandItemEnchant
import net.solidanchor.features.command.commands.ingame.creative.CommandItemGive
import net.solidanchor.features.command.commands.ingame.creative.CommandItemRename
import net.solidanchor.features.command.commands.ingame.creative.CommandItemSkull
import net.solidanchor.features.command.commands.ingame.creative.CommandItemStack
import net.solidanchor.features.command.commands.module.CommandAutoAccount
import net.solidanchor.test.MinecraftBootstrap
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Smoke test for commands written directly against the Brigadier tree
 * (see the `brigadier` package DSL).
 *
 * Only commands whose `register` does not touch the [CommandExecutor] static initializer
 * (i.e. no `executesSuspend`) can be registered in the unit-test environment, because
 * [CommandExecutor] requires a running Minecraft client.
 */
class CommandRegistrationTest {

    private val dispatcher = CommandDispatcher<ClientCommandSource>()

    @BeforeTest
    fun bootstrapMinecraft() {
        MinecraftBootstrap.ensureInitialized()
    }

    private val registrars: List<Pair<String, CommandRegistrar>> = listOf(
        "ping" to CommandPing,
        "tps" to CommandTps,
        "username" to CommandUsername,
        "autoaccount" to CommandAutoAccount,
        "rename" to CommandItemRename,
        "give" to CommandItemGive,
        "skull" to CommandItemSkull,
        "stack" to CommandItemStack,
        "enchant" to CommandItemEnchant,
    )

    @Test
    fun `directly registered commands appear as root children`() {
        registrars.forEach { (_, registrar) -> registrar.register(dispatcher) }

        registrars.forEach { (rootName, _) ->
            assertTrue(
                dispatcher.root.getChild(rootName) != null,
                "Root child '$rootName' not registered"
            )
        }
    }

    @Test
    fun `root children are literal nodes`() {
        registrars.forEach { (_, registrar) -> registrar.register(dispatcher) }

        val literals = dispatcher.root.children.filterIsInstance<LiteralCommandNode<ClientCommandSource>>()
        registrars.forEach { (rootName, _) ->
            assertTrue(
                literals.any { it.name == rootName },
                "Root child '$rootName' is not a literal node"
            )
        }
    }

}
