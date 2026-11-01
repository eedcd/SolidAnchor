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
package net.solidanchor.features.module.modules.player.autoshop.purchasemode

import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.features.module.modules.player.autoshop.ModuleAutoShop
import net.minecraft.world.inventory.ContainerInput

object NormalPurchaseMode : Mode("Normal") {
    override val parent: ModeValueGroup<*>
        get() = ModuleAutoShop.purchaseMode

    val extraDelay by intRange("ExtraDelay", 2..3, 0..10, "ticks")
    val action by enumChoice("Action", ActionType.PICK_UP)

    enum class ActionType(override val tag: String, val input: ContainerInput): Tagged {
        PICK_UP("PickUp", ContainerInput.PICKUP),
        THROW("Throw", ContainerInput.THROW),
    }
}
