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

package net.solidanchor.ui.hud.components

import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment

object KeyBindsHudComponent : NativeHudComponent(
    "KeyBinds", true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.LEFT,
        horizontalOffset = 10,
        verticalAlignment = Alignment.ScreenAxisY.TOP,
        verticalOffset = 90,
    ),
    description = "Shows module key bindings, enabled modules in blue.",
) {

    private const val LINE_HEIGHT = 10
    private const val PADDING = 3
    private const val GAP = 6

    // Bound modules stay visible even when disabled, so the bind list doubles as an overview.
    private fun displayedModules(): List<ClientModule> =
        ModuleManager.filter { !it.bind.isUnbound }
            .sortedWith(compareByDescending<ClientModule> { it.enabled }.thenBy { it.name })

    private fun keyText(module: ClientModule): String = module.bind.boundKey.displayName.string

    private fun lineWidth(module: ClientModule): Int =
        mc.font.width(keyText(module)) + GAP + mc.font.width(module.name)

    override val guiScaledWidth: Float
        get() = ((displayedModules().maxOfOrNull { lineWidth(it) } ?: 0) + PADDING * 2).toFloat()

    override val guiScaledHeight: Float
        get() = (displayedModules().size * LINE_HEIGHT + PADDING * 2).toFloat()

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val modules = displayedModules()
        if (modules.isEmpty()) {
            return@handler
        }

        with(event.context) {
            val bounds = getGuiScaledBounds()

            hudPanel(bounds)
            hudBorder(bounds)

            modules.forEachIndexed { index, module ->
                val x = bounds.xMin.toInt() + PADDING
                val y = bounds.yMin.toInt() + PADDING + index * LINE_HEIGHT
                val key = keyText(module)
                val color = if (module.enabled) HudStyle.ACCENT else HudStyle.TEXT_DIM

                hudText(key, x, y, color)
                hudText(module.name, x + hudTextWidth(key) + GAP, y, color)
            }
        }
    }

}
