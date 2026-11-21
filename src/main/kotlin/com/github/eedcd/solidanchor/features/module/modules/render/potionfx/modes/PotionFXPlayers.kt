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

package net.solidanchor.features.module.modules.render.potionfx.modes

import com.mojang.math.Axis
import net.ccbluex.fastutil.enumSetAllOf
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.config.utils.TextureMode
import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.modules.render.potionfx.ModulePotionFX
import net.solidanchor.features.module.modules.render.potionfx.ModulePotionFX.PresetTexture
import net.solidanchor.features.module.modules.render.potionfx.ModulePotionFX.SecondaryPresetTexture
import net.solidanchor.features.module.modules.render.potionfx.modes.PotionFXPlayers.MainEffect.radius
import net.solidanchor.features.module.modules.render.potionfx.modes.PotionFXPlayers.MainEffect.rotationSpeed
import net.solidanchor.features.module.modules.render.potionfx.modes.PotionFXPlayers.MainEffect.textureMode
import net.solidanchor.features.module.modules.render.potionfx.modes.PotionFXPlayers.SecondEffect.extraRadius
import net.solidanchor.features.module.modules.render.potionfx.modes.PotionFXPlayers.SecondEffect.secondaryTextureMode
import net.solidanchor.render.AnchorPoint
import net.solidanchor.render.drawSquareTexture
import net.solidanchor.render.renderEnvironment
import net.solidanchor.render.withPositionRelativeToCamera
import net.solidanchor.render.withPush
import net.solidanchor.utils.world.EntityLookup.Companion.EntityLookup
import net.solidanchor.utils.world.filterTo
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.alchemy.PotionContents.getColorOptional

object PotionFXPlayers : ToggleableValueGroup(ModulePotionFX, "Players", true) {

    private object MainEffect : ValueGroup("MainEffect") {
        val radius by float("Radius", 1f, 0.1f..10f)
        val rotationSpeed by float("RotationSpeed", 4f, -10f..10f)

        val textureMode = modes(this@PotionFXPlayers, "Source", 0) {
            arrayOf(
                TextureMode.Builtin(it, PresetTexture.DASHED, enumSetAllOf<PresetTexture>()),
                TextureMode.Custom(it),
            )
        }
    }

    private object SecondEffect : ToggleableValueGroup(this, "SecondEffect", false) {
        val rotationSpeed by float("RotationSpeed", 4f, -10f..10f)
        val extraRadius by float("ExtraRadius", 0f, 0f..10f)
        val secondaryTextureMode = modes(this, "Source", 0) {
            arrayOf(
                TextureMode.Builtin(it, SecondaryPresetTexture.CRACKED, enumSetAllOf<SecondaryPresetTexture>()),
                TextureMode.Custom(it),
            )
        }
    }

    init {
        tree(MainEffect)
        tree(SecondEffect)
    }

    private val canBeCovered by boolean("CanBeCovered", true)

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        event.renderEnvironment {
            val texture = textureMode.activeMode.texture ?: return@handler
            val secondaryTexture = secondaryTextureMode.activeMode.texture ?: return@handler
            if (players.isEmpty()) return@handler

            for (entity in players) {
                withPositionRelativeToCamera(entity.getPosition(event.partialTicks).add(0.0, 0.01, 0.0)) {
                    poseStack.withPush {
                        val rotation = (entity.tickCount + event.partialTicks) * rotationSpeed
                        val secondRotation = (entity.tickCount + event.partialTicks) * SecondEffect.rotationSpeed

                        withPush {
                            rotate(Axis.XP.rotationDegrees(-90f))
                            rotate(Axis.ZP.rotationDegrees(rotation))
                            drawSquareTexture(
                                texture,
                                radius * 2,
                                getColorOptional(entity.activeEffects).asInt,
                                AnchorPoint.CENTER,
                                noDepthTest = !canBeCovered
                            )
                        }
                        if (SecondEffect.enabled) {
                            withPush {
                                rotate(Axis.XP.rotationDegrees(-90f))
                                rotate(Axis.ZP.rotationDegrees(secondRotation))
                                drawSquareTexture(
                                    secondaryTexture,
                                    (radius + extraRadius) * 2,
                                    getColorOptional(entity.activeEffects).asInt,
                                    AnchorPoint.CENTER,
                                    noDepthTest = !canBeCovered
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private val players by EntityLookup { set ->
        filterTo(
            set,
            EntityTypes.PLAYER
        ) { !it.activeEffects.isEmpty() && (!getColorOptional(it.activeEffects).isEmpty) }
    }

    override fun onDisabled() {
        players.clear()
        super.onDisabled()
    }

}
