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
import com.github.eedcd.solidanchor.features.addon.AddonManager
import com.github.eedcd.solidanchor.features.addon.SolidAnchorAddon
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.arguments.ClientStringArgumentType
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.CmdChainScope
import com.github.eedcd.solidanchor.features.command.brigadier.CmdI18n
import com.github.eedcd.solidanchor.features.command.brigadier.CmdLiteralScope
import com.github.eedcd.solidanchor.features.command.brigadier.get
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.features.command.brigadier.suggestions
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.client.variable
import com.github.eedcd.solidanchor.utils.text.joinToText
import net.minecraft.network.chat.ComponentUtils.DEFAULT_SEPARATOR

object CommandAddon : CommandRegistrar {

    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("addon") {
            literal("list") {
                exec {
                    listAddons()
                }
            }
            literal("info") {
                addonNameArgument { name ->
                    exec { ctx ->
                        addonInfo(ctx.get(name))
                    }
                }
            }
        }
    }

    private fun CmdLiteralScope.addonNameArgument(block: CmdChainScope.ArgContinuation<String>) =
        argument(
            "name",
            ClientStringArgumentType.word(),
            suggestions(strings = { AddonManager.addons.map { it.id } }),
            block,
        )

    private fun CmdI18n.listAddons(): Int {
        val addons = AddonManager.addons

        if (addons.isEmpty()) {
            chat(regular(t("list.noAddons")))
            return 1
        }

        chat(regular(t("list.addons", addons.joinToText(DEFAULT_SEPARATOR) { variable("${it.id} (${it.version})") })))
        reportPendingRestart()
        return 1
    }

    private fun CmdI18n.addonInfo(id: String): Int {
        val addon = AddonManager[id] ?: run {
            chat(regular(t("info.notFound", variable(id))))
            return 1
        }

        chat(regular(t("info.name", variable(addon.displayName), variable(addon.version))))
        chat(regular(t("info.state", variable(addon.state.name))))

        if (addon.authors.isNotEmpty()) {
            chat(regular(t("info.authors", addon.authors.joinToText(DEFAULT_SEPARATOR, ::variable))))
        }
        if (addon.description.isNotBlank()) {
            chat(regular(t("info.about", variable(addon.description))))
        }
        addon.metadata.sources?.let { chat(regular(t("info.sources", variable(it)))) }

        chat(regular(t("info.features", variable(featureSummary(addon)))))
        return 1
    }

    private fun featureSummary(addon: SolidAnchorAddon) = buildString {
        append(addon.registeredModules.size).append(" modules, ")
        append(addon.registeredNodes.count { it.redirect == null }).append(" commands, ")
        append(addon.registeredCategories.size).append(" categories")
    }

    private fun CmdI18n.reportPendingRestart() {
        if (!AddonManager.pendingRestart) {
            return
        }

        chat(regular(t("list.restartRequired", AddonManager.restartReasons.joinToText(DEFAULT_SEPARATOR, ::variable))))
    }

}
