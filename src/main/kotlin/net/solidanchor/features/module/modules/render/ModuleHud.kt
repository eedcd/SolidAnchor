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

import net.solidanchor.config.ConfigSystem
import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.event.EventManager
import net.solidanchor.event.events.SpaceSeperatedNamesChangeEvent
import net.solidanchor.features.misc.SelfDestruct.isDestructed
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.ui.hud.HudComponentManager

/**
 * Module HUD
 *
 * The client in-game dashboard.
 */

object ModuleHud : ClientModule("HUD", ModuleCategories.RENDER, state = true, hide = true) {

    override val running
        get() = this.enabled && !isDestructed
    override val baseKey: String
        get() = "${ConfigSystem.KEY_PREFIX}.module.hud"

    object Blur : ToggleableValueGroup(ModuleHud, "Blur", enabled = true) {
        /**
         * Gaussian sigma controlling blur strength. Higher values produce stronger blur.
         */
        val sigma by float("Sigma", 5.0F, 1.0F..15.0F)

        /**
         * The range in which the blending from not-blurred to blurred occurs.
         */
        val alphaBlendRange by floatRange("AlphaBlendRange", 0.0F..0.75F, 0.0F..1.0F)
    }

    @Suppress("unused")
    private val spaceSeperatedNames by boolean("SpaceSeperatedNames", true).onChange { state ->
        EventManager.callEvent(SpaceSeperatedNamesChangeEvent(state))
        state
    }

    val isBlurEffectActive
        get() = Blur.enabled

    val components = tree(ValueGroup("AdditionalComponents"))

    init {
        tree(Blur)
        HudComponentManager.nativeComponents.forEach(components::tree)
    }

    var hudEditorSelected = false

}
