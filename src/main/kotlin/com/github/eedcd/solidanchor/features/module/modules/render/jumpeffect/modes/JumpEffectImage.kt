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

package net.solidanchor.features.module.modules.render.jumpeffect.modes

import com.mojang.math.Axis
import net.ccbluex.fastutil.enumSetAllOf
import net.solidanchor.SolidAnchor
import net.solidanchor.SolidAnchor.CLIENT_NAME
import net.solidanchor.config.utils.TextureMode
import net.solidanchor.features.module.modules.render.jumpeffect.JumpEffectColorSettings
import net.solidanchor.features.module.modules.render.jumpeffect.JumpEffectMode
import net.solidanchor.render.AnchorPoint
import net.solidanchor.render.WorldRenderEnvironment
import net.solidanchor.render.drawSquareTextureGradient
import net.solidanchor.render.withPush
import net.solidanchor.utils.render.asTexture
import net.solidanchor.utils.render.readNativeImage

internal object JumpEffectImage : JumpEffectMode("Image") {

    private val colors = JumpEffectColorSettings()
    private val rotationSpeed by int("RotationSpeed", 10, -360..360)

    private val textureMode = modes("Source", 0) {
        arrayOf(
            TextureMode.Builtin(it, PresetTexture.SOLIDANCHOR, enumSetAllOf<PresetTexture>()),
            TextureMode.Custom(it),
        )
    }

    init {
        tree(colors)
    }

    override fun WorldRenderEnvironment.drawJumpEffect(progress: Float, age: Float) {
        val texture = textureMode.activeMode.texture ?: return
        poseStack.withPush {
            val currentRotation = rotationSpeed * age

            rotate(Axis.XP.rotationDegrees(90f))
            rotate(Axis.ZP.rotationDegrees(currentRotation))
            drawSquareTextureGradient(
                sampler0 = texture,
                outerRadius = endRadius.endInclusive * progress,
                innerRadius = endRadius.start * progress,
                animateColor(colors.outerColor, age),
                animateColor(colors.innerColor, age),
                anchor = AnchorPoint.CENTER,
                startOffset = 0.8f,
                noDepthTest = !canBeCovered
            )
        }
    }

    @Suppress("unused")
    private enum class PresetTexture(override val tag: String, val path: String) : TextureMode.Builtin.Preset {
        SOLIDANCHOR(CLIENT_NAME, "jump_effect/solidanchor.png"),
        SOLIDANCHOR_LOGO(CLIENT_NAME + "WithLogo", "jump_effect/solidanchor_with_logo.png");

        override val texture = SolidAnchor.resource(this.path)
            .readNativeImage().asTexture { "JumpEffect Image $tag" }
    }
}
