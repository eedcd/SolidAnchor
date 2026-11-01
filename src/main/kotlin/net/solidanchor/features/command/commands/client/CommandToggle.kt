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
package net.solidanchor.features.command.commands.client

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.ModuleArgumentType
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.module.ClientModule
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable

/**
 * Toggle Command
 *
 * Allows you to enable or disable a specific module.
 */
object CommandToggle : CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("toggle", aliases = listOf("t")) {
            argument("module", ModuleArgumentType("module")) { module ->
                exec { ctx ->
                    val toggledModule: ClientModule = ctx.get(module)

                    val isEnabled = !toggledModule.enabled
                    toggledModule.enabled = isEnabled
                    chat(
                        regular(
                            t("moduleToggled",
                                variable(toggledModule.name),
                                variable(
                                    if (isEnabled) {
                                        t("enabled")
                                    } else {
                                        t("disabled")
                                    }
                                )
                            )
                        ),
                        metadata = MessageMetadata(id = "CToggle#success${toggledModule.name}")
                    )
                    1
                }
            }
        }
    }

}
