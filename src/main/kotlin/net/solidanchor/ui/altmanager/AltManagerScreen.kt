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

package net.solidanchor.ui.altmanager

import com.mojang.blaze3d.platform.InputConstants
import net.solidanchor.features.account.AccountManager
import net.solidanchor.utils.client.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

/**
 * Native replacement for the former web based Alt Manager.
 */
class AltManagerScreen : Screen(Component.literal("Alt Manager")) {

    private class HitArea(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val action: () -> Unit,
    ) {
        fun contains(x: Double, y: Double) = x >= x1 && x <= x2 && y >= y1 && y <= y2
    }

    private val hits = ArrayList<HitArea>()

    private var listScroll = 0F
    private var listX = 10F
    private var listY = 40F
    private var listW = 0F
    private var listH = 0F

    private var usernameBuffer = ""
    private var editingUsername = false

    private val accounts get() = AccountManager.accounts

    override fun init() {
        listW = (width - 20).toFloat()
        listH = (height - 120).coerceAtLeast(40).toFloat()
        listScroll = listScroll.coerceIn(0F, maxScroll())
    }

    private fun maxScroll(): Float = (accounts.size * ROW_HEIGHT - listH).coerceAtLeast(0F)

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        hits.clear()

        context.fill(0, 0, width, height, BACKGROUND)
        context.centeredText(font, Component.literal("Alt Manager"), width / 2, 10, ACCENT)
        context.centeredText(
            font,
            Component.literal("Current: ${mc.user.name}"),
            width / 2,
            22,
            TEXT_DIM,
        )

        context.fill(listX.toInt(), listY.toInt(), (listX + listW).toInt(), (listY + listH).toInt(), PANEL_BG)

        var rowY = listY + 2F - listScroll
        for ((index, account) in accounts.withIndex()) {
            val rowTop = rowY
            if (rowTop + ROW_HEIGHT >= listY && rowTop <= listY + listH) {
                drawAccountRow(context, index, account.username, account.service.tag, account.favorite, rowTop, mouseX, mouseY)
            }
            rowY += ROW_HEIGHT
        }

        if (accounts.isEmpty()) {
            context.centeredText(
                font,
                Component.literal("No accounts yet"),
                (listX + listW / 2).toInt(),
                (listY + listH / 2).toInt(),
                TEXT_DIM,
            )
        }

        drawBottomControls(context, mouseX, mouseY)

        listScroll = listScroll.coerceIn(0F, maxScroll())

