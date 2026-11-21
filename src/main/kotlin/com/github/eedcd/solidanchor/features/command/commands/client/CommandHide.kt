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
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.arguments.MultiSelectArgumentType
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.get
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.features.command.preset.pagedList
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleManager
import com.github.eedcd.solidanchor.utils.client.MessageMetadata
import com.github.eedcd.solidanchor.utils.client.bold
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.copyable
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.client.variable
import com.github.eedcd.solidanchor.utils.client.withColor
import com.github.eedcd.solidanchor.utils.text.asText
import com.github.eedcd.solidanchor.utils.text.joinToText
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ComponentUtils

/**
 * Hide Command
 *
 * Allows you to hide specific modules.
 */
object CommandHide : CommandRegistrar {
    @Suppress("detekt:LongMethod")
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("hide") {
            literal("hide") {
                argument(
                    "modules",
                    MultiSelectArgumentType(
                        "Module",
                        ModuleManager,
                        predicate = { !it.hidden },
                        nameOf = ClientModule::name
                    ),
                ) { modules ->
                    exec { ctx ->
                        val hiddenModules = ctx.get(modules)
                        hiddenModules.forEach { it.hidden = true }

                        chat(
                            t("hide.moduleHidden",
                                hiddenModules.joinToText(ComponentUtils.DEFAULT_SEPARATOR) { variable(it.name) }
                            ),
                            metadata = MessageMetadata(id = "CHide#info")
                        )
                        1
                    }
                }
            }
            literal("unhide") {
                argument(
                    "modules",
                    MultiSelectArgumentType(
                        "Module",
                        ModuleManager,
                        predicate = { it.hidden },
                        nameOf = ClientModule::name
                    ),
                ) { modules ->
                    exec { ctx ->
                        val unhiddenModules = ctx.get(modules)
                        unhiddenModules.forEach { it.hidden = false }

                        chat(
                            t("unhide.moduleUnhidden",
                                unhiddenModules.joinToText(ComponentUtils.DEFAULT_SEPARATOR) { variable(it.name) }
                            ),
                            metadata = MessageMetadata(id = "CHide#info")
                        )
                        1
                    }
                }
            }
            literal("clear") {
                exec {
                    ModuleManager.forEach { it.hidden = false }
                    chat(
                        regular(
                            t("clear.modulesUnhidden")
                        ),
                        metadata = MessageMetadata(id = "CHide#info")
                    )
                    1
                }
            }
            pagedList(
                header = {
                    t("list.hidden")
                        .withColor(ChatFormatting.RED)
                        .bold(true)
                },
                items = {
                    ModuleManager.filter { it.hidden }
                },
                eachRow = { _, module ->
                    "\u2B25 ".asText()
                        .withStyle(ChatFormatting.BLUE)
                        .append(variable(module.name).copyable())
                        .append(regular(" ("))
                        .append(regular(t("list.hidden"))) // TODO: click to unhide?
                        .append(regular(")"))
                }
            )
        }
    }

}
