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
package net.solidanchor.features.module.modules.player.antivoid.mode

import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.event.events.BlinkPacketEvent
import net.solidanchor.event.events.TransferOrigin
import net.solidanchor.event.handler
import net.solidanchor.features.blink.BlinkManager
import net.solidanchor.features.blink.BlinkManager.Action
import net.solidanchor.features.blink.BlinkManager.positions
import net.solidanchor.features.module.modules.player.antivoid.ModuleAntiVoid
import net.solidanchor.features.module.modules.world.scaffold.ModuleScaffold

object AntiVoidBlinkMode : AntiVoidMode("Blink") {

    override val parent: ModeValueGroup<*>
        get() = ModuleAntiVoid.mode

    // Cases in which the AntiVoid protection should not be active.
    override val isExempt
        get() = super.isExempt || ModuleScaffold.running

    // Whether artificial lag is needed to prevent falling into the void.
    private val requiresLag
        get() = running && ModuleAntiVoid.isLikelyFalling && ModuleAntiVoid.rescuePosition != null && !isExempt

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        if (event.origin == TransferOrigin.OUTGOING && requiresLag) {
            event.action = Action.QUEUE
        }
    }

    /**
     * This method is called to discover a safe position to teleport to.
     * In this case, we simply return the last known safe position.
     *
     * TODO: This does not seem to be consistent enough,
     *   so we rather rely on the base [discoverRescuePosition] method.
     */
//    override fun discoverRescuePosition(): Vec3d? {
//        return positions.first { pos -> ModuleAntiVoid.isSafeForRescue(pos) }
//    }

    override fun rescue(): Boolean {
        if (!requiresLag) {
            return false
        }

        // Check if we have any previous positions to teleport to.
        if (positions.isEmpty()) {
            return false
        }

        BlinkManager.cancel()
        return true
    }

}
