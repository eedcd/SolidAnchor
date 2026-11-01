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
package net.solidanchor.features.command.commands.ingame

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.config.ConfigSystem
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.clickablePath
import net.solidanchor.utils.client.markAsError
import net.solidanchor.utils.client.player
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.world
import net.solidanchor.utils.entity.handItems
import net.solidanchor.utils.io.clipboardSet
import net.solidanchor.utils.item.mapSavedData
import net.solidanchor.utils.item.toNativeImage
import net.minecraft.core.component.DataComponents.MAP_ID
import net.minecraft.world.level.saveddata.maps.MapId
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import java.io.File

/**
 * MapImage Command
 *
 * Renders the map currently held (main or off hand) as a 128x128 PNG image, then either
 * copies it to the system clipboard (as an actual image) or saves it under the solidanchor folder.
 * An optional filename may be given to `save`, otherwise a unique timestamped name is generated.
 */
object CommandMapImage : CommandRegistrar {

    private val dir = File(ConfigSystem.rootFolder, "map_image")

    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("map_image") {
            requires { it.isIngame }

            literal("copy") {
                exec {
                    val (_, data) = heldMapData() ?: run {
                        chat(markAsError(t("notHoldingMap")))
                        return@exec 1
                    }
                    data.toNativeImage().use { image ->
                        if (clipboardSet(image)) {
                            chat(regular(t("copy.success")))
                        } else {
                            chat(markAsError(t("copy.failed")))
                        }
                    }
                    1
                }
            }
            literal("save") {
                optional("filename", ClientStringArgumentType.string()) { filename ->
                    exec { ctx ->
                        val (mapId, data) = heldMapData() ?: run {
                            chat(markAsError(t("notHoldingMap")))
                            return@exec 1
                        }

                        data.toNativeImage().use { image ->
                            dir.mkdirs()
                            val file = dir.resolve(
                                ctx.get(filename)?.let {
                                    if (!it.endsWith(".png", ignoreCase = true)) "$it.png" else it
                                } ?: "map_${mapId.id}_${System.currentTimeMillis().toString(16)}.png"
                            )

                            image.writeToFile(file)
                            chat(regular(t("save.success")).append(clickablePath(file)))
                        }
                        1
                    }
                }
            }
        }
    }

    private fun heldMapData(): Pair<MapId, MapItemSavedData>? =
        player.handItems.firstNotNullOfOrNull { stack ->
            val mapId = stack[MAP_ID] ?: return@firstNotNullOfOrNull null
            stack.mapSavedData(world)?.let { Pair(mapId, it) }
        }

}
