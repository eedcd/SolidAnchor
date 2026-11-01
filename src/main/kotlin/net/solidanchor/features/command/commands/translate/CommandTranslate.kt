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
package net.solidanchor.features.command.commands.translate

import com.google.common.collect.Sets
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.api.thirdparty.translator.TranslateLanguage
import net.solidanchor.api.thirdparty.translator.TranslationResult
import net.solidanchor.features.command.CommandException
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.command.brigadier.suggestions
import net.solidanchor.features.global.GlobalSettingsAutoTranslate
import net.solidanchor.lang.translation
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.copyable
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable

object CommandTranslate : CommandRegistrar {
    @Suppress("detekt:LongMethod", "detekt:CognitiveComplexMethod")
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("translate", aliases = listOf("tr")) {
            argument(
                "sourceLanguage",
                ClientStringArgumentType.word(),
                suggestions(Sets.union(setOf("auto"), languageCodes.keys)),
            ) { sourceLanguage ->
                argument(
                    "targetLanguage",
                    ClientStringArgumentType.word(),
                    suggestions(languageCodes.keys),
                ) { targetLanguage ->
                    argument("text", StringArgumentType.greedyString()) { text ->
                        execSuspend { ctx ->
                            val source = ctx.get(sourceLanguage)
                            val target = ctx.get(targetLanguage)
                            val input = ctx.get(text)

                            if (source.equals(target, ignoreCase = true)) {
                                throw CommandException(
                                    t("sameLanguage")
                                )
                            }

                            val result = GlobalSettingsAutoTranslate.translate(
                                TranslateLanguage.of(source),
                                TranslateLanguage.of(target),
                                input,
                            )

                            if (result is TranslationResult.Success) {
                                if (result.translation == result.origin) {
                                    throw CommandException(
                                        t("sameText")
                                    )
                                } else {
                                    chat(
                                        regular("("),
                                        variable(result.fromLanguage.literal),
                                        regular(") "),
                                        regular(result.origin)
                                            .copyable(copyContent = result.origin),
                                    )
                                    chat(
                                        regular("("),
                                        variable(result.toLanguage.literal),
                                        regular(") "),
                                        regular(result.translation)
                                            .copyable(copyContent = result.translation),
                                    )
                                }
                            } else {
                                chat(result.toResultText())
                            }
                        }
                    }
                }
            }
        }
    }

}
