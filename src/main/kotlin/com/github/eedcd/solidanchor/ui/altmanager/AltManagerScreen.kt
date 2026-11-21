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

package com.github.eedcd.solidanchor.ui.altmanager

import com.mojang.blaze3d.platform.InputConstants
import com.github.eedcd.solidanchor.event.EventListener
import com.github.eedcd.solidanchor.event.events.AccountManagerAdditionResultEvent
import com.github.eedcd.solidanchor.features.account.AccountManager
import com.github.eedcd.solidanchor.utils.client.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
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

    private lateinit var usernameField: EditBox

    private var statusMessage = ""
    private var statusIsError = false

    private val eventOwner = object : EventListener {}

    private val additionHook = eventOwner.on(AccountManagerAdditionResultEvent::class.java) { event ->
        val error = event.error
        val username = event.username
        if (error != null || username != null) {
            mc.execute {
                statusMessage = error ?: "Added account $username"
                statusIsError = error != null
            }
        }
    }

    private val accounts get() = AccountManager.accounts

    override fun init() {
        listW = (width - 20).toFloat()
        listH = (height - 120).coerceAtLeast(40).toFloat()
        listScroll = listScroll.coerceIn(0F, maxScroll())

        val previousUsername = if (::usernameField.isInitialized) usernameField.value else ""

        val fieldX = 10
        val fieldY = height - 72
        val fieldW = 160
        val fieldH = 18

        usernameField = EditBox(font, fieldX, fieldY, fieldW, fieldH, Component.literal("Username"))
        usernameField.setMaxLength(16)
        usernameField.value = previousUsername
        addRenderableWidget(usernameField)

        addRenderableWidget(
            Button.builder(Component.literal("Add Cracked")) { addCrackedAccount() }
                .pos(fieldX + fieldW + 6, fieldY)
                .size(90, fieldH)
                .build(),
        )
        addRenderableWidget(
            Button.builder(Component.literal("Microsoft Login")) {
                statusMessage = "Opening the Microsoft sign-in page in your browser..."
                statusIsError = false
                AccountManager.newMicrosoftAccountViaBrowser()
            }
                .pos(fieldX, fieldY + fieldH + 6)
                .size(120, fieldH)
                .build(),
        )
        addRenderableWidget(
            Button.builder(Component.literal("Restore Initial")) { AccountManager.restoreInitial() }
                .pos(fieldX + 126, fieldY + fieldH + 6)
                .size(120, fieldH)
                .build(),
        )
        addRenderableWidget(
            Button.builder(Component.literal("Back")) { mc.gui.setScreen(null) }
                .pos(width - 70, fieldY + fieldH + 6)
                .size(60, fieldH)
                .build(),
        )
    }

    private fun addCrackedAccount() {
        val username = usernameField.value.trim()
        if (username.isEmpty()) {
            return
        }
        AccountManager.newCrackedAccount(username)
        usernameField.value = ""
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

        listScroll = listScroll.coerceIn(0F, maxScroll())

        val pendingCode = AccountManager.pendingMicrosoftLoginCode
        val message = if (pendingCode != null) "Enter code $pendingCode at microsoft.com/link" else statusMessage
        if (message.isNotEmpty()) {
            context.text(font, Component.literal(message), 10, height - 24, if (statusIsError) ERROR else TEXT_DIM, false)
        }

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

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        if (super.mouseClicked(click, doubled)) {
            return true
        }

        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            for (hit in hits) {
                if (hit.contains(click.x, click.y)) {
                    hit.action()
                    return true
                }
            }
        }

        return false
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
        if (super.keyPressed(input)) {
            return true
        }

        if (input.key == InputConstants.KEY_ESCAPE) {
            mc.gui.setScreen(null)
            return true
        }

        return false
    }

    override fun removed() {
        additionHook.close()
        super.removed()
    }

    override fun isPauseScreen() = false

    private companion object {
        const val ROW_HEIGHT = 16F

        val BACKGROUND = 0xC0101014.toInt()
        val PANEL_BG = 0xE01A1A20.toInt()
        val ROW_HOVER = 0xFF30303A.toInt()
        val ACCENT = 0xFF5C9DFF.toInt()
        val TEXT = 0xFFE8E8EC.toInt()
        val TEXT_DIM = 0xFF9A9AA6.toInt()
        val ERROR = 0xFFFF6B6B.toInt()
    }

}
