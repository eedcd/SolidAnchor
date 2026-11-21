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
package net.solidanchor.features.module.modules.player

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.aiming.RotationsValueGroup

/**
 * NoRotateSet module.
 *
 * Prevents the server from rotating your head.
 */
object ModuleNoRotateSet : ClientModule("NoRotateSet", ModuleCategories.PLAYER) {
    val mode = choices(
        "Mode", SilentAccept, arrayOf(
            SilentAccept, ResetRotation
        )
    ).apply { tagBy(this) }

    object ResetRotation : Mode("ResetRotation") {
        override val parent: ModeValueGroup<Mode>
            get() = mode

        val rotations = tree(RotationsValueGroup(this))
    }

    object SilentAccept : Mode("SilentAccept") {
        override val parent: ModeValueGroup<Mode>
            get() = mode
    }
}
