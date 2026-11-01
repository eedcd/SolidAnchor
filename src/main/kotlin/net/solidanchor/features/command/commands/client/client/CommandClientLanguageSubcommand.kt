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

import net.ccbluex.fastutil.mapToArray
import net.solidanchor.config.ConfigSystem
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.features.command.brigadier.CmdLiteralScope
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.suggestions
import net.solidanchor.features.global.GlobalManager
import net.solidanchor.lang.LanguageManager
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular

object CommandClientLanguageSubcommand {
    fun CmdLiteralScope.language() {
        literal("language") {
            literal("list") {
                exec {
                    chat(regular("Available languages:"))
                    chat(texts = LanguageManager.languageCodes.mapToArray { regular("-> $it") })
                    1
                }
            }
            literal("set") {
                argument(
                    "language",
                    ClientStringArgumentType.word(),
                    suggestions(LanguageManager.languageCodes),
                ) { language ->
                    exec { ctx ->
                        val code = ctx.get(language)
                        val choice = LanguageManager.languageChoiceFromCode(code)
                        if (choice == null) {
                            chat(regular("Language not found."))
                            return@exec 1
                        }

                        chat(regular("Setting language to ${choice.tag}..."))
                        LanguageManager.clientLanguage = choice

                        ConfigSystem.store(GlobalManager)
                        1
                    }
                }
            }
            literal("unset") {
                exec {
                    chat(regular("Unset override language..."))
                    LanguageManager.clientLanguage = LanguageManager.ClientLanguage.AUTO
                    ConfigSystem.store(GlobalManager)
                    1
                }
            }
        }
    }
}
