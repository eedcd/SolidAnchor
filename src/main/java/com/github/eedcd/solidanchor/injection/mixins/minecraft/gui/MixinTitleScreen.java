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

package com.github.eedcd.solidanchor.injection.mixins.minecraft.gui;

import com.github.eedcd.solidanchor.features.misc.SelfDestruct;
import com.github.eedcd.solidanchor.ui.altmanager.AltManagerScreen;
import com.github.eedcd.solidanchor.ui.clickgui.ClickGuiScreen;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends MixinScreen {

    /** Height of one vanilla menu row (20px button + 4px gap). */
    private static final int ROW_HEIGHT = 24;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_GAP = 6;

    /**
     * Reserves one free row between "Minecraft Realms" and the vanilla row below it, so the
     * SolidAnchor buttons can be placed directly under Realms without overlapping anything.
     */
    @ModifyReturnValue(method = "createNormalMenuOptions(II)I", at = @At("RETURN"))
    private int reserveSolidAnchorRow(int original) {
        if (SelfDestruct.INSTANCE.isDestructed()) {
            return original;
        }
        return original + ROW_HEIGHT;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectSolidAnchorButton(CallbackInfo ci) {
        if (SelfDestruct.INSTANCE.isDestructed()) {
            return;
        }

        Button clickGuiButton = Button.builder(Component.literal("ClickGUI"), _ -> this.minecraft.gui.setScreen(new ClickGuiScreen()))
            .size(BUTTON_WIDTH, 20)
            .build();
        Button altManagerButton = Button.builder(Component.literal("Alt Manager"), _ -> this.minecraft.gui.setScreen(new AltManagerScreen()))
            .size(BUTTON_WIDTH, 20)
            .build();

        if (this.minecraft.isDemo()) {
            // No Realms button in the demo menu, keep the old bottom-left placement.
            clickGuiButton.setPosition(4, this.height - 24);
            altManagerButton.setPosition(98, this.height - 24);
        } else {
            // Vanilla rows start at height / 4 + 48: Singleplayer, Multiplayer, Realms.
            // Realms is the third row, so the row reserved above it is 72px further down.
            int y = this.height / 4 + 48 + ROW_HEIGHT * 3;
            int x = this.width / 2 - (BUTTON_WIDTH * 2 + BUTTON_GAP) / 2;
            clickGuiButton.setPosition(x, y);
            altManagerButton.setPosition(x + BUTTON_WIDTH + BUTTON_GAP, y);
        }

        addRenderableWidget(clickGuiButton);
        addRenderableWidget(altManagerButton);
    }

}
