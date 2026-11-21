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
import net.solidanchor.features.module.modules.render.customambience.ModuleCustomAmbience;
import net.solidanchor.interfaces.ClientClockInstanceAddition;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Since 26.3 the overworld clock is read through {@code ClockInstance#totalTicks()} instead of the
 * former {@code ClockManager#getTotalTicks(Holder)}. Clock instances do not know their own definition,
 * so it is tagged by {@link MixinClientClockManager} upon creation.
 *
 * @see MixinClientClockManager
 */
@NullMarked
@Mixin(targets = "net.minecraft.client.ClientClockManager$ClientClockInstance")
public abstract class MixinClientClockInstance implements ClockInstance, ClientClockInstanceAddition {

    @Unique
    private @Nullable Holder<WorldClock> solidanchor$definition;

    @Unique
    @Override
    public void solid_anchor$setDefinition(Holder<WorldClock> definition) {
        this.solidanchor$definition = definition;
    }

    @ModifyReturnValue(method = "totalTicks", at = @At("RETURN"))
    private long injectOverrideClockTime(long original) {
        var definition = this.solidanchor$definition;
        if (definition == null || !definition.is(WorldClocks.OVERWORLD)) {
            return original;
        }

        return ModuleCustomAmbience.getWorldClockTime(original);
    }

}
