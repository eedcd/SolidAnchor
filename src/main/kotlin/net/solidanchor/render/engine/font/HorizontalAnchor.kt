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
enum class HorizontalAnchor(override val tag: String) : Tagged {
    START("Start") {
        override fun anchorToDrawX(x: Float, width: Float, scale: Float): Float =
            x
    },
    CENTER("Center") {
        override fun anchorToDrawX(x: Float, width: Float, scale: Float): Float =
            x - width * scale * 0.5f
    },
    END("End") {
        override fun anchorToDrawX(x: Float, width: Float, scale: Float): Float =
            x - width * scale
    };

    /**
     * @param x Anchor X position
     * @param width Unscaled text width
     * @param scale Render scale
     * @return Draw (top-left) X position
     */
    abstract fun anchorToDrawX(x: Float, width: Float, scale: Float): Float
}
