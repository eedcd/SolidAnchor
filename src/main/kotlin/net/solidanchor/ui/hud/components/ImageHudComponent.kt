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

package net.solidanchor.ui.hud.components

import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.render.drawTexQuad
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.ui.hud.NativeHudComponent
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.render.Alignment
import net.solidanchor.utils.render.asTexture
import net.solidanchor.utils.render.readNativeImage
import net.solidanchor.utils.render.textureSetup
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.texture.DynamicTexture
import java.io.File

object ImageHudComponent : NativeHudComponent(
    "Image",
    false,
    Alignment(
        horizontalAlignment = Alignment.ScreenAxisX.CENTER_TRANSLATED,
        horizontalOffset = 0,
        verticalAlignment = Alignment.ScreenAxisY.CENTER_TRANSLATED,
        verticalOffset = 0,
    ),
    description = "Displays a local image file.",
) {

    private const val PLACEHOLDER_SIZE = 96f
    private const val RELOAD_INTERVAL_MS = 1000L

    private val file by text("File", "")
    private val scale by float("Scale", 1.0F, 0.0F..10.0F)
    private val opacity by int("Opacity", 100, 0..100)

    private var texture: DynamicTexture? = null
    private var textureKey: String? = null
    private var textureWidth = 0
    private var textureHeight = 0
    private var lastReloadCheck = 0L

    override val guiScaledWidth: Float
        get() = if (texture != null) textureWidth * scale else PLACEHOLDER_SIZE

    override val guiScaledHeight: Float
        get() = if (texture != null) textureHeight * scale else PLACEHOLDER_SIZE

    val renderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        reloadTexture()

        val bounds = getGuiScaledBounds()
        val loadedTexture = texture

        with(event.context) {
            if (loadedTexture == null) {
                hudPanel(bounds)
                hudBorder(bounds)
                hudText(
                    "Image",
                    bounds.xMin.toInt() + (bounds.width.toInt() - hudTextWidth("Image")) / 2,
                    bounds.yMin.toInt() + bounds.height.toInt() / 2,
                    HudStyle.TEXT_DIM,
                )
            } else {
                val alpha = (opacity * 255 / 100).coerceIn(0, 255)
                drawTexQuad(
                    textureSetup = loadedTexture.textureSetup,
                    x0 = bounds.xMin,
                    y0 = bounds.yMin,
                    x1 = bounds.xMax,
                    y1 = bounds.yMax,
                    argb = Color4b.WHITE.alpha(alpha).argb,
                )
            }
        }
    }

    private fun reloadTexture() {
        val now = System.currentTimeMillis()
        if (now - lastReloadCheck < RELOAD_INTERVAL_MS) {
            return
        }
        lastReloadCheck = now

        val path = file
        val imageFile = path.takeIf { it.isNotBlank() }?.let(::File)
        val exists = imageFile?.isFile == true
        val newKey = if (exists) {
            "$path|${imageFile!!.lastModified()}|${imageFile.length()}"
        } else {
            "$path|missing"
        }

        if (newKey == textureKey) {
            return
        }
        textureKey = newKey

        texture?.close()
        texture = null
        textureWidth = 0
        textureHeight = 0

        if (!exists) {
            return
        }

        var nativeImage: NativeImage? = null
        try {
            val image = imageFile!!.readNativeImage()
            nativeImage = image
            textureWidth = image.width
            textureHeight = image.height
            texture = image.asTexture("HUD Image ${imageFile.name}")
        } catch (exception: Exception) {
            // Keep showing the placeholder when the file cannot be decoded.
            texture?.close()
            texture = null
            textureWidth = 0
            textureHeight = 0
            nativeImage?.close()
        }
    }

}
