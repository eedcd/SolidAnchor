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

package net.solidanchor.features.command

import com.mojang.brigadier.Command
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.solidanchor.config.ConfigSystem
import net.solidanchor.event.EventListener
import net.solidanchor.event.events.ChatSendEvent
import net.solidanchor.event.events.ClientShutdownEvent
import net.solidanchor.event.handler
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.lang.translation
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.text.asPlainText
import net.solidanchor.utils.client.bold
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.highlight
import net.solidanchor.utils.client.logger
import net.solidanchor.utils.client.markAsError
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.client.onClick
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.removeMessage
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import okio.appendingSink
import okio.buffer
import java.io.File

/**
 * Links minecraft with the command engine
 */
object CommandExecutor : EventListener {

    private val commandHistoryFile = File(ConfigSystem.rootFolder, "command_history.txt")

    @Volatile
    private var isShuttingDown: Boolean = false

    /**
     * Wraps a suspend handler into a Brigadier [Command], keeping the scheduling,
     * re-entrance guard and progress-message behavior of the previous meta-model
     * `suspendHandler`.
     *
     * @param allowParallel allow or prevent duplicated executions
     */
    fun wrapSuspend(
        allowParallel: Boolean = false,
        handler: KAsyncCommand.Handler<ClientCommandSource>,
    ): Command<ClientCommandSource> = KAsyncCommand(allowParallel, commandCoroutineScope, handler)

    /**
     * Handling exceptions for suspend handlers
     */
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (isShuttingDown && throwable is CancellationException) {
            // Client shutdown, ignored
        } else {
            handleExceptions(throwable)
        }
    }

    /**
     * Render thread scope
     */
    private val commandCoroutineScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() + coroutineExceptionHandler
    )

    internal fun handleExceptions(e: Throwable) {
        when (e) {
            is CommandException -> {
                mc.gui.hud.chat.removeMessage("CommandManager#error")
                val data = MessageMetadata(id = "CommandManager#error", remove = false)
                chat(e.text.withStyle(ChatFormatting.RED), metadata = data)

                if (e.usageInfo.isNotEmpty()) {
                    chat(highlight("Usage: ").bold(true), metadata = data)

                    // Zip the usage info together, e.g.
                    // ⬥ .friend add <name> [<alias>]
                    // ⬥ .friend remove <name>
                    for (usage in e.usageInfo) {
                        val prefix = CommandManager.GlobalSettings.prefix
                        val text = regular("")
                            .append("\u2B25 ".asPlainText(ChatFormatting.BLUE))
                            .append(regular(prefix))
                            .append(usage)
                            .onClick(ClickEvent.SuggestCommand(prefix + usage.string))

                        chat(text, metadata = data)
                    }
                }
            }
            else -> {
                chat(
                    markAsError(
                        translation(
                            "solidanchor.commandManager.exceptionOccurred",
                            e.javaClass.simpleName ?: "Class name missing", e.message ?: "No message"
                        )
                    ),
                    metadata = MessageMetadata(id = "CommandManager#error")
                )
                logger.error("An exception occurred while executing a command", e)
            }
        }
    }

    @Suppress("unused")
    private val shutdownHandler = handler<ClientShutdownEvent> {
        isShuttingDown = true
        commandCoroutineScope.cancel()
    }

    /**
     * Handles command execution
     */
    @Suppress("unused")
    private val chatEventHandler = handler<ChatSendEvent>(priority = EventPriorityConvention.FIRST_PRIORITY) {
        if (!it.message.startsWith(CommandManager.GlobalSettings.prefix)) {
            return@handler
        }

        val commandBody = it.message.substring(CommandManager.GlobalSettings.prefix.length)
        try {
            CommandManager.execute(commandBody)
        } catch (e: Throwable) {
            handleExceptions(e)
        } finally {
            it.cancelEvent()
        }

        commandHistoryFile.appendingSink().buffer().use { sink ->
            sink.writeUtf8(commandBody)
                .writeByte('\n'.code)
        }
    }
}
