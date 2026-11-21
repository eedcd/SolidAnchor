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
package net.solidanchor.features.module.modules.combat.criticals.modes

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.BlinkPacketEvent
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.TransferOrigin
import net.solidanchor.event.handler
import net.solidanchor.features.blink.BlinkManager
import net.solidanchor.features.module.modules.combat.criticals.ModuleCriticals
import net.solidanchor.features.module.modules.combat.criticals.ModuleCriticals.wouldDoCriticalHit
import net.solidanchor.utils.combat.findEnemy
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import net.minecraft.network.protocol.game.ServerboundAttackPacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundPunchPacket
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket

object CriticalsBlink : Mode("Blink") {

    override val parent: ModeValueGroup<*>
        get() = ModuleCriticals.modes

    private val delay by intRange("Delay", 300..600, 0..1000, "ms")
    private val range by float("Range", 4.0f, 0.0f..10.0f)
    private var nextDelay = delay.random()

    var isInState = false
    private var enemyInRange = false

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        enemyInRange = world.findEnemy(0.0f, range) != null
    }

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        if (event.origin == TransferOrigin.OUTGOING && !wouldDoCriticalHit(ignoreSprint = true) && enemyInRange) {
            if (BlinkManager.isAboveTime(nextDelay.toLong())) {
                nextDelay = delay.random()
                return@handler
            }

            event.action = when (event.packet) {
                is ServerboundUseItemOnPacket,
                is ServerboundPlayerActionPacket,
                is ServerboundSignUpdatePacket,
                is ServerboundInteractPacket,
                is ServerboundAttackPacket,
                is ServerboundSpectatorActionPacket,
                is ServerboundPunchPacket,
                is ServerboundResourcePackPacket -> BlinkManager.Action.PASS
                else -> BlinkManager.Action.QUEUE
            }
            isInState = true
        } else {
            isInState = false
        }
    }

    override fun disable() {
        isInState = false
        super.disable()
    }

}


