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

@file:Suppress("FunctionName", "PropertyName", "NOTHING_TO_INLINE", "CAST_NEVER_SUCCEEDS")

package net.solidanchor.additions

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
import net.minecraft.world.entity.player.Input

interface ServerboundPlayerInputPacketAddition {
    var `solidAnchor$forceSneak`: Boolean

    fun `solidAnchor$getRawInput`(): Input
}

/**
 * Changes the return value of record component [ServerboundPlayerInputPacket.input].
 */
inline var ServerboundPlayerInputPacket.forceSneak: Boolean
    get() = (this as ServerboundPlayerInputPacketAddition).`solidAnchor$forceSneak`
    set(value) {
        (this as ServerboundPlayerInputPacketAddition).`solidAnchor$forceSneak` = value
    }

inline val ServerboundPlayerInputPacket.rawInput: Input
    get() = (this as ServerboundPlayerInputPacketAddition).`solidAnchor$getRawInput`()
