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
package net.solidanchor.features.command.commands.ingame.creative

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.solidanchor.features.command.CommandException
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.CmdI18n
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.command.brigadier.suggestions
import net.solidanchor.features.module.MinecraftShortcuts
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable
import net.solidanchor.utils.item.setInventoryItemCreative

object CommandItemStack : MinecraftShortcuts, CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("stack") {
            requires { it.isIngame }
            exec {
                stack(64)
            }
            argument("amount", IntegerArgumentType.integer(1, 64), suggestions("16", "32", "64")) { amount ->
                exec { ctx ->
                    stack(ctx.get(amount))
                }
            }
        }
    }

    private fun CmdI18n.stack(amount: Int): Int {
        if (!player.hasInfiniteMaterials()) {
            throw CommandException(t("mustBeCreative"))
        }

        val mainHandStack = player.mainHandItem
        if (mainHandStack.isEmpty) {
            throw CommandException(t("noItem"))
        }

        if (mainHandStack.count == amount) {
            chat(regular(t("hasAlreadyAmount", variable(amount.toString()))))
            return 1
        }

        mainHandStack.count = amount

        player.setInventoryItemCreative(itemStack = mainHandStack)

        chat(regular(t("amountChanged", variable(amount.toString()))))
        return 1
    }

}
