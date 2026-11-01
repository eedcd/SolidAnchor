/*
 * This file is part of SolidAnchor
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * SolidAnchor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SolidAnchor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SolidAnchor. If not, see <https://www.gnu.org/licenses/>.
 */
package net.solidanchor.features.command.commands.client

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.brigadier.CommandDispatcher
import net.ccbluex.fastutil.toEnumSet
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.features.command.arguments.ModuleArgumentType
import net.solidanchor.features.command.arguments.MultiTaggedArgumentType
import net.solidanchor.features.command.arguments.TaggedArgumentType
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.CmdI18n
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.command.brigadier.suggestions
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.modules.render.ModuleClickGui
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable
import net.solidanchor.utils.input.InputBind
import net.solidanchor.utils.input.availableInputKeys
import net.solidanchor.utils.input.bind
import net.solidanchor.utils.input.inputByName
import net.solidanchor.utils.input.renderText
import net.solidanchor.utils.input.unbind

/**
 * Bind Command
 *
 * Allows you to bind a key to a module, which means that the module will be activated when the key is pressed.
 */
object CommandBind : CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("bind") {
            argument("module", ModuleArgumentType("module")) { module ->
                argument("key", ClientStringArgumentType.word(), suggestions(availableInputKeys)) { key ->
                    optional(
                        "action",
                        TaggedArgumentType<InputBind.BindAction>("action"),
                        default = null,
                    ) { action ->
                        optional(
                            "modifiers",
                            MultiTaggedArgumentType("modifiers", InputBind.Modifier.entries, InputBind.Modifier::tag),
                            default = null,
                        ) { modifiers ->
                            exec { ctx ->
                                bind(
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

    private fun CmdI18n.bind(
        module: ClientModule,
        keyName: String,
        action: InputBind.BindAction?,
        modifiers: List<InputBind.Modifier>?,
    ): Int {
        val resolvedAction = action ?: module.bindValue.get().action
        val resolvedModifiers = modifiers?.toEnumSet() ?: module.bindValue.get().modifiers

        if (keyName.equals("none", true)) {
            module.bindValue.unbind()
            ModuleClickGui.sync()
            chat(
                regular(t("moduleUnbound", variable(module.name))),
                metadata = MessageMetadata(id = "Bind#${module.name}")
            )
            return 1
        }

        val boundKey = inputByName(keyName)
        if (boundKey == InputConstants.UNKNOWN) {
            chat(
                regular(t("keyNotFound", variable(keyName))),
                metadata = MessageMetadata(id = "Bind#${module.name}")
            )
            return 1
        }

        module.bindValue.bind(boundKey, resolvedAction, resolvedModifiers)
        ModuleClickGui.sync()
        chat(
            regular(
                t("moduleBound",
                    variable(module.name),
                    module.bind.renderText()
                )
            ),
            metadata = MessageMetadata(id = "Bind#${module.name}")
        )

        return 1
    }

}
