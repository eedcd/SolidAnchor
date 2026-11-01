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
import net.solidanchor.config.autoconfig.AutoConfig
import net.solidanchor.features.command.CommandException
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.CmdI18n
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.minecraft.network.chat.MutableComponent

/**
 * Panic Command
 *
 * Allows you to disable all modules or modules in a specific category.
 */
object CommandPanic : CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("panic") {
            exec {
                // Omitting the category disables all non-render modules.
                panic("nonrender")
            }
            argument("category", ClientStringArgumentType.word()) { category ->
                exec { ctx ->
                    panic(ctx.get(category))
                }
            }
        }
    }

    private fun CmdI18n.panic(type: String): Int {
        var modules = ModuleManager.filter { it.running }
        val msg: MutableComponent = when (type) {
            "all" -> t("disabledAllModules")
            "nonrender" -> {
                modules = modules.filter {
                    it.category != ModuleCategories.RENDER
                }
                t("disabledAllCategoryModules",
                    t("nonRender")
                )
            }

            else -> {
                val category = ModuleCategories.byName(type)
                    ?: throw CommandException(t("categoryNotFound", type))
                modules = modules.filter { it.category == category }
                t("disabledAllCategoryModules", category.tag)
            }
        }

        runCatching {
            AutoConfig.withLoading {
                for (module in modules) {
                    module.enabled = false
                }
            }
        }.onSuccess {
            chat(regular(msg), metadata = MessageMetadata(id = "Cpanic#info"))
        }.onFailure {
            throw CommandException(t("panicFailed"))
        }

        return 1
    }

}
