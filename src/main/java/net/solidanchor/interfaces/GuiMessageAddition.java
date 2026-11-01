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

import net.solidanchor.features.module.modules.misc.betterchat.ModuleBetterChat;

/**
 * Additions to {@link net.minecraft.client.GuiMessage}.
 */
public interface GuiMessageAddition {

    /**
     * Sets the count of the message.
     * This indicates how many times this massage has already been sent in {@link ModuleBetterChat}.
     */
    void solid_anchor$setCount(int count);

    /**
     * Gets the count stored in this line.
     */
    @SuppressWarnings("unused")
    int solid_anchor$getCount();

}
