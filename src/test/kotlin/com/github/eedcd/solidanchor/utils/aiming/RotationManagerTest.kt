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
package net.solidanchor.utils.aiming

import net.solidanchor.utils.aiming.features.MovementCorrection
import kotlin.test.Test
import kotlin.test.assertEquals

class RotationManagerTest {

    @Test
    fun `movement yaw follows active correction`() {
        val playerYaw = 30f
        val managedYaw = 120f

        assertEquals(playerYaw, resolveMovementYaw(playerYaw, managedYaw, null))
        assertEquals(playerYaw, resolveMovementYaw(playerYaw, managedYaw, MovementCorrection.OFF))
        assertEquals(playerYaw, resolveMovementYaw(playerYaw, Float.NaN, MovementCorrection.SILENT))
        assertEquals(managedYaw, resolveMovementYaw(playerYaw, managedYaw, MovementCorrection.STRICT))
        assertEquals(managedYaw, resolveMovementYaw(playerYaw, managedYaw, MovementCorrection.SILENT))
        assertEquals(managedYaw, resolveMovementYaw(playerYaw, managedYaw, MovementCorrection.CHANGE_LOOK))
    }
}
