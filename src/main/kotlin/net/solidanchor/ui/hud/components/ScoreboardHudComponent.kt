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
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.ui.hud.HudComponentTweak
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerScoreEntry

object ScoreboardHudComponent : NativeHudComponent(
    "Scoreboard",
    true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.LEFT,
        horizontalOffset = 15,
        verticalAlignment = Alignment.ScreenAxisY.CENTER_TRANSLATED,
        verticalOffset = 0,
    ),
    tweaks = arrayOf(HudComponentTweak.DISABLE_SCOREBOARD),
    description = "Displays the current server scoreboard.",
) {

    private const val PADDING = 4
    private const val LINE_HEIGHT = 9
    private const val SCORE_GAP = 6

    private val SCORE_COLOR = 0xFFFF5555.toInt()

    private enum class Show(override val tag: String) : Tagged {
        HEADER("Header"),
        NAME("Name"),
        SCORE("Score"),
    }

    private data class Entry(val name: String?, val score: String?, val header: Boolean)

    private val show by multiEnumChoice("Show", Show.HEADER, Show.NAME, Show.SCORE, canBeNone = false)
    private val replaceRegex by text("ReplaceRegex", "")
    private val replaceWith by text("ReplaceWith", "")

    override val guiScaledWidth: Float
        get() = guiWidth(lines())

    override val guiScaledHeight: Float
        get() = guiHeight(lines())

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val lines = lines()
        if (lines.isEmpty()) {
            return@handler
        }

        val context = event.context
        val bounds = getGuiScaledBounds(guiWidth(lines), guiHeight(lines))

        context.hudPanel(bounds)
        context.hudBorder(bounds)

        var y = (bounds.yMin + PADDING).toInt()
        val left = (bounds.xMin + PADDING).toInt()
        val right = (bounds.xMax - PADDING).toInt()

        for (line in lines) {
            line.name?.let {
                context.hudText(
                    it,
                    left,
                    y,
                    if (line.header) HudStyle.ACCENT else HudStyle.TEXT,
                    true,
                )
            }

            line.score?.let {
                context.hudText(it, right - mc.font.width(it), y, SCORE_COLOR, true)
            }

            y += LINE_HEIGHT
        }
    }

    private fun lines(): List<Entry> {
        val objective = mc.level?.scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()
        val shown = show
        val result = ArrayList<Entry>()

        if (Show.HEADER in shown) {
            result += Entry(applyReplacement(objective.displayName.string), null, true)
        }

        val scores = objective.scoreboard.listPlayerScores(objective)
            .filter { !it.isHidden() }
            .sortedWith(compareByDescending<PlayerScoreEntry> { it.value() }.thenBy { it.owner() })

        for (score in scores) {
            result += Entry(
                if (Show.NAME in shown) applyReplacement(score.ownerName().string) else null,
                if (Show.SCORE in shown) score.value().toString() else null,
                false,
            )
        }

        return result
    }

    private fun guiWidth(lines: List<Entry>): Float {
        if (lines.isEmpty()) {
            return 0F
        }

        val width = lines.maxOfOrNull { lineWidth(it) } ?: 0
        return (width + PADDING * 2).toFloat()
    }

    private fun guiHeight(lines: List<Entry>): Float {
        if (lines.isEmpty()) {
            return 0F
        }

        return (lines.size * LINE_HEIGHT + PADDING * 2).toFloat()
    }

    private fun lineWidth(line: Entry): Int {
        val nameWidth = line.name?.let { mc.font.width(it) } ?: 0
        val scoreWidth = line.score?.let { mc.font.width(it) } ?: 0
        val gap = if (line.name != null && line.score != null) SCORE_GAP else 0
        return nameWidth + gap + scoreWidth
    }

    private fun applyReplacement(text: String): String {
        val pattern = replaceRegex
        if (pattern.isEmpty()) {
            return text
        }

        return runCatching { Regex(pattern).replace(text, replaceWith) }.getOrDefault(text)
    }

}
