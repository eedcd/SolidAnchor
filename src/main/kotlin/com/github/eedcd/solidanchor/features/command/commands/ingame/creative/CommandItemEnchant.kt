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
import net.solidanchor.features.command.arguments.EnchantLevel
import net.solidanchor.features.command.arguments.EnchantLevelArgumentType
import net.solidanchor.features.command.arguments.render
import net.solidanchor.features.command.arguments.resourceArgument
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.command.brigadier.CmdI18n
import net.solidanchor.features.command.brigadier.get
import net.solidanchor.features.command.brigadier.register
import net.solidanchor.features.module.MinecraftShortcuts
import net.solidanchor.utils.client.MessageMetadata
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.regular
import net.solidanchor.utils.item.clearEnchantments
import net.solidanchor.utils.item.removeEnchantment
import net.solidanchor.utils.item.setInventoryItemCreative
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment

/**
 * ItemEnchant Command
 *
 * Allows you to add, remove, clear, and enchant all possible enchantments on an item.
 */
object CommandItemEnchant : MinecraftShortcuts, CommandRegistrar {
    @Suppress("detekt:LongMethod")
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("enchant") {
            requires { it.isIngame }

            literal("add") {
                argument("enchantment", resourceArgument(Registries.ENCHANTMENT)) { enchantment ->
                    optional("level", EnchantLevelArgumentType) { level ->
                        exec { ctx ->
                            runAdd(ctx.get(enchantment), ctx.get(level))
                            1
                        }
                    }
                }
            }
            literal("remove") {
                argument("enchantment", resourceArgument(Registries.ENCHANTMENT)) { enchantment ->
                    exec { ctx ->
                        val enchantmentHolder = ctx.get(enchantment)

                        creativeOrThrow()
                        val itemStack = getItemOrThrow()

                        itemStack.removeEnchantment(enchantmentHolder)

                        sendItemPacket(itemStack)
                        chat(
                            regular(
                                t("unenchantedItem",
                                    enchantmentHolder.registeredName
                                )
                            ),
                            metadata = MessageMetadata(id = "CItemEnchant#info")
                        )
                        1
                    }
                }
            }
            literal("clear") {
                exec {
                    creativeOrThrow()
                    val itemStack = getItemOrThrow()

                    itemStack.clearEnchantments()

                    sendItemPacket(itemStack)
                    1
                }
            }
            literal("all") {
                optional("level", EnchantLevelArgumentType) { level ->
                    exec { ctx ->
                        runAll(false, ctx.get(level))
                        1
                    }
                }
            }
            literal("all_possible") {
                optional("level", EnchantLevelArgumentType) { level ->
                    exec { ctx ->
                        runAll(true, ctx.get(level))
                        1
                    }
                }
            }
        }
    }

    private fun CmdI18n.runAdd(enchantmentHolder: Holder<Enchantment>, level: EnchantLevel?) {
        creativeOrThrow()
        val itemStack = getItemOrThrow()

        // An omitted level and the `max` keyword both resolve to the enchantment's maximum
        itemStack.enchant(enchantmentHolder, (level as? EnchantLevel.Explicit)?.level
            ?: enchantmentHolder.value().maxLevel)

        sendItemPacket(itemStack)
        chat(
            regular(
                t("enchantedItem",
                    enchantmentHolder.registeredName,
                    level?.render() ?: "max"
                )
            ),
            metadata = MessageMetadata(id = "CItemEnchant#info")
        )
    }

    private fun CmdI18n.runAll(onlyAcceptable: Boolean, level: EnchantLevel?) {
        creativeOrThrow()
        val itemStack = getItemOrThrow()

        enchantAll(itemStack, onlyAcceptable, (level as? EnchantLevel.Explicit)?.level)

        sendItemPacket(itemStack)
        chat(
            regular(
                t("enchantedItem",
                    if (onlyAcceptable) "all_possible" else "all",
                    level?.render() ?: "Max"
                )
            ),
            metadata = MessageMetadata(id = "CItemEnchant#info")
        )
    }

    private fun sendItemPacket(itemStack: ItemStack) {
        player.setInventoryItemCreative(itemStack = itemStack, animation = false)
    }

    private fun CmdI18n.creativeOrThrow() {
        if (!player.hasInfiniteMaterials()) {
            throw CommandException(t("mustBeCreative"))
        }
    }

    private fun CmdI18n.getItemOrThrow(): ItemStack {
        val itemStack = player.getItemInHand(InteractionHand.MAIN_HAND)

        if (itemStack.isEmpty) {
            throw CommandException(t("mustHoldItem"))
        }

        return itemStack
    }

    /**
     * Enchants every enchantment: [level] is the resolved explicit level, or `null` to
     * use each enchantment's own maximum (the legacy behavior of the `max` keyword).
     */
    private fun enchantAll(item: ItemStack, onlyAcceptable: Boolean, level: Int?) {
        world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap().forEach { enchantment ->
            if (!enchantment.value().canEnchant(item) && onlyAcceptable) {
                return@forEach
            }

            item.enchant(enchantment, level ?: enchantment.value().maxLevel)
        }
    }

}
