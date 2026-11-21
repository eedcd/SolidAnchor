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

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.brigadier.CommandDispatcher
import com.github.eedcd.solidanchor.features.command.CommandException
import com.github.eedcd.solidanchor.features.command.CommandExecutor
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.arguments.ClientStringArgumentType
import com.github.eedcd.solidanchor.features.command.arguments.ModuleArgumentType
import com.github.eedcd.solidanchor.features.command.arguments.MultiSelectArgumentType
import com.github.eedcd.solidanchor.features.command.arguments.TaggedArgumentType
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.CmdI18n
import com.github.eedcd.solidanchor.features.command.brigadier.get
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.features.command.brigadier.suggestions
import com.github.eedcd.solidanchor.features.command.preset.pagedList
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleManager
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleClickGui
import com.github.eedcd.solidanchor.utils.client.MessageMetadata
import com.github.eedcd.solidanchor.utils.client.bold
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.copyable
import com.github.eedcd.solidanchor.utils.client.highlight
import com.github.eedcd.solidanchor.utils.client.markAsError
import com.github.eedcd.solidanchor.utils.client.onClickRun
import com.github.eedcd.solidanchor.utils.client.onHover
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.client.variable
import com.github.eedcd.solidanchor.utils.client.withColor
import com.github.eedcd.solidanchor.utils.input.InputBind
import com.github.eedcd.solidanchor.utils.input.availableInputKeys
import com.github.eedcd.solidanchor.utils.input.bind
import com.github.eedcd.solidanchor.utils.input.inputByName
import com.github.eedcd.solidanchor.utils.input.renderText
import com.github.eedcd.solidanchor.utils.input.unbind
import com.github.eedcd.solidanchor.utils.text.asText
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.HoverEvent

/**
 * Binds Command
 *
 * Allows you to manage the bindings of modules to keys.
 * It provides subcommands to add, remove, list and clear bindings.
 */
object CommandBinds : CommandRegistrar {
    @Suppress("detekt:LongMethod")
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("binds") binds@{
            literal("clear") {
                exec {
                    ModuleManager.forEach { it.bindValue.unbind() }
                    chat(
                        t("clear.bindsCleared"),
                        metadata = MessageMetadata(id = "Binds#global")
                    )
                    1
                }
            }
            pagedList(
                header = {
                    t("list.bindings")
                        .withColor(ChatFormatting.RED)
                        .bold(true)
                },
                items = {
                    ModuleManager.filter { !it.bind.isUnbound }
                },
                eachRow = { _, module ->
                    val bind = module.bind
                    "\u2B25 ".asText()
                        .withStyle(ChatFormatting.BLUE)
                        .append(
                            markAsError("[\u2715] ")
                                .onHover(
                                    HoverEvent.ShowText(
                                        "Unbind ".asText().append(variable(module.name))
                                    )
                                )
                                .onClickRun {
                                    runCatching {
                                        this@binds.handleRemoveBind(setOf(module))
                                    }.onFailure(CommandExecutor::handleExceptions)
                                }
                        )
                        .append(highlight(module.name).copyable())
                        .append(regular(": "))
                        .append(bind.renderText())
                }
            )
            literal("remove") {
                argument(
                    "modules",
                    MultiSelectArgumentType(
                        "Module",
                        ModuleManager,
                        predicate = { !it.bind.isUnbound },
                        nameOf = ClientModule::name,
                    ),
                ) { modules ->
                    exec { ctx ->
                        handleRemoveBind(ctx.get(modules))
                        1
                    }
                }
            }
            literal("add") {
                argument("module", ModuleArgumentType("module")) { module ->
                    argument("key", ClientStringArgumentType.word(), suggestions(availableInputKeys)) { key ->
                        optional(
                            "action",
                            TaggedArgumentType<InputBind.BindAction>("action"),
                            default = null,
                        ) { action ->
                            optional(
                                "modifiers",
                                MultiSelectArgumentType(
                                    "Modifier",
                                    InputBind.Modifier.entries,
                                    predicate = { true },
                                    nameOf = InputBind.Modifier::tag,
                                ),
                                default = null,
                            ) { modifiers ->
                                exec { ctx ->
                                    addBind(
                                        ctx.get(module),
                                        ctx.get(key),
                                        ctx.get(action),
                                        ctx.get(modifiers),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun CmdI18n.addBind(
        module: ClientModule,
        keyName: String,
        action: InputBind.BindAction?,
        modifiers: Set<InputBind.Modifier>?,
    ): Int {
        val resolvedAction = action ?: module.bind.action
        val resolvedModifiers = modifiers ?: module.bind.modifiers

        val bindKey = inputByName(keyName)
        if (bindKey == InputConstants.UNKNOWN) {
            throw CommandException(t("add.unknownKey"))
        }

        module.bindValue.bind(bindKey, resolvedAction, resolvedModifiers)
        ModuleClickGui.sync()
        chat(
            regular(
                t("add.moduleBound",
                    variable(module.name),
                    module.bind.renderText()
                )
            ),
            metadata = MessageMetadata(id = "Binds#${module.name}")
        )

        return 1
    }

    private fun CmdI18n.handleRemoveBind(modules: Set<ClientModule>) {
        modules.forEach { module ->
            if (module.bind.isUnbound) {
                throw CommandException(t("remove.moduleNotBound"))
            }

            module.bindValue.unbind()

            chat(
                regular(
                    t("remove.bindRemoved",
                        variable(module.name)
                    )
                ),
                metadata = MessageMetadata(id = "Binds#${module.name}")
            )
        }

        ModuleClickGui.sync()
    }

}
