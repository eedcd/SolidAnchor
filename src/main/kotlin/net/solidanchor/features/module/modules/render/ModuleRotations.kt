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
package net.solidanchor.features.module.modules.render

import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.render.ModuleRotations.smooth
import net.solidanchor.render.drawBox
import net.solidanchor.render.drawLine
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.render.engine.type.Vec3f
import net.solidanchor.render.renderEnvironment
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.entity.lastRotation
import net.solidanchor.utils.kotlin.EventPriorityConvention
import net.solidanchor.utils.math.toVec3f
import net.minecraft.world.phys.AABB

/**
 * Rotations module
 *
 * Allows you to see server-sided rotations.
 */

object ModuleRotations : ClientModule("Rotations", ModuleCategories.RENDER) {

    /**
     * Body part to modify the rotation of.
     */
    private val bodyPart by multiEnumChoice("BodyPart", BodyPart.entries)

    @Suppress("unused")
    enum class BodyPart(
        override val tag: String,
    ) : Tagged {
        HEAD("Head"),
        BODY("Body");
    }

    fun isPartAllowed(part: BodyPart) = part in bodyPart

    /**
     * Smoothes the rotation visually only.
     */
    private val smooth by float("Smooth", 0.0f, 0.0f..0.3f)

    private val vectorLine by color("VectorLine", Color4b.WHITE.with(a = 0)) // alpha 0 means OFF
    private val vectorDot by color("VectorDot", Color4b(0x00, 0x80, 0xFF, 0x00))

    /**
     * The current model rotation, we could be using
     * [RotationManager.currentRotation] and [RotationManager.previousRotation]
     * directly but this is required for [smooth] to work.
     */
    var modelRotation: Rotation? = null
        get() = if (this.running) field else null
    var prevModelRotation: Rotation? = null

    @Suppress("unused")
    private val modelUpdater = handler<GameTickEvent>(priority = EventPriorityConvention.READ_FINAL_STATE) {
        val prev = prevModelRotation ?: player.lastRotation
        val current = RotationManager.currentRotation

        if (current == null) {
            prevModelRotation = modelRotation
            modelRotation = null
            return@handler
        }

        val next = if (smooth > 0f) {
            prev.interpolateTo(current, 1f - smooth)
        } else {
            current
        }

        prevModelRotation = modelRotation
        modelRotation = next
    }

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        val drawVectorLine = vectorLine.a > 0
        val drawVectorDot = vectorDot.a > 0

        if (drawVectorLine || drawVectorDot) {
            val currentRotation = RotationManager.currentRotation ?: return@handler
            val previousRotation = RotationManager.previousRotation ?: currentRotation

            val interpolatedRotationVec = previousRotation.directionVector
                .lerp(currentRotation.directionVector, event.partialTicks.toDouble())
                .toVec3f()

            val eyeVector = Vec3f.eyeVector(event.camera)

            event.renderEnvironment {
                val vector = eyeVector.fma(100f, interpolatedRotationVec)
                if (drawVectorLine) {
                    drawLine(eyeVector, vector, vectorLine.argb)
                }

                if (drawVectorDot) {
                    drawBox(AABB.ofSize(vector.toVec3d(), 2.5, 2.5, 2.5), vectorDot)
                }
            }
        }
    }

    override fun onDisabled() {
        this.modelRotation = null
        this.prevModelRotation = null
        super.onDisabled()
    }
}
