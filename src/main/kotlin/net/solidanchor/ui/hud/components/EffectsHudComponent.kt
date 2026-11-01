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
import net.solidanchor.ui.hud.HudComponentTweak
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.effect.MobEffectInstance

object EffectsHudComponent : NativeHudComponent(
    "Effects", true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.RIGHT,
        horizontalOffset = 15,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 15,
    ),
    tweaks = arrayOf(HudComponentTweak.DISABLE_STATUS_EFFECT_OVERLAY),
    description = "Shows your active status effects.",
) {

    private const val LINE_HEIGHT = 10
    private const val PADDING = 3

    private fun activeEffects(): List<MobEffectInstance> = player.activeEffects.toList()

    private fun effectText(effect: MobEffectInstance): String {
        val duration = if (effect.isInfiniteDuration) "∞" else "${effect.duration / 20}s"
        return "${effect.effect.value().displayName.string} $duration"
    }

    override val guiScaledWidth: Float
        get() = ((activeEffects().maxOfOrNull { mc.font.width(effectText(it)) } ?: 0) + PADDING * 2).toFloat()

    override val guiScaledHeight: Float
        get() = (activeEffects().size * LINE_HEIGHT + PADDING * 2).toFloat()

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val effects = activeEffects()
        if (effects.isEmpty()) {
            return@handler
        }

        with(event.context) {
            val bounds = getGuiScaledBounds()

            hudPanel(bounds)
            hudBorder(bounds)

            effects.forEachIndexed { index, effect ->
                val text = effectText(effect)
                hudText(
                    text,
                    bounds.xMax.toInt() - PADDING - hudTextWidth(text),
                    bounds.yMin.toInt() + PADDING + index * LINE_HEIGHT,
                    HudStyle.TEXT,
                )
            }
        }
    }

}
