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

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.NotificationEvent
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.network.chat.Component
import kotlin.math.max

object NotificationsHudComponent : NativeHudComponent(
    "Notifications",
    true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.RIGHT,
        horizontalOffset = 15,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 5,
    ),
    description = "Displays client notifications.",
) {

    private const val LIFETIME_MS = 3000L
    private const val FADE_DURATION_MS = 500L
    private const val ROW_HEIGHT = 22f
    private const val MIN_WIDTH = 140f
    private const val MAX_NOTIFICATIONS = 6

    /**
     * [NotificationEvent.Severity] itself is not [Tagged], so use a small mirror enum for the config value.
     */
    private enum class SeverityChoice(override val tag: String) : Tagged {
        INFO("INFO"),
        SUCCESS("SUCCESS"),
        ERROR("ERROR"),
        ENABLED("ENABLED"),
        DISABLED("DISABLED");

        companion object {
            fun of(severity: NotificationEvent.Severity): SeverityChoice = valueOf(severity.name)
        }
    }

    private class Notification(
        val title: String,
        val message: String,
        val severity: NotificationEvent.Severity,
        val timestamp: Long = System.currentTimeMillis(),
    )

    private val severities by multiEnumChoice<SeverityChoice>(
        "Severities",
        *SeverityChoice.entries.toTypedArray(),
        canBeNone = false,
    )

    private val notifications = ArrayDeque<Notification>()

    @Suppress("unused")
    private val notificationHandler = handler<NotificationEvent> { event ->
        if (SeverityChoice.of(event.severity) !in severities) {
            return@handler
        }

        while (notifications.size >= MAX_NOTIFICATIONS) {
            notifications.removeFirst()
        }
        notifications.addLast(Notification(event.title, event.message, event.severity))
    }

    override val guiScaledWidth: Float
        get() {
            if (notifications.isEmpty()) {
                return MIN_WIDTH
            }

            return notifications.maxOf {
                max(mc.font.width(it.title), mc.font.width(it.message)) + 12
            }.toFloat().coerceAtLeast(MIN_WIDTH)
        }

    override val guiScaledHeight: Float
        get() = (notifications.size * ROW_HEIGHT).coerceAtLeast(0f)

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val now = System.currentTimeMillis()
        notifications.removeAll { now - it.timestamp >= LIFETIME_MS }

        if (notifications.isEmpty()) {
            return@handler
        }

        val bounds = getGuiScaledBounds()

        with(event.context) {
            var rowBottom = bounds.yMax

            for (index in notifications.indices.reversed()) {
                val notification = notifications[index]
                val rowTop = rowBottom - ROW_HEIGHT

                val remaining = LIFETIME_MS - (now - notification.timestamp)
                val fade = (remaining.toFloat() / FADE_DURATION_MS).coerceIn(0f, 1f)

                val accent = severityColor(notification.severity).fade(fade)
                val left = bounds.xMin.toInt()
                val top = rowTop.toInt()
                val right = bounds.xMax.toInt()
                val bottom = rowBottom.toInt()

                fill(left, top, right, bottom, Color4b(HudStyle.BACKGROUND).fade(fade).argb)
                fill(left, top, left + 2, bottom, accent.argb)

                text(
                    mc.font,
                    Component.literal(notification.title),
                    left + 6,
                    top + 3,
                    accent.argb,
                    false,
                )
                text(
                    mc.font,
                    Component.literal(notification.message),
                    left + 6,
                    top + 12,
                    Color4b.WHITE.fade(fade).argb,
                    false,
                )

                rowBottom = rowTop
            }
        }
    }

    private fun severityColor(severity: NotificationEvent.Severity): Color4b = when (severity) {
        NotificationEvent.Severity.INFO -> Color4b(0x5C, 0x9D, 0xFF)
        NotificationEvent.Severity.SUCCESS -> Color4b(0x4C, 0xAF, 0x50)
        NotificationEvent.Severity.ERROR -> Color4b(0xE5, 0x39, 0x35)
        NotificationEvent.Severity.ENABLED -> Color4b(0x66, 0xBB, 0x6A)
        NotificationEvent.Severity.DISABLED -> Color4b(0x9E, 0x9E, 0x9E)
    }

}
