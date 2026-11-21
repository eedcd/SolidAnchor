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

package net.solidanchor.ui.hud

import java.util.concurrent.CopyOnWriteArrayList
import net.solidanchor.features.addon.AddonApi
import net.solidanchor.features.module.modules.render.ModuleHud
import net.solidanchor.ui.hud.components.ArmorItemsHudComponent
import net.solidanchor.ui.hud.components.ArrayListHudComponent
import net.solidanchor.ui.hud.components.BlockCounterHudComponent
import net.solidanchor.ui.hud.components.ClosedCaptionsHudComponent
import net.solidanchor.ui.hud.components.CraftingInventoryHudComponent
import net.solidanchor.ui.hud.components.EffectsHudComponent
import net.solidanchor.ui.hud.components.EnderChestInventoryHudComponent
import net.solidanchor.ui.hud.components.HotbarHudComponent
import net.solidanchor.ui.hud.components.ImageHudComponent
import net.solidanchor.ui.hud.components.InventoryHudComponent
import net.solidanchor.ui.hud.components.InventoryStatisticsHudComponent
import net.solidanchor.ui.hud.components.KeyBindsHudComponent
import net.solidanchor.ui.hud.components.KeystrokesHudComponent
import net.solidanchor.ui.hud.components.NotificationsHudComponent
import net.solidanchor.ui.hud.components.ScoreboardHudComponent
import net.solidanchor.ui.hud.components.TabGuiHudComponent
import net.solidanchor.ui.hud.components.TacoHudComponent
import net.solidanchor.ui.hud.components.TargetHudComponent
import net.solidanchor.ui.hud.components.TextHudComponent
import net.solidanchor.ui.hud.components.WatermarkHudComponent
import net.solidanchor.ui.hud.components.minimap.MinimapHudComponent

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
