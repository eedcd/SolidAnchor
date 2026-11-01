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

package net.solidanchor.injection.mixins.minecraft.gui.custom;

import net.solidanchor.features.misc.SelfDestruct;
import net.solidanchor.injection.mixins.minecraft.gui.MixinScreen;
import net.solidanchor.ui.multiplayer.MultiplayerActions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the spoofer settings button to the multiplayer screen.
 */
@Mixin(JoinMultiplayerScreen.class)
public abstract class MixinJoinMultiplayerScreen extends MixinScreen {

    @Inject(method = "init", at = @At("TAIL"))
    private void injectSpooferButton(CallbackInfo callback) {
        if (SelfDestruct.INSTANCE.isDestructed()) {
            return;
        }

        Button spooferButton = Button.builder(
            Component.literal("Spoofer"),
            _ -> this.minecraft.gui.setScreen(
                MultiplayerActions.INSTANCE.spooferScreen((Screen) (Object) this)
            )
        ).pos(4, 4).size(80, 20).build();

        addRenderableWidget(spooferButton);
    }

}
