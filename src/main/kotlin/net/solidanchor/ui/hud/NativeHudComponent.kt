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

import net.solidanchor.features.addon.AddonApi
import net.solidanchor.render.engine.type.BoundingBox2f
import net.solidanchor.utils.render.Alignment

@AddonApi
abstract class NativeHudComponent @JvmOverloads constructor(
    name: String,
    enabled: Boolean,
    alignment: Alignment,
    tweaks: Array<HudComponentTweak> = emptyArray(),
    description: String = "",
) : HudComponent(name, enabled, alignment, tweaks, description) {

    /**
     * Width of the component in GUI-scaled units.
     */
    abstract val guiScaledWidth: Float

    /**
     * Height of the component in GUI-scaled units.
     */
    abstract val guiScaledHeight: Float

    protected fun getGuiScaledBounds(
        width: Float = guiScaledWidth,
        height: Float = guiScaledHeight,
    ): BoundingBox2f = alignment.getBounds(width, height)

}
