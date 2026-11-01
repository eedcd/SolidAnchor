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
package net.solidanchor.features.command.commands.ingame.creative

import com.mojang.brigadier.CommandDispatcher
import net.solidanchor.features.command.CommandException
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.arguments.ClientStringArgumentType
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.module.MinecraftShortcuts
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.client.variable
import net.solidanchor.utils.item.setInventoryItemCreative
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID

/**
 * CommandItemSkull
 *
 * Allows you to create a player skull item with a specified name.
 */
object CommandItemSkull : MinecraftShortcuts, CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("skull") {
            requires { it.isIngame }
            argument("name", ClientStringArgumentType.word()) { name ->
                exec { ctx ->
                    if (!player.hasInfiniteMaterials()) {
                        throw CommandException(t("mustBeCreative"))
                    }

                    val skullName = ctx.get(name)

                    val itemStack = ItemStack(Items.PLAYER_HEAD)
                        .apply {
                            val profile = runCatching { UUID.fromString(skullName) }
                                .fold(
                                    onSuccess = { ResolvableProfile.createUnresolved(it) },
                                    onFailure = { ResolvableProfile.createUnresolved(skullName) }
                                )
                            DataComponentPatch.builder()
                                .set(DataComponents.PROFILE, profile)
                                .build()
                                .also { applyComponents(it) }
                        }

                    val emptySlot = player.inventory.freeSlot
                    if (emptySlot == -1) {
                        throw CommandException(t("noEmptySlot"))
                    }

                    player.setInventoryItemCreative(emptySlot, itemStack)
                    chat(regular(t("skullGiven", variable(skullName))))
                    1
                }
            }
        }
    }

}
