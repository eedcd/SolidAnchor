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

import com.mojang.blaze3d.platform.InputConstants
import net.solidanchor.event.events.KeyboardKeyEvent
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.ModuleCategory
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.network.chat.Component
import kotlin.math.max

object TabGuiHudComponent : NativeHudComponent(
    "TabGui",
    false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.LEFT,
        horizontalOffset = 15,
        verticalAlignment = Alignment.ScreenAxisY.TOP,
        verticalOffset = 90,
    ),
    description = "Provides keyboard navigation for modules.",
) {

    private const val ROW_HEIGHT = 12f
    private const val CATEGORY_WIDTH = 72f
    private const val SCREEN_PADDING = 4

    private var selectedCategory = 0
    private var selectedModule = 0

    private val categories: List<ModuleCategory>
        get() = ModuleCategories.entries.toList()

    private fun modulesOf(category: ModuleCategory): List<ClientModule> =
        ModuleManager.filter { it.category == category }.toList()

    private fun currentModules(): List<ClientModule> =
        categories.getOrNull(selectedCategory)?.let(::modulesOf) ?: emptyList()

    override val guiScaledWidth: Float
        get() {
            val moduleWidth = currentModules().maxOfOrNull { mc.font.width(it.name) } ?: 0
            return CATEGORY_WIDTH + max(moduleWidth + SCREEN_PADDING * 2, 40)
        }

    override val guiScaledHeight: Float
        get() = max(categories.size, currentModules().size) * ROW_HEIGHT

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val categoryList = categories
        if (categoryList.isEmpty()) {
            return@handler
        }

        selectedCategory = selectedCategory.coerceIn(0, categoryList.lastIndex)
        val moduleList = modulesOf(categoryList[selectedCategory])
        selectedModule = if (moduleList.isEmpty()) {
            0
        } else {
            selectedModule.coerceIn(0, moduleList.lastIndex)
        }

        val bounds = getGuiScaledBounds()
        val left = bounds.xMin.toInt()
        val top = bounds.yMin.toInt()
        val right = bounds.xMax.toInt()
        val bottom = bounds.yMax.toInt()

        with(event.context) {
            fill(left, top, right, bottom, HudStyle.BACKGROUND)
            fill(left, top, left + 1, bottom, HudStyle.ACCENT)
            fill(left + CATEGORY_WIDTH.toInt(), top, left + CATEGORY_WIDTH.toInt() + 1, bottom, HudStyle.BORDER)

            for (index in categoryList.indices) {
                val rowY = top + (index * ROW_HEIGHT).toInt()
                if (index == selectedCategory) {
                    fill(left, rowY, left + CATEGORY_WIDTH.toInt(), rowY + ROW_HEIGHT.toInt(), HudStyle.ACCENT)
                }
                text(
                    mc.font,
                    Component.literal(categoryList[index].tag),
                    left + SCREEN_PADDING,
                    rowY + 2,
                    if (index == selectedCategory) HudStyle.TEXT else HudStyle.TEXT_DIM,
                    false,
                )
            }

            val moduleLeft = left + CATEGORY_WIDTH.toInt() + 2
            for (index in moduleList.indices) {
                val module = moduleList[index]
                val rowY = top + (index * ROW_HEIGHT).toInt()

                if (index == selectedModule) {
                    fill(moduleLeft, rowY, right, rowY + ROW_HEIGHT.toInt(), HudStyle.BACKGROUND_LIGHT)
                }

                val color = when {
                    module.enabled -> HudStyle.ACCENT
                    index == selectedModule -> HudStyle.TEXT
                    else -> HudStyle.TEXT_DIM
                }

                text(
                    mc.font,
                    Component.literal(module.name),
                    moduleLeft + SCREEN_PADDING,
                    rowY + 2,
                    color,
                    false,
                )
            }
        }
    }

    @Suppress("unused")
    private val keyboardHandler = handler<KeyboardKeyEvent> { event ->
        if (mc.gui.screen() != null) {
            return@handler
        }
        if (!event.isPressed && !event.isRepeat) {
            return@handler
        }

        when (event.key.value) {
            InputConstants.KEY_UP -> {
                val moduleList = currentModules()
                if (moduleList.isNotEmpty()) {
                    selectedModule = (selectedModule - 1 + moduleList.size) % moduleList.size
                }
            }

            InputConstants.KEY_DOWN -> {
                val moduleList = currentModules()
                if (moduleList.isNotEmpty()) {
                    selectedModule = (selectedModule + 1) % moduleList.size
                }
            }

            InputConstants.KEY_LEFT -> {
                val categoryList = categories
                if (categoryList.isNotEmpty()) {
                    selectedCategory = (selectedCategory - 1 + categoryList.size) % categoryList.size
                    selectedModule = 0
                }
            }

            InputConstants.KEY_RIGHT -> {
                val categoryList = categories
                if (categoryList.isNotEmpty()) {
                    selectedCategory = (selectedCategory + 1) % categoryList.size
                    selectedModule = 0
                }
            }

            InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
                if (!event.isPressed) {
                    return@handler
                }

                val moduleList = currentModules()
                if (moduleList.isNotEmpty()) {
                    val module = moduleList[selectedModule.coerceIn(0, moduleList.lastIndex)]
                    module.enabled = !module.enabled
                }
            }
        }
    }

}
