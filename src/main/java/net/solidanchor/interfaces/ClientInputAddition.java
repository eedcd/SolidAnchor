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

package net.solidanchor.interfaces;

import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public interface ClientInputAddition {
    /**
     * @param movementVector x -> movementSideways; y -> movementForward
     */
    void solid_anchor$setMovementInput(Vec2 movementVector);

    Input solid_anchor$getInitial();
    Input solid_anchor$getUntransformed();
}
