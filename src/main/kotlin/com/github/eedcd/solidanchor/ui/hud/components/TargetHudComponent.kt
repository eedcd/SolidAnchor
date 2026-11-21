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
import net.solidanchor.event.events.TargetChangeEvent
import net.solidanchor.event.handler
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.entity.armorItems
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.math.toFixed
import net.solidanchor.utils.render.Alignment
import net.minecraft.world.entity.LivingEntity

object TargetHudComponent : NativeHudComponent(
    "TargetHud",
    true,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER,
        horizontalOffset = 20,
        verticalAlignment = Alignment.ScreenAxisY.CENTER_TRANSLATED,
        verticalOffset = 0,
    ),
    description = "Shows combat information about your current target.",
) {

    private const val PANEL_WIDTH = 170F
    private const val PANEL_HEIGHT = 56F
    private const val PADDING = 6
    private const val ARMOR_ICON_SIZE = 16

    private val HEALTH_COLOR = 0xFFE04040.toInt()
    private val BAR_BACKGROUND = 0x60000000

    private val showArmor by boolean("ShowArmor", true)

    private var currentTarget: LivingEntity? = null

    val targetHandler = handler<TargetChangeEvent> { currentTarget = it.target }

    override val guiScaledWidth: Float
        get() = PANEL_WIDTH

    override val guiScaledHeight: Float
        get() = PANEL_HEIGHT

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val target = currentTarget?.takeIf {
            it.isAlive && !it.isRemoved && it.level() == mc.level
        } ?: return@handler

        val context = event.context
        val bounds = getGuiScaledBounds()

        context.hudPanel(bounds)
        context.hudBorder(bounds)

        val maxHealth = target.maxHealth.coerceAtLeast(1F)
        val health = target.health.coerceIn(0F, maxHealth)
        val healthText = health.toFixed(1)

        val left = (bounds.xMin + PADDING).toInt()
        val top = (bounds.yMin + PADDING).toInt()

        context.hudText(target.name.string, left, top, HudStyle.ACCENT, true)
        context.hudText(
            healthText,
            (bounds.xMax - PADDING).toInt() - mc.font.width(healthText),
            top,
            HudStyle.TEXT,
            true,
        )

        if (showArmor) {
            var armorX = (bounds.xMax - PADDING).toInt()
            val armorY = (bounds.yMin + 20).toInt()

            for (stack in target.armorItems) {
                if (stack.isEmpty) {
                    continue
                }

                armorX -= ARMOR_ICON_SIZE
                context.item(stack, armorX, armorY)
            }
        }

        val barX = (bounds.xMin + PADDING).toInt()
        val barY = (bounds.yMin + 38).toInt()
        val barWidth = (bounds.width - PADDING * 2).toInt()
        val barHeight = 5

        context.fill(barX, barY, barX + barWidth, barY + barHeight, BAR_BACKGROUND)
        context.fill(
            barX,
            barY,
            barX + (barWidth * (health / maxHealth)).toInt(),
            barY + barHeight,
            HEALTH_COLOR,
        )

        context.hudText(
            "${player.distanceTo(target).toFixed(1)}m",
            left,
            (bounds.yMin + 46).toInt(),
            HudStyle.TEXT_DIM,
            false,
        )
    }

}
