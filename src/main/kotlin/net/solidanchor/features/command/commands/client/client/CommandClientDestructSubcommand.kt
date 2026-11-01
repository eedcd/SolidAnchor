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

import net.solidanchor.features.command.arguments.BooleanArgumentType
import net.solidanchor.features.command.brigadier.CmdLiteralScope
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.misc.SelfDestruct.destructClient
import net.solidanchor.features.misc.SelfDestruct.wipeClient
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.markAsError
import net.solidanchor.utils.client.regular

object CommandClientDestructSubcommand {
    fun CmdLiteralScope.destruct() {
        literal("destruct") {
            optional("confirm", BooleanArgumentType("confirm"), default = false) { confirm ->
                optional("wipe", BooleanArgumentType("wipe"), default = null) { wipe ->
                    exec { ctx ->
                        destruct(ctx.get(confirm), ctx.get(wipe) == true)
                    }
                }
            }
        }
    }

    private fun destruct(confirm: Boolean, wipe: Boolean): Int {
        if (!confirm) {
            chat(
                regular("Do you really want to destruct the client? " +
                    "If so, type the command again with 'yes' at the end.")
            )
            chat(markAsError("If you also want to wipe the client, add an additional 'yes' at the end."))
            chat(regular("For full destruct: .client destruct yes yes"))
            chat(regular("For temporary destruct: .client destruct yes"))
            return 1
        }

        chat(regular("SolidAnchor is being destructed from your client..."))
        if (!wipe) {
            chat(
                regular("WARNING: You have not wiped the client (missing wipe parameter) - therefore " +
                    "some files may still be present!")
            )
        }

        destructClient()
        chat(
            regular("SolidAnchor has been destructed from your client. " +
                "You can clear your chat using F3+D. If wipe was enabled, the chat will be cleared automatically.")
        )

        if (wipe) {
            chat(regular("Wiping client..."))
            // Runs on a separate thread to prevent blocking the main thread and
            // repeating the process when required
            wipeClient()
        }
        return 1
    }
}
