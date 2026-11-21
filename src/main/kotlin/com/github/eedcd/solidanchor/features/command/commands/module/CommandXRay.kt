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
package net.solidanchor.features.command.commands.module

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.features.command.CommandException
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.resourceArgument
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.command.preset.pagedList
import net.solidanchor.features.module.modules.render.ModuleXRay
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.bold
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.copyable
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable
import net.solidanchor.utils.client.withColor
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries

/**
 * XRay Command
 *
 * Allows you to add, remove, list, clear, and reset blocks for the XRay module.
 *
 * Module: [ModuleXRay]
 */
object CommandXRay : CommandRegistrar {
    @Suppress("detekt:LongMethod")
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("xray") {
            literal("add") {
                argument("block", resourceArgument(Registries.BLOCK)) { block ->
                    exec { ctx ->
                        val addedBlock = ctx.get(block).value()
                        if (!ModuleXRay.blocks.add(addedBlock)) {
                            throw CommandException(t("add.blockIsPresent", addedBlock.name))
                        }

                        chat(
                            regular(t("add.blockAdded", addedBlock.name)),
                            metadata = MessageMetadata(id = "CXRay#info")
                        )
                        1
                    }
                }
            }
            literal("remove") {
                argument("block", resourceArgument(Registries.BLOCK)) { block ->
                    exec { ctx ->
                        val removedBlock = ctx.get(block).value()
                        if (!ModuleXRay.blocks.remove(removedBlock)) {
                            throw CommandException(t("remove.blockNotFound", removedBlock.name))
                        }

                        chat(
                            regular(t("remove.blockRemoved", removedBlock.name)),
                            metadata = MessageMetadata(id = "CXRay#info")
                        )
                        1
                    }
                }
            }
            pagedList(
                header = {
                    t("list.list")
                        .withColor(ChatFormatting.RED)
                        .bold(true)
                },
                items = {
                    ModuleXRay.blocks.sortedBy { it.descriptionId }
                },
                eachRow = { _, block ->
                    regular("\u2B25 ")
                        .append(variable(block.name).copyable())
                        .append(regular(" ("))
                        .append(variable(BuiltInRegistries.BLOCK.getKey(block).toString()).copyable())
                        .append(regular(")"))
                }
            )

            literal("clear") {
                exec {
                    ModuleXRay.blocks.clear()
                    chat(
                        t("clear.blocksCleared"),
                        metadata = MessageMetadata(id = "CXRay#global")
                    )
                    1
                }
            }
            literal("reset") {
                exec {
                    ModuleXRay.applyDefaults()
                    chat(
                        regular(t("reset.blocksReset")),
                        metadata = MessageMetadata(id = "CXRay#global")
                    )
                    1
                }
            }
        }
    }

}
