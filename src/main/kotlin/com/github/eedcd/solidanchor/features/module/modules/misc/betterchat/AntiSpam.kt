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

package net.solidanchor.features.module.modules.misc.betterchat

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.event.events.ChatReceiveEvent
import net.solidanchor.event.handler
import net.solidanchor.interfaces.GuiMessageAddition
import net.solidanchor.interfaces.GuiMessageLineAddition
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.text.TextBuilder
import net.solidanchor.utils.text.asPlainText
import net.minecraft.ChatFormatting
import net.minecraft.util.StringDecomposer

object AntiSpam : ToggleableValueGroup(ModuleBetterChat, "AntiSpam", true) {

    private val stack by boolean("StackMessages", false)
    private val regexFilters by regexList("Filters", linkedSetOf())

    @Suppress("unused", "CAST_NEVER_SUCCEEDS" /* succeed with mixins */)
    val chatHandler = handler<ChatReceiveEvent> { event ->
        val string = StringDecomposer.getPlainText(event.textData)

        if (regexFilters.isNotEmpty()) {
            val content = string.subSequence(string.indexOf('>') + 1, string.length).trim()

            val shouldBeRemoved = regexFilters.any {
                it.matches(content)
            }

            if (shouldBeRemoved) {
                event.cancelEvent()
                return@handler
            }
        }

        // stacks messages so that e.g., when a message is sent twice
        // it gets replaces by a new messages that has `[2]` appended
        if (stack && event.type != ChatReceiveEvent.ChatType.DISGUISED_CHAT_MESSAGE) {
            // always cancel so each message gets an ID
            event.cancelEvent()

            // appends "external" to every message id
            // so servers can't troll users with messages that
            // imitate client messages
            val id = "$string-external"

            val chatText = TextBuilder()
            val text = event.applyChatDecoration.apply(event.textData)
            chatText.add(text)

            val other = mc.gui.hud.chat.allMessages.find {
                (it as GuiMessageLineAddition).`solid_anchor$getId`() == id
            }

            var count = 1
            other?.let {
                count += (other as GuiMessageAddition).`solid_anchor$getCount`()
                chatText.add(" [$count]".asPlainText(ChatFormatting.GRAY))
            }

            val data = MessageMetadata(prefix = false, id = id, remove = true, count = count)
            chat(chatText.build(), data)
        }
    }

}
