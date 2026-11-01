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

package net.solidanchor.injection.mixins.minecraft.gui;

import net.solidanchor.features.misc.SelfDestruct;
import net.solidanchor.ui.altmanager.AltManagerScreen;
import net.solidanchor.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends MixinScreen {

    @Inject(method = "init", at = @At("TAIL"))
    private void injectSolidAnchorButton(CallbackInfo ci) {
        if (SelfDestruct.INSTANCE.isDestructed()) {
            return;
        }

        Button clickGuiButton = Button.builder(Component.literal("ClickGUI"), _ -> this.minecraft.gui.setScreen(new ClickGuiScreen()))
            .pos(4, this.height - 24)
            .size(90, 20)
            .build();

        addRenderableWidget(clickGuiButton);

        Button altManagerButton = Button.builder(Component.literal("Alt Manager"), _ -> this.minecraft.gui.setScreen(new AltManagerScreen()))
            .pos(98, this.height - 24)
            .size(90, 20)
            .build();

        addRenderableWidget(altManagerButton);
    }

}
