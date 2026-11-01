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

package net.solidanchor.injection.mixins.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.solidanchor.features.module.modules.movement.ModuleSprint;
import net.solidanchor.interfaces.ClientInputAddition;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientInput.class)
public abstract class MixinClientInput implements ClientInputAddition {

    @Shadow
    protected Vec2 moveVector;

    @Unique
    protected Input initial = Input.EMPTY;

    @Unique
    protected Input untransformed = Input.EMPTY;

    @ModifyReturnValue(method = "hasForwardImpulse", at = @At("RETURN"))
    private boolean hookOmnidirectionalSprint(boolean original) {
        // Allow omnidirectional sprinting
        if (ModuleSprint.INSTANCE.getShouldSprintOmnidirectional()) {
            return Math.abs(moveVector.x) > 1.0E-5F || Math.abs(moveVector.y) > 1.0E-5F;
        }

        return original;
    }

    @Override
    public void solid_anchor$setMovementInput(Vec2 movementVector) {
        this.moveVector = movementVector;
    }

    @Override
    public Input solid_anchor$getInitial() {
        return initial;
    }

    @Override
    public Input solid_anchor$getUntransformed() {
        return untransformed;
    }

}
