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

import org.jspecify.annotations.Nullable;

/**
 * Additions to {@link net.minecraft.client.multiplayer.chat.GuiMessage} and
 * {@link net.minecraft.client.multiplayer.chat.GuiMessage.Line}.
 */
public interface GuiMessageLineAddition {

    /**
     * Sets the ID for the chat message.
     * The ID will be used for removing chat messages.
     */
    void solid_anchor$setId(@Nullable String id);

    /**
     * Gets the ID of the chat message.
     */
    @Nullable String solid_anchor$getId();

}
