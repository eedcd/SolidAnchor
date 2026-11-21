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
package com.github.eedcd.solidanchor.features.module.modules.movement.fly

import com.github.eedcd.solidanchor.config.types.group.ToggleableValueGroup
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.events.PlayerStrideEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.FlyAirWalk
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.FlyCreative
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.FlyEnderpearl
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.FlyExplosion
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.FlyJetpack
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.FlyVanilla
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.fireball.FlyFireball
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.grim.FlyGrim2373Jan15
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.grim.FlyGrim2859V
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.hypixel.FlyHypixel
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.hypixel.FlyHypixelFlat
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.polar.FlyHycraftDamage
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.sentinel.FlySentinel10thMar
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.sentinel.FlySentinel20thApr
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.sentinel.FlySentinel26thDec
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.sentinel.FlySentinel27thJan
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.spartan.FlySpartan524
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.specific.FlyNcpClip
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.verus.FlyVerusB3869Flat
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.verus.FlyVerusB3896Damage
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.vulcan.FlyVulcan277
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.vulcan.FlyVulcan286
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.vulcan.FlyVulcan286MC18
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.modes.vulcan.FlyVulcan286Teleport
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.markAsError
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * Fly module
 *
 * Allows you to fly.
 */

object ModuleFly : ClientModule("Fly", ModuleCategories.MOVEMENT, aliases = listOf("Glide", "Jetpack")) {

    internal val modes = choices(
        "Mode", FlyVanilla, arrayOf(
            // Generic fly modes
            FlyVanilla,
            FlyCreative,
            FlyJetpack,
            FlyEnderpearl,
            FlyAirWalk,
            FlyExplosion,
            FlyFireball,

            // Anti-cheat specific fly modes
            FlyVulcan277,
            FlyVulcan286,
            FlyVulcan286MC18,
            FlyVulcan286Teleport,
            FlyGrim2859V,
            FlyGrim2373Jan15,
            FlySpartan524,

            // Server specific fly modes
            FlySentinel20thApr,
            FlySentinel27thJan,
            FlySentinel10thMar,
            FlySentinel26thDec,

            FlyVerusB3896Damage,
            FlyVerusB3869Flat,
            FlyNcpClip,

            FlyHypixel,
            FlyHypixelFlat,

            FlyHycraftDamage
        )
    ).apply { tagBy(this) }

    private object Visuals : ToggleableValueGroup(this, "Visuals", true) {

        private val stride by boolean("Stride", true)

        @Suppress("unused")
        val strideHandler = handler<PlayerStrideEvent> { event ->
            if (stride) {
                event.strideForce = 0.1.coerceAtMost(player.deltaMovement.horizontalDistance()).toFloat()
            }

        }

    }

    init {
        tree(Visuals)
    }

    private val disableOnSetback by boolean("DisableOnSetback", false)

    private var wasFlyingAllowed = false

    override fun onEnabled() {
        wasFlyingAllowed = player.abilities.mayfly
        player.abilities.mayfly = false
    }

    override fun onDisabled() {
        player.abilities.mayfly = wasFlyingAllowed
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        // Setback detection
        if (disableOnSetback && event.packet is ClientboundPlayerPositionPacket) {
            chat(markAsError(message("setbackDetected")))
            enabled = false
        }

        if (event.packet is ClientboundPlayerAbilitiesPacket) {
            wasFlyingAllowed = event.packet.canFly()
        }
    }

}
