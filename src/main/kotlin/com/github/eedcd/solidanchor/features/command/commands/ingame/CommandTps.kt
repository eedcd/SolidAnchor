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
package com.github.eedcd.solidanchor.features.command.commands.ingame

import com.mojang.brigadier.CommandDispatcher
import com.github.eedcd.solidanchor.features.command.CommandRegistrar
import com.github.eedcd.solidanchor.features.command.brigadier.ClientCommandSource
import com.github.eedcd.solidanchor.features.command.brigadier.register
import com.github.eedcd.solidanchor.utils.client.MessageMetadata
import com.github.eedcd.solidanchor.utils.client.ServerObserver
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.regular
import com.github.eedcd.solidanchor.utils.client.variable
import com.github.eedcd.solidanchor.utils.math.roundToDecimalPlaces
import net.minecraft.network.protocol.game.ClientboundSetTimePacket

/**
 * TPS (ticks per second) Command
 *
 * Allows you to see the current TPS.
 *
 * This will not work on all servers as some servers modify the [ClientboundSetTimePacket] behavior.
 *
 * @author ccetl
 */
object CommandTps : CommandRegistrar {
    override fun register(dispatcher: CommandDispatcher<ClientCommandSource>) {
        dispatcher.register("tps") {
            requires { it.isIngame }
            exec {
                val tps = ServerObserver.tps
                chat(
                    regular(
                        t("tpsCheck",
                            variable(
                                if (tps.isNaN()) {
                                    t("nan").string
                                } else {
                                    tps.roundToDecimalPlaces(2).toString()
                                }
                            )
                        )
                    ),
                    metadata = MessageMetadata(id = "Ctps#info")
                )
                1
            }
        }
    }

}
