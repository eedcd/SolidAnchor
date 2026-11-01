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

import net.solidanchor.SolidAnchor
import net.solidanchor.features.command.brigadier.CmdLiteralScope
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable

object CommandClientInfoSubcommand {
    fun CmdLiteralScope.info() {
        literal("info") {
            exec {
                chat(
                    regular(t("info.clientName", variable(SolidAnchor.CLIENT_NAME))),
                    metadata = MessageMetadata(prefix = false)
                )
                chat(
                    regular(t("info.clientVersion", variable(SolidAnchor.clientVersion))),
                    metadata = MessageMetadata(prefix = false)
                )
                chat(
                    regular(t("info.clientAuthor", variable(SolidAnchor.CLIENT_AUTHOR))),
                    metadata = MessageMetadata(prefix = false)
                )
                1
            }
        }
    }
}
