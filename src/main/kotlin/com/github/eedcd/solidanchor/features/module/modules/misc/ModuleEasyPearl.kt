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
package net.solidanchor.features.module.modules.misc

import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.events.PlayerInteractItemEvent
import net.solidanchor.event.events.RotationUpdateEvent
import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.render.FontManager
import net.solidanchor.render.FULL_BOX
import net.solidanchor.render.drawBox
import net.solidanchor.render.drawBoxSide
import net.solidanchor.render.drawGradientSides
import net.solidanchor.render.engine.font.HorizontalAnchor
import net.solidanchor.render.engine.font.VerticalAnchor
import net.solidanchor.render.engine.type.Color4b
import net.solidanchor.render.renderEnvironment
import net.solidanchor.render.withPositionRelativeToCamera
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.aiming.data.Rotation
import net.solidanchor.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.solidanchor.utils.block.state
import net.solidanchor.utils.client.chat
import net.solidanchor.utils.client.markAsError
import net.solidanchor.utils.entity.PositionExtrapolation
import net.solidanchor.utils.inventory.HotbarItemSlot
import net.solidanchor.utils.inventory.Slots
import net.solidanchor.utils.inventory.useHotbarSlotOrOffhand
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.math.toFixed
import net.solidanchor.utils.math.toBlockPos
import net.solidanchor.utils.render.WorldToScreen
import net.solidanchor.utils.render.trajectory.TrajectoryInfo
import net.solidanchor.utils.text.withFormat
import net.minecraft.core.Direction
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

/**
 * EasyPearl module
 *
 * Throw pearl to where you are looking at.
 */
