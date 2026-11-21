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

package com.github.eedcd.solidanchor.ui.hud

import java.util.concurrent.CopyOnWriteArrayList
import com.github.eedcd.solidanchor.features.addon.AddonApi
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleHud
import com.github.eedcd.solidanchor.ui.hud.components.ArmorItemsHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.ArrayListHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.BlockCounterHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.ClosedCaptionsHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.CraftingInventoryHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.EffectsHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.EnderChestInventoryHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.HotbarHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.ImageHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.InventoryHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.InventoryStatisticsHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.KeyBindsHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.KeystrokesHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.NotificationsHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.ScoreboardHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.TabGuiHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.TacoHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.TargetHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.TextHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.WatermarkHudComponent
import com.github.eedcd.solidanchor.ui.hud.components.minimap.MinimapHudComponent

@AddonApi
object HudComponentManager {

    private val registeredComponents = CopyOnWriteArrayList<NativeHudComponent>()

    private val builtInComponents: List<NativeHudComponent> by lazy {
        listOf<NativeHudComponent>(
            MinimapHudComponent,
            WatermarkHudComponent,
            TabGuiHudComponent,
            ArrayListHudComponent,
            NotificationsHudComponent,
            HotbarHudComponent,
            ScoreboardHudComponent,
            ArmorItemsHudComponent,
            InventoryStatisticsHudComponent,
            InventoryHudComponent,
            CraftingInventoryHudComponent,
            EnderChestInventoryHudComponent,
            TargetHudComponent,
            BlockCounterHudComponent,
            EffectsHudComponent,
            KeystrokesHudComponent,
            TacoHudComponent,
            ImageHudComponent,
            TextHudComponent,
            KeyBindsHudComponent,
            ClosedCaptionsHudComponent,
        )
    }

    val nativeComponents: List<NativeHudComponent>
        get() = builtInComponents + registeredComponents

    @AddonApi
    fun register(component: NativeHudComponent) {
        registeredComponents.addIfAbsent(component)
    }

    @AddonApi
    fun unregister(component: NativeHudComponent) {
        registeredComponents.remove(component)
    }

    @JvmStatic
    fun isTweakEnabled(tweak: HudComponentTweak): Boolean = ModuleHud.running &&
        nativeComponents.any { component ->
            component.enabled && component.tweaks.contains(tweak)
        }

    @JvmStatic
    fun getComponentWithTweak(tweak: HudComponentTweak): HudComponent? {
        if (!ModuleHud.running) {
            return null
        }

        return nativeComponents.find { component ->
            component.enabled && component.tweaks.contains(tweak)
        }
    }

    fun bringComponentToFront(component: HudComponent): Int {
        val zIndex = (nativeComponents.maxOfOrNull { it.zIndex } ?: 0) + 1
        component.zIndex = zIndex
        return zIndex
    }

}
