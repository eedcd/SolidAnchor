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
package com.github.eedcd.solidanchor.features.command.commands.client

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import com.github.eedcd.solidanchor.features.command.CommandManager
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.features.command.preset.pagedQuery
import com.github.eedcd.solidanchor.lang.translation
import com.github.eedcd.solidanchor.utils.client.bold
import com.github.eedcd.solidanchor.utils.client.onClick
import com.github.eedcd.solidanchor.utils.client.onHover
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.client.withColor
import com.github.eedcd.solidanchor.utils.text.asText
import com.github.eedcd.solidanchor.utils.text.buildText
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.HoverEvent

/**
 * Help Command
 *
 * Provides a help page for displaying other commands.
 */
object CommandHelp : CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("help") {
            pagedQuery(
                header = {
                    t("help").withColor(ChatFormatting.RED).bold(true)
                },
                items = {
                    CommandManager.mainCommandNodes
                },
                eachRow = { _, node ->
                    buildRow(node)
                }
            )
        }
    }

    private fun buildRow(node: LiteralCommandNode<ClientCommandSource>): Component {
        val commandName = node.name
        val commandStart = CommandManager.GlobalSettings.prefix + commandName
        return "\u2B25 ".asText()
            .withStyle(ChatFormatting.BLUE)
            .onHover(
                HoverEvent.ShowText(
                    translation("solidanchor.command.$commandName.description")
                )
            )
            .append(
                commandStart.asText()
                    .withStyle(ChatFormatting.GRAY)
                    .onClick(ClickEvent.SuggestCommand(commandStart))
            )
            .append(buildAliasesText(node))
    }

    /**
     * Alias nodes are registered as redirecting literals that share the main node's subtree.
     */
    private fun buildAliasesText(mainNode: LiteralCommandNode<ClientCommandSource>): Component = buildText {
        val aliases = CommandManager.rootCommandNodes
            .filter { it.redirect === mainNode }
            .map { it.name }

        aliases.forEach { alias ->
            this += ComponentUtils.DEFAULT_SEPARATOR
            this += regular(alias).withStyle(ChatFormatting.GRAY)
                .onClick(ClickEvent.SuggestCommand(CommandManager.GlobalSettings.prefix + alias))
        }
    }

}
