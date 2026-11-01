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

package net.solidanchor.features.module.modules.render.totemeffect

import net.solidanchor.annotations.ValueClassCandidate
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.utils.collection.ExpiringList.Companion.ExpiringList
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.solidanchor.features.module.modules.render.totemeffect.modes.TotemEffectShockwave
import net.solidanchor.features.module.modules.render.totemeffect.modes.TotemEffectSoul
import net.solidanchor.utils.combat.shouldBeShown
import net.solidanchor.utils.network.isDeathProtection
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object ModuleTotemEffect : ClientModule("TotemEffect", ModuleCategories.RENDER) {

    val entities = ExpiringList<TotemPopSnapshot>()

    val modes = choices("Mode", 0) {
        arrayOf(
            TotemEffectShockwave,
            TotemEffectSoul
        )
    }.apply { tagBy(this); onChanged { entities.clear() } }

    override fun onDisabled() { entities.clear() }

    @Suppress("unused")
    private val totemHandler = handler<PacketEvent> { event ->
        val packet = event.packet as? ClientboundEntityEventPacket ?: return@handler
        if (!packet.isDeathProtection) return@handler

        mc.execute {
            val entity = event.packet.getEntity(world).takeIf { it.shouldBeShown() } ?: return@execute

            val lifetime = modes.activeMode.lifetime
            entities.add(TotemPopSnapshot(entity), lifetime)
        }
    }

    @ValueClassCandidate
    data class TotemPopSnapshot(
        val pos: Vec3,
        val xRot: Float,
        val yRot: Float,
        val bbHeight: Float,
    ) {
        constructor(entity: Entity) : this(
            pos = entity.position(),
            xRot = entity.xRot,
            yRot = entity.yRot,
            bbHeight = entity.bbHeight
        )
    }

}
