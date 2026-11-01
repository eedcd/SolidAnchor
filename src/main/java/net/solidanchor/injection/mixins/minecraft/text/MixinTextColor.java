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

import com.google.common.base.Objects;
import net.solidanchor.interfaces.TextColorAddition;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Why not Style? Because it is immutable so we would have to create a new instance every edit.
 */
@Mixin(TextColor.class)
public abstract class MixinTextColor implements TextColorAddition {

    @Shadow
    @Final
    private @Nullable String name;
    @Shadow
    @Final
    private int value;
    @Unique
    private boolean bypassesNameProtect = false;

    @Override
    public boolean solid_anchor$doesBypassingNameProtect() {
        return bypassesNameProtect;
    }

    @Override
    public TextColor solid_anchor$withNameProtectionBypass() {
        var textColor = new TextColor(this.value, this.name);

        ((TextColorAddition) ((Object) textColor)).solid_anchor$setBypassingNameProtection(true);

        return textColor;
    }

    @Override
    public void solid_anchor$setBypassingNameProtection(boolean bypassesNameProtect) {
        this.bypassesNameProtect = bypassesNameProtect;
    }

    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    private void equals(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (o instanceof TextColor) {
            if (this.bypassesNameProtect != ((TextColorAddition) o).solid_anchor$doesBypassingNameProtect()) {
                cir.setReturnValue(false);
            }
        }
    }
    /**
     * @author superblaubeere27
     * @reason Nobody will ever overwrite this method too fr.
     */
    @Overwrite
    public int hashCode() {
        return Objects.hashCode(this.name, this.value, this.bypassesNameProtect);
    }

}
