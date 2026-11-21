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

package net.solidanchor.ui.hud

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.features.addon.AddonApi
import net.solidanchor.features.module.modules.render.ModuleHud
import net.solidanchor.utils.render.Alignment
import java.util.UUID

/**
 * Represents a HUD component rendered by the client itself.
 */
@AddonApi
abstract class HudComponent @JvmOverloads constructor(
    name: String,
    enabled: Boolean,
    alignment: Alignment = Alignment.center(),
    val tweaks: Array<HudComponentTweak> = emptyArray(),
    val componentDescription: String = "",
) : ToggleableValueGroup(parent = ModuleHud, name = name, enabled = enabled) {

    val id: UUID = UUID.randomUUID()

    private val defaultAlignment = Alignment(
        alignment.horizontalAlignment,
        alignment.horizontalOffset,
        alignment.verticalAlignment,
        alignment.verticalOffset,
    )

    var zIndex by int("ZIndex", 0, 0..Int.MAX_VALUE).notAnOption()

    val alignment = tree(alignment)

    fun resetAlignment() {
        alignment.setFrom(defaultAlignment)
    }

    /**
     * Kept for add-on compatibility. Native components read their values directly while rendering,
     * so there is nothing to invalidate.
     */
    @Suppress("UNUSED_PARAMETER")
    protected fun registerComponentListen(valueGroup: ValueGroup) = Unit

}
