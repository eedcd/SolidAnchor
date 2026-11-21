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
package com.github.eedcd.solidanchor.features.module.modules.player

import com.github.eedcd.solidanchor.event.events.MovementInputEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.utils.kotlin.EventPriorityConvention.CRITICAL_MODIFICATION

/**
 * AutoWalk module
 *
 * Automatically makes you walk.
 */
object ModuleAutoWalk : ClientModule("AutoWalk", ModuleCategories.PLAYER) {

    @Suppress("unused")
    private val moveInputHandler = handler<MovementInputEvent>(priority = CRITICAL_MODIFICATION) { event ->
        event.directionalInput = event.directionalInput.copy(forwards = true)
    }

}
