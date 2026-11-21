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
import com.github.eedcd.solidanchor.config.types.list.MultiChoiceListValue
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.arguments.TaggedArgumentType
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.CmdLiteralScope
import com.github.eedcd.solidanchor.features.command.brigadier.get
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.features.global.GlobalSettingsTarget
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleClickGui
import com.github.eedcd.solidanchor.lang.translation
import com.github.eedcd.solidanchor.utils.client.MessageMetadata
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.combat.Targets

/**
 * Enemy Command
 *
 * Provides subcommands for enemy configuration.
 */
object CommandTargets : CommandRegistrar {

    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("targets", aliases = listOf("target", "enemies", "enemy")) {
            literal("combat") {
                targetsChoice("category", GlobalSettingsTarget.combatChoices, "combat")
            }
            literal("visual") {
                targetsChoice("category", GlobalSettingsTarget.visualChoices, "visual")
            }
        }
    }

    private fun CmdLiteralScope.targetsChoice(
        name: String,
        targets: MultiChoiceListValue<Targets>,
        subcommand: String,
    ) {
        argument(name, TaggedArgumentType<Targets>(name) { it in targets.choices }) { category ->
            exec { ctx ->
                val entry = ctx.get(category)

                val state = targets.toggle(entry)
                val localizedState = if (state) "enabled" else "disabled"
                chat(
                    regular(
                        translation(
                            "solidanchor.command.targets.$subcommand.$localizedState",
                            entry.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                    ),
                    metadata = MessageMetadata(id = "CTargets#info")
                )

                ModuleClickGui.sync()
                1
            }
        }
    }

}
