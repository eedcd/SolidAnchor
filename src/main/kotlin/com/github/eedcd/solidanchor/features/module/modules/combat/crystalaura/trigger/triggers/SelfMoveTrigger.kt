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
package com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.trigger.triggers

import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.destroy.SubmoduleCrystalDestroyer
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.place.SubmoduleCrystalPlacer
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.mc
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.runDestroy
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.runPlace
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.trigger.Trigger
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * Runs placing when you move.
 */
object SelfMoveTrigger : Trigger("SelfMove", false) {

    @Suppress("unused")
    private val packetListener = handler<PacketEvent>(-1) { event ->
        val packet = event.packet
        if (packet !is ServerboundMovePlayerPacket) {
            return@handler
        }

        mc.execute {
            runDestroy { SubmoduleCrystalDestroyer.tick() }
            runPlace { SubmoduleCrystalPlacer.tick() }
        }
    }

}