object ModuleEasyPearl :
    ClientModule("EasyPearl", ModuleCategories.MISC, aliases = listOf("PearlHelper", "PearlAssist", "PearlTP")) {
    private val aimOffThreshold by float("AimOffThreshold", 2f, 0.5f..10f)
    private val reachableCheck by boolean("ReachableCheck", true)
    private val rotation = tree(RotationsValueGroup(this))

    private val showDistance by boolean("ShowDistance", true)

    private var targetPosition: Vec3? = null

    private val fontRenderer get() = FontManager.FONT_RENDERER

    var currentTargetRotation : Rotation? = null
        private set

    private val enderPearlSlot: HotbarItemSlot?
        get() = Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL)

    override fun onDisabled() {
        currentTargetRotation = null
        super.onDisabled()
    }

    /**
     * Handler throw pearl by player self.
     */
    @Suppress("unused")
    private val interactItemHandler = handler<PlayerInteractItemEvent> { event ->
        if (!isHoldingPearl() || !mc.options.keyUse.isDown) {
            return@handler
        }

        val hitResult = getPositionPlayerLookAt()
        // While reachable check is enabled, we will check if the player
        // is looking at a block father than pearl can reach
        if (reachableCheck && getTargetRotation(hitResult.location) == null
            && hitResult.type != HitResult.Type.MISS) {
            chat(markAsError(message("noInReachWarning")))
            event.cancelEvent()
            targetPosition = null
            return@handler
        }
        targetPosition = hitResult.location

        // check if we are rotating to the target position correctly
        if (isRotationDone(targetPosition!!)) {
            targetPosition = null
        } else {
            event.cancelEvent()
        }
    }

    @Suppress("unused")
    private val rotationHandler = handler<RotationUpdateEvent> {
        /**
         * handler for rotation update event,and rotate to the target rotation
         */
        val finalTargetRotation = getTargetRotation(targetPosition ?: return@handler) ?: return@handler

        RotationManager.setRotationTarget(
            rotation.toRotationTarget(finalTargetRotation),
            Priority.IMPORTANT_FOR_PLAYER_LIFE,
            this@ModuleEasyPearl,
        )
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        /**
         * handler for tick event,and check if we are rotating to the target rotation correctly,if yes,throw the pearl
         */
        currentTargetRotation = targetPosition?.let(::getTargetRotation) ?: return@handler

        if (isRotationDone(targetPosition ?: return@handler)) {
            useHotbarSlotOrOffhand(
                enderPearlSlot ?: return@handler,
                0,
                currentTargetRotation!!.yaw,
                currentTargetRotation!!.pitch,
            )
            targetPosition = null
            currentTargetRotation = null
        }
    }

    /**
     * handler for world render event,and render the target position
     */
    @Suppress("unused")
    private val worldRenderHandler = handler<WorldRenderEvent> { event ->
        if (!isHoldingPearl()) {
            return@handler
        }

        val pos = getPositionPlayerLookAt(event.partialTicks)?.location ?: return@handler
        val blockPos = pos.toBlockPos()
        val state = blockPos.state ?: return@handler

        event.renderEnvironment {
            val color = targetColor(pos)

            val baseColor = color.with(a = 50)
            val transparentColor = baseColor.with(a = 0)
            val outlineColor = color.with(a = 200)

            withPositionRelativeToCamera(blockPos) {
                if (state.renderShape != RenderShape.MODEL && state.isAir) {
                    drawBoxSide(
                        FULL_BOX,
                        Direction.DOWN,
                        baseColor,
                        outlineColor,
                    )
                    drawGradientSides(0.7, baseColor, transparentColor, FULL_BOX)
                } else {
                    drawBox(
                        FULL_BOX,
                        baseColor,
                        outlineColor,
                    )
                }
            }
        }
    }

    @Suppress("unused")
    private val overlayRenderHandler = handler<OverlayRenderEvent> { event ->
        if (!isHoldingPearl() || !showDistance) {
            return@handler
        }

        val pos = getPositionPlayerLookAt(event.tickDelta)?.location ?: return@handler
        pos.toBlockPos().state ?: return@handler

        val screenPos = WorldToScreen.calculateScreenPos(
            pos.add(0.0, player.eyeHeight.toDouble(), 0.0)
        ) ?: return@handler
        val distanceText = "${player.position().distanceTo(pos).toFixed(1)}m".withFormat()
        val fontRenderer = fontRenderer

        with(event.context) {
            pose().pushMatrix()
            pose().translate(screenPos.x, screenPos.y)
            pose().scale(fontRenderer.scaleToVanillaFont)

            fontRenderer.draw(distanceText) {
                this.color = targetColor(pos)
                horizontalAnchor = HorizontalAnchor.CENTER
                verticalAnchor = VerticalAnchor.MIDDLE
                shadow = true
            }

            pose().popMatrix()
        }
    }

    private fun isHoldingPearl() =
        player.mainHandItem.item == Items.ENDER_PEARL || player.offhandItem.item == Items.ENDER_PEARL

    private fun targetColor(pos: Vec3) =
        if (getTargetRotation(pos) != null) {
            Color4b(0x20, 0xC2, 0x06)
        } else {
            Color4b(0xD7, 0x09, 0x09)
        }

    /**
     * check if we are rotating to the target rotation correctly
     * @param targetPosition the target position
     * @return true if we are rotating to the target rotation correctly, false otherwise
     */
    @Suppress("ReturnCount")
    private fun isRotationDone(targetPosition: Vec3): Boolean {
        return RotationManager.serverRotation.directionAngleTo(
            getTargetRotation(targetPosition) ?: return true,
        ) <= aimOffThreshold
    }

    /**
     * get the position player look at
     * @return the position player look at
     */
    private fun getPositionPlayerLookAt(partialTicks: Float = 0f) =
        player.pick(1000.0, partialTicks, false)

    /**
     * get the target rotation for the target position
     * @param targetPosition the target position
     * @return the target rotation for the target position
     */
    private fun getTargetRotation(targetPosition: Vec3): Rotation? =
        SituationalProjectileAngleCalculator.calculateAngleFor(
            TrajectoryInfo.GENERIC,
            sourcePos = player.position(),
            targetPosFunction = PositionExtrapolation.constant(targetPosition),
            targetShape = EntityDimensions.fixed(1.0F, 0.0F),
        )
}
