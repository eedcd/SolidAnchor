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

import net.solidanchor.event.events.ClosedCaptionEntry
import net.solidanchor.event.events.ClosedCaptionsEvent
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.ui.hud.HudComponentTweak
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment

object ClosedCaptionsHudComponent : NativeHudComponent(
    "ClosedCaptions",
    false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.RIGHT,
        horizontalOffset = 2,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 40,
    ),
    tweaks = arrayOf(HudComponentTweak.DISABLE_SUBTITLE_OVERLAY),
    description = "Displays Minecraft's closed captions (Settings -> Accessibility Settings -> Closed Captions).",
) {

    private var captionEntries: Array<ClosedCaptionEntry> = emptyArray()

    override val guiScaledWidth: Float
        get() = (captionEntries.maxOfOrNull { mc.font.width(it.text) } ?: 0).toFloat()

    override val guiScaledHeight: Float
        get() {
            if (captionEntries.isEmpty()) {
                return 0F
            }

            return (captionEntries.size * mc.font.lineHeight + captionEntries.size - 1).toFloat()
        }

    val captionsHandler = handler<ClosedCaptionsEvent> { captionEntries = it.entries }

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val entries = captionEntries
        if (entries.isEmpty()) {
            return@handler
        }

        val context = event.context
        val bounds = getGuiScaledBounds()

        var y = (bounds.yMax - mc.font.lineHeight).toInt()

        for (entry in entries) {
            context.textWithBackdrop(
                mc.font,
                entry.text,
                (bounds.xMax - mc.font.width(entry.text)).toInt(),
                y,
                entry.textColor,
                entry.backgroundColor,
            )
            y -= mc.font.lineHeight + 1
        }
    }

}
