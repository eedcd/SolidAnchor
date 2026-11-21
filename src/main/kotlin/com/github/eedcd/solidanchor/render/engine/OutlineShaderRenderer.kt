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

package net.solidanchor.render.engine

import net.solidanchor.render.ClientRenderPipelines
import net.solidanchor.utils.client.inGame

/**
 * For [net.solidanchor.features.module.modules.render.ModuleBlockESP] outline mode.
 *
 * @see net.solidanchor.event.events.DrawOutlinesEvent
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
