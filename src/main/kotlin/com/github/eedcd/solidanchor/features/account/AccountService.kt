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

package net.solidanchor.features.account

import net.solidanchor.config.types.list.Tagged

enum class AccountService(
    override val tag: String,
    val canJoinOnline: Boolean,
    /**
     * The simple class name the [MinecraftAccount] subclass used to have, kept so that configs written
     * by older versions keep loading.
     */
    val serialName: String,
) : Tagged {
    MICROSOFT("Microsoft", true, "MicrosoftAccount"),
    SESSION("Session", true, "SessionAccount"),
    THEALTENING("TheAltening", true, "AlteningAccount"),
    CRACKED("Cracked", false, "CrackedAccount");

    companion {
        fun bySerialName(serialName: String) = entries.firstOrNull { it.serialName == serialName }
    }

}
