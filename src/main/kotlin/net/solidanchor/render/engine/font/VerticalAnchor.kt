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

package net.solidanchor.render.engine.font

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.addon.AddonApi

@AddonApi
enum class VerticalAnchor(override val tag: String) : Tagged {
    TOP("Top") {
        override fun anchorToDrawY(y: Float, height: Float, scale: Float): Float =
            y
    },
    MIDDLE("Middle") {
        override fun anchorToDrawY(y: Float, height: Float, scale: Float): Float =
            y - height * scale * 0.5f
    },
    BOTTOM("Bottom") {
        override fun anchorToDrawY(y: Float, height: Float, scale: Float): Float =
            y - height * scale
    };

    /**
     * @param y Anchor Y position
     * @param height Unscaled font height
     * @param scale Render scale
     * @return Draw (top-left) Y position
     */
    abstract fun anchorToDrawY(y: Float, height: Float, scale: Float): Float
}
