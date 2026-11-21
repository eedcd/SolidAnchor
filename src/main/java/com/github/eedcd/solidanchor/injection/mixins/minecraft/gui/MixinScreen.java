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

import com.github.eedcd.solidanchor.SolidAnchor;
import com.github.eedcd.solidanchor.additions.ScreenAddition;
import com.github.eedcd.solidanchor.features.module.modules.player.cheststealer.features.FeatureSilentScreen;
import com.github.eedcd.solidanchor.features.module.modules.render.DoRender;
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleAntiBlind;
import com.github.eedcd.solidanchor.utils.text.RunnableClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen implements ScreenAddition {

    @Shadow
    public int height;
    @Shadow
    public int width;
    @Final
    @Shadow
    protected Font font;
    @Final
    @Shadow
    @Nullable
    protected Minecraft minecraft;
    @Shadow
    private boolean initialized;

    /**
     * Allows the execution of {@link RunnableClickEvent}.
     * (default branch in switch pattern matching)
     */
    @Inject(method = "defaultHandleClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 0, shift = At.Shift.BEFORE, remap = false), cancellable = true)
    private static void hookExecuteClickEvents(ClickEvent clickEvent, Minecraft client, Screen screenAfterRun, CallbackInfo ci) {
        if (clickEvent instanceof RunnableClickEvent runnableClickEvent) {
            runnableClickEvent.run();
            ci.cancel();
        }
    }

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable> T addRenderableWidget(T drawableElement);

    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    private void hookRenderInGameBackground(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (!ModuleAntiBlind.canRender(DoRender.GUI_BACKGROUND)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"), cancellable = true)
    private void cancelRenderByChestStealer(CallbackInfo ci) {
        if (SolidAnchor.INSTANCE.isInitialized() && FeatureSilentScreen.INSTANCE.getShouldHide()) {
            ci.cancel();
        }
    }

    @Unique
    @Override
    public boolean solidanchor$screenInitialized() {
        return initialized;
    }
}
