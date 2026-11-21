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

package com.github.eedcd.solidanchor.render.engine

import com.github.eedcd.solidanchor.render.ClientRenderPipelines
import com.github.eedcd.solidanchor.utils.client.inGame

/**
 * For [com.github.eedcd.solidanchor.features.module.modules.render.ModuleBlockESP] outline mode.
 *
 * @see com.github.eedcd.solidanchor.event.events.DrawOutlinesEvent
 */
@Deprecated("Disabled this feature")
object OutlineShaderRenderer : OverlayShaderRenderer(
    "Outline",
    ClientRenderPipelines.Outline,
    useDepth = false,
) {
    override fun shouldRender(): Boolean {
        return inGame
    }
}
