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
import net.solidanchor.render.engine.type.BoundingBox2f
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.player
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractCraftingMenu
import net.minecraft.world.item.ItemStack

/**
 * Shows the crafting grid of the player inventory menu (2x2) and, while a crafting table screen
 * is open, its 3x3 grid. When no crafting screen is open the grids are displayed empty.
 */
object CraftingInventoryHudComponent : NativeHudComponent(
    "CraftingInventory", false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.RIGHT,
        horizontalOffset = 35,
        verticalAlignment = Alignment.ScreenAxisY.BOTTOM,
        verticalOffset = 305,
    ),
    description = "Displays the contents of your crafting grid.",
) {

    private const val SLOT_SIZE = 18
    private const val SLOT_GAP = 2
    private const val GRID_GAP = 6
    private const val ITEM_OFFSET = 1

    private const val SMALL_GRID_SIZE = 2
    private const val LARGE_GRID_SIZE = 3

    private fun gridWidth(size: Int): Int = SLOT_SIZE * size + SLOT_GAP * (size - 1)

    private val smallGridWidth: Int
        get() = gridWidth(SMALL_GRID_SIZE)

    private val largeGridWidth: Int
        get() = gridWidth(LARGE_GRID_SIZE)

    override val guiScaledWidth: Float
        get() = (smallGridWidth + GRID_GAP + largeGridWidth).toFloat()

    override val guiScaledHeight: Float
        get() = largeGridWidth.toFloat()

    /**
     * The 2x2 grid is part of [net.minecraft.world.inventory.InventoryMenu], which is always
     * available and contains the items while the inventory screen is open.
     */
    private fun playerCraftingGrid(): List<ItemStack> =
        player.inventoryMenu.inputGridSlots.map { it.item }

    /**
     * The 3x3 grid of a crafting table can only be read while its screen is open, otherwise
     * `null` is returned and an empty placeholder grid is drawn instead.
     */
    private fun openCraftingTableGrid(): List<ItemStack>? {
        val menu = (mc.gui.screen() as? AbstractContainerScreen<*>)?.menu as? AbstractCraftingMenu ?: return null
        return if (menu.gridWidth == LARGE_GRID_SIZE) {
            menu.inputGridSlots.map { it.item }
        } else {
            null
        }
    }

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val smallGrid = playerCraftingGrid()
        val largeGrid = openCraftingTableGrid()

        with(event.context) {
            val bounds = getGuiScaledBounds()
            val x = bounds.xMin.toInt()
            val y = bounds.yMin.toInt()

            drawGrid(
                x,
                y + (largeGridWidth - smallGridWidth) / 2,
                SMALL_GRID_SIZE,
                SMALL_GRID_SIZE,
                smallGrid,
            )
            drawGrid(x + smallGridWidth + GRID_GAP, y, LARGE_GRID_SIZE, LARGE_GRID_SIZE, largeGrid)
        }
    }

    private fun GuiGraphicsExtractor.drawGrid(
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        stacks: List<ItemStack>?,
    ) {
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val index = row * columns + column
                val slotX = x + column * (SLOT_SIZE + SLOT_GAP)
                val slotY = y + row * (SLOT_SIZE + SLOT_GAP)
                val slotBounds = BoundingBox2f(
                    slotX.toFloat(),
                    slotY.toFloat(),
                    (slotX + SLOT_SIZE).toFloat(),
                    (slotY + SLOT_SIZE).toFloat(),
                )

                fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, HudStyle.BACKGROUND_LIGHT)
                hudBorder(slotBounds)

                val stack = stacks?.getOrNull(index) ?: ItemStack.EMPTY
                if (!stack.isEmpty) {
                    item(stack, slotX + ITEM_OFFSET, slotY + ITEM_OFFSET)
                    itemDecorations(mc.font, stack, slotX + ITEM_OFFSET, slotY + ITEM_OFFSET, null)
                }
            }
        }
    }

}
