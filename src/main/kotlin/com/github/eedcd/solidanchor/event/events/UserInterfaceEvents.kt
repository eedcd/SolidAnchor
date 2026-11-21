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

package net.solidanchor.event.events

import net.solidanchor.annotations.Tag
import net.solidanchor.event.CancellableEvent
import net.solidanchor.event.Event
import net.minecraft.network.chat.Component

@Tag("fps")
@Suppress("unused")
class FpsChangeEvent(val fps: Int) : Event()

@Tag("fpsLimit")
@Suppress("unused")
class FpsLimitEvent(var fps: Int) : Event()

sealed class TitleEvent : CancellableEvent() {
    sealed class TextContent : TitleEvent() {
        abstract var text: Component?
    }

    @Tag("title")
    class Title(override var text: Component?) : TextContent()

    @Tag("subtitle")
    class Subtitle(override var text: Component?) : TextContent()

    @Tag("titleFade")
    class Fade(var fadeInTicks: Int, var stayTicks: Int, var fadeOutTicks: Int) : TitleEvent()

    @Tag("clearTitle")
    class Clear(var reset: Boolean) : TitleEvent()
}

@Tag("closedCaptions")
class ClosedCaptionsEvent(val entries: Array<ClosedCaptionEntry>) : Event()

enum class ClosedCaptionDirection { NONE, LEFT, RIGHT }

data class ClosedCaptionEntry(
    val text: Component,
    val direction: ClosedCaptionDirection,
    val textColor: Int,
    val backgroundColor: Int,
)