        super.extractRenderState(context, mouseX, mouseY, partialTick)
    }

    private fun drawAccountRow(
        context: GuiGraphicsExtractor,
        index: Int,
        name: String,
        service: String,
        favorite: Boolean,
        rowTop: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val rowBottom = rowTop + ROW_HEIGHT - 1
        val hovered = mouseX.toDouble() in listX.toDouble()..(listX + listW).toDouble() &&
            mouseY.toDouble() in rowTop.toDouble()..rowBottom.toDouble()

        if (hovered) {
            context.fill(listX.toInt(), rowTop.toInt(), (listX + listW).toInt(), rowBottom.toInt(), ROW_HOVER)
        }

        context.text(font, Component.literal(name), (listX + 6).toInt(), (rowTop + 3).toInt(), TEXT, false)
        context.text(
            font,
            Component.literal(service),
            (listX + 6 + font.width(name) + 6).toInt(),
            (rowTop + 3).toInt(),
            TEXT_DIM,
            false,
        )

        val starX = listX + listW - 50F
        val removeX = listX + listW - 18F

        context.text(font, Component.literal(if (favorite) "★" else "☆"), starX.toInt(), (rowTop + 3).toInt(), ACCENT, false)
        context.text(font, Component.literal("x"), removeX.toInt(), (rowTop + 3).toInt(), TEXT_DIM, false)

        hits += HitArea(listX, rowTop, starX - 4, rowBottom, {
            AccountManager.loginAccount(index)
        })
        hits += HitArea(starX - 4, rowTop, removeX - 4, rowBottom, {
            if (favorite) AccountManager.unfavoriteAccount(index) else AccountManager.favoriteAccount(index)
        })
        hits += HitArea(removeX - 4, rowTop, listX + listW, rowBottom, {
            AccountManager.removeAccount(index)
        })
    }

    private fun drawBottomControls(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val fieldX = 10F
        val fieldY = height - 72F
        val fieldW = 160F
        val fieldH = 18F

        context.fill(fieldX.toInt(), fieldY.toInt(), (fieldX + fieldW).toInt(), (fieldY + fieldH).toInt(), PANEL_BG)
        context.fill(
            fieldX.toInt(),
            fieldY.toInt(),
            (fieldX + fieldW).toInt(),
            (fieldY + fieldH).toInt(),
            if (editingUsername) ACCENT else BORDER,
        )
        context.fill(
            (fieldX + 1).toInt(),
            (fieldY + 1).toInt(),
            (fieldX + fieldW - 1).toInt(),
            (fieldY + fieldH - 1).toInt(),
            PANEL_BG,
        )
        context.text(
            font,
            Component.literal(usernameBuffer.ifEmpty { "Username" }),
            (fieldX + 4).toInt(),
            (fieldY + 5).toInt(),
            if (usernameBuffer.isEmpty()) TEXT_DIM else TEXT,
            false,
        )
        hits += HitArea(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, { editingUsername = true })

        drawButton(context, "Add Cracked", fieldX + fieldW + 6, fieldY, 90F, fieldH, mouseX, mouseY) {
            val username = usernameBuffer.trim()
            if (username.isNotEmpty()) {
                AccountManager.newCrackedAccount(username)
                usernameBuffer = ""
            }
        }

        val row2Y = fieldY + fieldH + 6F
        drawButton(context, "Microsoft Login", fieldX, row2Y, 120F, fieldH, mouseX, mouseY) {
            AccountManager.newMicrosoftAccountViaBrowser()
        }
        drawButton(context, "Restore Initial", fieldX + 126, row2Y, 120F, fieldH, mouseX, mouseY) {
            AccountManager.restoreInitial()
        }
        drawButton(context, "Back", width - 70F, row2Y, 60F, fieldH, mouseX, mouseY) {
            mc.gui.setScreen(null)
        }
    }

    private fun drawButton(
        context: GuiGraphicsExtractor,
        label: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        mouseX: Int,
        mouseY: Int,
        action: () -> Unit,
    ) {
        val hovered = mouseX.toDouble() in x.toDouble()..(x + w).toDouble() &&
            mouseY.toDouble() in y.toDouble()..(y + h).toDouble()

        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), if (hovered) PANEL_HEADER else PANEL_BG)
        context.fill(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt(), BORDER)
        context.fill((x + 1).toInt(), (y + 1).toInt(), (x + w - 1).toInt(), (y + h - 1).toInt(), if (hovered) PANEL_HEADER else PANEL_BG)
        context.centeredText(font, Component.literal(label), (x + w / 2).toInt(), (y + 5).toInt(), TEXT)

        hits += HitArea(x, y, x + w, y + h, action)
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            for (hit in hits) {
                if (hit.contains(click.x, click.y)) {
                    hit.action()
                    return true
                }
            }
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (mouseX in listX.toDouble()..(listX + listW).toDouble() &&
            mouseY in listY.toDouble()..(listY + listH).toDouble()
        ) {
            listScroll = (listScroll - verticalAmount.toFloat() * 12F).coerceIn(0F, maxScroll())
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (editingUsername) {
            when (input.key) {
                InputConstants.KEY_ESCAPE -> editingUsername = false
                InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
                    val username = usernameBuffer.trim()
                    if (username.isNotEmpty()) {
                        AccountManager.newCrackedAccount(username)
                        usernameBuffer = ""
                    }
                    editingUsername = false
                }
                InputConstants.KEY_BACKSPACE -> {
                    if (usernameBuffer.isNotEmpty()) {
                        usernameBuffer = usernameBuffer.dropLast(1)
                    }
                }
            }
            return true
        }

        if (input.key == InputConstants.KEY_ESCAPE) {
            mc.gui.setScreen(null)
            return true
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        if (editingUsername) {
            val codePoint = input.codepoint()
            if (codePoint >= 32 && usernameBuffer.length < 16) {
                usernameBuffer += codePoint.toChar()
            }
            return true
        }
        return super.charTyped(input)
    }

    override fun isPauseScreen() = false

    private companion object {
        const val ROW_HEIGHT = 16F

        val BACKGROUND = 0xC0101014.toInt()
        val PANEL_BG = 0xE01A1A20.toInt()
        val PANEL_HEADER = 0xFF26262E.toInt()
        val ROW_HOVER = 0xFF30303A.toInt()
        val BORDER = 0xFF3A3A46.toInt()
        val ACCENT = 0xFF5C9DFF.toInt()
        val TEXT = 0xFFE8E8EC.toInt()
        val TEXT_DIM = 0xFF9A9AA6.toInt()
    }

}
