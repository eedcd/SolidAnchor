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

package net.solidanchor.injection.mixins.minecraft.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.solidanchor.features.misc.SelfDestruct;
import net.solidanchor.features.module.modules.render.ModuleSkinChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInfo.class)
public abstract class MixinPlayerInfo {

    @Shadow
    @Final
    private GameProfile profile;

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    @SuppressWarnings({"ConstantConditions", "EqualsBetweenInconvertibleTypes", "RedundantCast"})
    private PlayerSkin solid_anchor$skin(PlayerSkin original) {
        if (SelfDestruct.INSTANCE.isDestructed()) {
            return original;
        }

        if (ModuleSkinChanger.INSTANCE.getRunning()) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var playerListEntry = player.getPlayerInfo();
                if (playerListEntry != null && playerListEntry.equals((PlayerInfo) (Object) this)) {
                    var customSkinTextures = ModuleSkinChanger.INSTANCE.getSkinTextures();
                    if (customSkinTextures != null) {
                        original = customSkinTextures.get();
                    }
                }
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "createSkinLookup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalPlayer(Ljava/util/UUID;)Z"))
    private static boolean solid_anchor$allow_custom_skin(boolean b) {
        return b || ModuleSkinChanger.INSTANCE.getRunning();
    }

}
