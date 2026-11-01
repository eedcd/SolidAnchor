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
package net.solidanchor.features.module.modules.misc.antibot.modes

import net.ccbluex.fastutil.objectHashSetOf
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.event.waitTicks
import net.solidanchor.features.module.modules.misc.antibot.ModuleAntiBot.isADuplicate
import net.solidanchor.features.module.modules.misc.antibot.ModuleAntiBot.isGameProfileUnique
import net.solidanchor.utils.entity.armorItems
import net.solidanchor.utils.item.isPlayerArmor
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.UUID

object MatrixAntiBotMode : AntiBotMode("Matrix") {

    private val suspectList = objectHashSetOf<UUID>()
    private val botList = objectHashSetOf<UUID>()

    val packetHandler = handler<PacketEvent> {
        when (val packet = it.packet) {
            is ClientboundPlayerInfoUpdatePacket -> mc.execute {
                for (entry in packet.newEntries()) {
                    val profile = entry.profile ?: continue

                    if (entry.latency < 2 || profile.properties?.isEmpty == false || isGameProfileUnique(profile)) {
                        continue
                    }

                    if (isADuplicate(profile)) {
                        botList.add(entry.profileId)
                        continue
                    }

                    suspectList.add(entry.profileId)
                }
            }

            is ClientboundPlayerInfoRemovePacket -> mc.execute {
                val uuids = packet.profileIds
                suspectList.removeAll(uuids)
                botList.removeAll(uuids)
            }
        }
    }

    val repeatable = tickHandler {
        if (suspectList.isEmpty()) {
            return@tickHandler
        }

        for (entity in world.players()) {
            if (!suspectList.contains(entity.uuid)) {
                continue
            }

            var armor: Array<ItemStack>? = null

            if (!isFullyArmored(entity)) {
                armor = entity.armorItems
                waitTicks(1)
            }

            if ((isFullyArmored(entity) || updatesArmor(entity, armor)) && entity.gameProfile.properties.isEmpty) {
                botList.add(entity.uuid)
            }

            suspectList.remove(entity.uuid)
        }
    }

    private fun isFullyArmored(entity: Player): Boolean {
        return entity.armorItems.all { stack ->
            stack.isPlayerArmor && stack.isEnchanted
        }
    }

    /**
     * Matrix spawns its bot with a random set of armor but then instantly and silently gets a new set,
     * therefore somewhat tricking the client that the bot already had the new armor.
     *
     * With the help of at least 1 tick of waiting time, this function patches this "trick".
     */
    private fun updatesArmor(entity: Player, prevArmor: Array<ItemStack>?): Boolean {
        return !prevArmor.contentEquals(entity.armorItems)
    }

    override fun isBot(entity: Player): Boolean {
        return botList.contains(entity.uuid)
    }

    override fun reset() {
        suspectList.clear()
        botList.clear()
    }
}
