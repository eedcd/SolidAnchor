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
package net.solidanchor.injection.mixins.minecraft.text;

import net.solidanchor.interfaces.GuiMessageAddition;
import net.solidanchor.interfaces.GuiMessageLineAddition;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public abstract class MixinGuiMessage implements GuiMessageLineAddition, GuiMessageAddition {

    @Unique
    private @Nullable String solid_anchor$id = null;

    @Unique
    private int solid_anchor$count = 1;

    @Unique
    @Override
    public void solid_anchor$setId(@Nullable String id) {
        this.solid_anchor$id = id;
    }

    @Unique
    @Override
    public @Nullable String solid_anchor$getId() {
        return solid_anchor$id;
    }

    @Unique
    @Override
    public void solid_anchor$setCount(int count) {
        this.solid_anchor$count = count;
    }

    @Unique
    @Override
    public int solid_anchor$getCount() {
        return solid_anchor$count;
    }

}
