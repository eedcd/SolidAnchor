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

package net.solidanchor.ui.multiplayer

import net.solidanchor.features.account.AccountManager
import net.solidanchor.features.spoofer.SpooferManager
import net.solidanchor.ui.clickgui.ClickGuiScreen
import net.solidanchor.utils.client.ServerObserver
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.randomUsername
import net.minecraft.client.gui.screens.Screen
import kotlin.concurrent.thread

/**
 * Actions offered by the multiplayer and disconnected screens.
 */
object MultiplayerActions {

    /** Spoofer settings as a standalone screen, opened from the multiplayer screen. */
    fun spooferScreen(parent: Screen?): Screen = ClickGuiScreen(
        parent = parent,
        settingsOnly = true,
        initialSettings = SpooferManager,
        initialSettingsTitle = "Spoofer",
    )

    fun hasLastServer(): Boolean = ServerObserver.serverInfo != null

    fun hasAccounts(): Boolean = AccountManager.accounts.isNotEmpty()

    fun reconnect() {
        if (!hasLastServer()) {
            return
        }

        ServerObserver.reconnect()
    }

    /** Switches back to the account the client was started with and reconnects. */
    fun reconnectWithInitialSession() {
        AccountManager.restoreInitial()
        reconnect()
    }

    /** Logs into one of the saved accounts (picked at random) and reconnects. */
    fun reconnectWithRandomAccount() {
        val account = AccountManager.accounts.randomOrNull() ?: return

        // Authenticating can block, so keep it off the render thread and reconnect once it is done.
        thread(name = "reconnect-random-account", isDaemon = true) {
            AccountManager.loginDirectAccount(account)
            mc.execute { reconnect() }
        }
    }

    /** Uses a freshly generated cracked username and reconnects. */
    fun reconnectWithRandomUsername() {
        thread(name = "reconnect-random-username", isDaemon = true) {
            AccountManager.loginCrackedAccount(randomUsername())
            mc.execute { reconnect() }
        }
    }

}
