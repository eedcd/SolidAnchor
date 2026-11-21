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
package com.github.eedcd.solidanchor.features.module.modules.misc.debugrecorder

import com.github.eedcd.solidanchor.config.ConfigSystem
import com.github.eedcd.solidanchor.config.gson.adapter.toUnderlinedString
import com.github.eedcd.solidanchor.config.gson.fileGson
import com.github.eedcd.solidanchor.config.types.group.Mode
import com.github.eedcd.solidanchor.config.types.group.ModeValueGroup
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.misc.debugrecorder.modes.AimDebugRecorder
import com.github.eedcd.solidanchor.features.module.modules.misc.debugrecorder.modes.BoxDebugRecorder
import com.github.eedcd.solidanchor.features.module.modules.misc.debugrecorder.modes.DebugCPSRecorder
import com.github.eedcd.solidanchor.features.module.modules.misc.debugrecorder.modes.GenericDebugRecorder
import com.github.eedcd.solidanchor.utils.text.asText
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.markAsError
import com.github.eedcd.solidanchor.utils.client.onClick
import com.github.eedcd.solidanchor.utils.client.onHover
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.client.underline
import com.github.eedcd.solidanchor.utils.client.variable
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import java.io.File
import java.time.LocalDateTime

object ModuleDebugRecorder : ClientModule("DebugRecorder", ModuleCategories.MISC, disableOnQuit = true) {

    init {
        // [Debug Recorder] is usually used by developers and testers and is not needed in the auto config.
        doNotIncludeAlways()
    }

    val modes = choices("Mode", GenericDebugRecorder, arrayOf(
        GenericDebugRecorder,
        DebugCPSRecorder,
        AimDebugRecorder,
        BoxDebugRecorder
    ))

    abstract class DebugRecorderMode<T>(name: String) : Mode(name) {
        override val parent: ModeValueGroup<*>
            get() = modes

        val folder = ConfigSystem.rootFolder.resolve("debug-recorder/$name").apply {
            mkdirs()
        }

        protected val packets = mutableListOf<T>()

        protected fun recordPacket(packet: T) {
            if (!this.isSelected) {
                return
            }

            packets.add(packet)
        }

        protected open val fileExtension: String get() = "json"

        protected open fun writePackets(file: File) {
            file.bufferedWriter().use { writer ->
                // No indent as default
                fileGson.toJson(this.packets, writer)
            }
        }

        override fun enable() {
            this.packets.clear()
            chat(regular("Recording "), variable(name), regular("..."))
        }

        override fun disable() {
            if (this.packets.isEmpty()) {
                chat(regular("No packets recorded."))
                return
            }

            runCatching {
                // Create parent folder
                folder.mkdirs()

                val baseName = LocalDateTime.now().toUnderlinedString()
                var file = folder.resolve("$baseName.$fileExtension")

                var idx = 0
                while (file.exists()) {
                    file = folder.resolve("${baseName}_${idx++}.$fileExtension")
                }

                writePackets(file)
                file.absolutePath
            }.onFailure {
                chat(markAsError("Failed to write log to file $it"))
            }.onSuccess { path ->
                val text = path.asText()
                    .underline(true)
                    .onHover(HoverEvent.ShowText(regular("Browse...")))
                    .onClick(ClickEvent.OpenFile(path))

                chat(regular("Log was written to "), text, regular("."))
            }

            this.packets.clear()
        }
    }
}
