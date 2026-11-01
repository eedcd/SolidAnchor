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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.solidanchor.event.EventManager;
import net.solidanchor.event.events.MovementInputEvent;
import net.solidanchor.event.events.SprintEvent;
import net.solidanchor.utils.aiming.RotationManager;
import net.solidanchor.utils.aiming.features.MovementCorrection;
import net.solidanchor.utils.movement.DirectionalInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.util.Mth.DEG_TO_RAD;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends MixinClientInput {

    @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"))
    private Input modifyInput(Input original) {
        this.initial = original;

        var event = new MovementInputEvent(new DirectionalInput(original), original.jump(), original.shift());
        EventManager.INSTANCE.callEvent(event);
        var untransformedDirectionalInput = event.getDirectionalInput();
        var directionalInput = transformDirection(untransformedDirectionalInput);

        var sprintEvent = new SprintEvent(directionalInput, original.sprint(), SprintEvent.Source.INPUT);
        EventManager.INSTANCE.callEvent(sprintEvent);

        // Store the untransformed input for later use
        this.untransformed = new Input(
                untransformedDirectionalInput.getForwards(),
                untransformedDirectionalInput.getBackwards(),
                untransformedDirectionalInput.getLeft(),
                untransformedDirectionalInput.getRight(),
                event.getJump(),
                event.getSneak(),
                sprintEvent.getSprint()
        );

        return new Input(
                directionalInput.getForwards(),
                directionalInput.getBackwards(),
                directionalInput.getLeft(),
                directionalInput.getRight(),
                event.getJump(),
                event.getSneak(),
                sprintEvent.getSprint()
        );
    }

    @Unique
    private DirectionalInput transformDirection(DirectionalInput input) {
        var player = Minecraft.getInstance().player;
        var rotation = RotationManager.INSTANCE.getCurrentRotation();
        var configurable = RotationManager.INSTANCE.getActiveRotationTarget();

        float z = KeyboardInput.calculateImpulse(input.getForwards(), input.getBackwards());
        float x = KeyboardInput.calculateImpulse(input.getLeft(), input.getRight());

        if (configurable == null || configurable.getMovementCorrection() != MovementCorrection.SILENT
                || rotation == null || player == null) {
            return input;
        }

        float deltaYaw = player.getYRot() - rotation.yRot();

        float newX = x * Mth.cos(deltaYaw * DEG_TO_RAD) - z *
                Mth.sin(deltaYaw * DEG_TO_RAD);
        float newZ = z * Mth.cos(deltaYaw * DEG_TO_RAD) + x *
                Mth.sin(deltaYaw * DEG_TO_RAD);

        var movementSideways = Math.round(newX);
        var movementForward = Math.round(newZ);

        return new DirectionalInput(movementForward, movementSideways);
    }

}
