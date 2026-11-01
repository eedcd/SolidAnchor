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
package net.solidanchor.injection.mixins.minecraft.render.entity;

import net.solidanchor.interfaces.EntityRenderStateAddition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class MixinEntityRenderState implements EntityRenderStateAddition {

    @Unique
    private Entity solid_anchor$entity;

    @Unique
    private boolean solid_anchor$isCustom = false;

    @Unique
    @Override
    public void solid_anchor$setEntity(Entity entity) {
        this.solid_anchor$entity = entity;
    }

    @Unique
    @Override
    public Entity solid_anchor$getEntity() {
        return solid_anchor$entity;
    }

    @Override
    public boolean solid_anchor$isCustom() {
        return solid_anchor$isCustom;
    }

    @Override
    public void solid_anchor$setCustom(boolean custom) {
        solid_anchor$isCustom = custom;
    }
}
