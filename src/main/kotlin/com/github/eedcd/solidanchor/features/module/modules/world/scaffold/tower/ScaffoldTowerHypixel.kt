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
package com.github.eedcd.solidanchor.features.module.modules.world.scaffold.tower

import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.ModuleScaffold
import com.github.eedcd.solidanchor.lang.translation
import com.github.eedcd.solidanchor.utils.client.chat
import com.github.eedcd.solidanchor.utils.client.isEqual1_8
import com.github.eedcd.solidanchor.utils.client.markAsError
import com.github.eedcd.solidanchor.utils.client.warning
import com.github.eedcd.solidanchor.utils.entity.horizontalSpeed

object ScaffoldTowerHypixel : ScaffoldTower("Hypixel") {
    private var notifiedWrongProtocol = false
    private var notifiedStationary = false

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (!mc.options.keyJump.isDown || ModuleScaffold.blockCount <= 0) {
            return@handler
        }

        if (!isEqual1_8) {
            if (!notifiedWrongProtocol) {
                notifiedWrongProtocol = true
                chat(
                    markAsError(
                        translation("solidanchor.module.scaffold.messages.hypixelTower.wrongProtocolVersion")
                    )
                )
            }
        } else {
            notifiedWrongProtocol = false
        }

        if (player.horizontalSpeed > 0.01) {
            if (!notifiedStationary) {
                notifiedStationary = true
                chat(
                    warning(
                        translation("solidanchor.module.scaffold.messages.hypixelTower.stationaryWarning")
                    )
                )
            }
            return@handler
        }

        if (player.onGround()) {
            player.deltaMovement.y = 0.42
        }
        if (player.deltaMovement.y <= 0.0 && player.deltaMovement.y >= -0.09) {
            player.deltaMovement.y = -0.38
        }
    }


}
