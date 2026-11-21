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
package com.github.eedcd.solidanchor.features.module.modules.player.nofall

import com.github.eedcd.solidanchor.config.types.list.Tagged
import com.github.eedcd.solidanchor.features.module.ClientModule
import com.github.eedcd.solidanchor.features.module.ModuleCategories
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallBlink
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallBlocksMC
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallCancel
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallForceJump
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallGrim2371
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallHypixel
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallHypixelPacket
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallMLG
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallNoGround
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallPacket
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallPacketJump
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallRettungsplatform
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallSpartan524Flag
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallSpoofGround
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallSpoofLanding
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallMount
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallVerus
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallVulcan
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.modes.NoFallVulcanTP
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.Items
import java.util.function.BooleanSupplier

/**
 * NoFall module
 *
 * Protects you from taking fall damage.
 */
object ModuleNoFall : ClientModule("NoFall", ModuleCategories.PLAYER) {
    internal val modes = choices(
        "Mode", NoFallSpoofGround, arrayOf(
            NoFallSpoofGround,
            NoFallSpoofLanding,
            NoFallNoGround,
            NoFallPacket,
            NoFallPacketJump,
            NoFallMLG,
            NoFallMount,
            NoFallRettungsplatform,
            NoFallSpartan524Flag,
            NoFallVulcan,
            NoFallVulcanTP,
            NoFallVerus,
            NoFallForceJump,
            NoFallCancel,
            NoFallBlink,
            NoFallHypixelPacket,
            NoFallHypixel,
            NoFallBlocksMC,
            NoFallGrim2371
        )
    ).apply(::tagBy)

    private val notConditions by multiEnumChoice("Not", NotCondition.entries)

    override val running: Boolean
        get() = when {
            !super.running -> false

            // In creative mode, we don't need to reduce fall damage
            player.isCreative || player.isSpectator -> false

            // Check if we are invulnerable or flying
            player.abilities.invulnerable || player.abilities.flying -> false

            // Test other conditions
            else -> notConditions.none { it.asBoolean }
        }

    @Suppress("unused")
    private enum class NotCondition(
        override val tag: String,
    ) : Tagged, BooleanSupplier {
        /**
         * With Elytra - we don't want to reduce fall damage.
         */
        WHILE_GLIDING("WhileGliding") {
            override fun getAsBoolean() = player.isFallFlying && player.hasPose(Pose.FALL_FLYING)
        },

        /**
         * Check if we are holding a mace
         */
        WITH_MACE("WithMace") {
            override fun getAsBoolean() = player.mainHandItem.item == Items.MACE
        };
    }
}
