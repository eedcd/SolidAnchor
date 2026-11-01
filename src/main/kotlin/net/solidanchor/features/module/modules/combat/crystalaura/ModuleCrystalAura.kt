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
package net.solidanchor.features.module.modules.combat.crystalaura

import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.event.events.RotationUpdateEvent
import net.solidanchor.event.handler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.combat.crystalaura.destroy.SubmoduleCrystalDestroyer
import net.solidanchor.features.module.modules.combat.crystalaura.place.SubmoduleCrystalPlacer
import net.solidanchor.features.module.modules.combat.crystalaura.post.CrystalPostAttackTracker
import net.solidanchor.features.module.modules.combat.crystalaura.post.SubmoduleSetDead
import net.solidanchor.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer
import net.solidanchor.utils.aiming.NoRotationMode
import net.solidanchor.utils.aiming.NormalRotationMode
import net.solidanchor.utils.client.FloatValueProvider
import net.solidanchor.utils.combat.CombatManager
import net.solidanchor.utils.combat.TargetTracker
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.render.TargetRenderer

/**
 * Module CrystalAura
 *
 * Automatically places and explodes end crystals.
 *
 * @author ccetl
 */
object ModuleCrystalAura : ClientModule(
    "CrystalAura",
    ModuleCategories.COMBAT,
    aliases = listOf("AutoCrystal"),
    disableOnQuit = true
) {

    val targetTracker = tree(TargetTracker(
        rangeValue = FloatValueProvider("Range", 4.5f, 1f..12f)
    ))

    object PredictFeature : ValueGroup("Predict") {
        init {
            treeAll(SelfPredict, TargetPredict)
        }
    }

    init {
        treeAll(
            SubmoduleCrystalPlacer,
            SubmoduleCrystalDestroyer,
            CrystalAuraDamageOptions,
            CrystalAuraTriggerer,
            PredictFeature,
            SubmoduleIdPredict,
            SubmoduleSetDead,
            SubmoduleBasePlace
        )
    }

    init {
        tree(TargetRenderer(this, targetTracker))
    }

    val rotationMode = modes(this, "RotationMode") {
        arrayOf(
            NormalRotationMode(it, this, Priority.IMPORTANT_FOR_USAGE_2, true),
            NoRotationMode(it, this)
        )
    }

    override fun onDisabled() {
        CrystalAuraTriggerer.terminateRunningTasks()
        SubmoduleCrystalPlacer.placementRenderer.clearSilently()
        SubmoduleCrystalDestroyer.postAttackHandlers.forEach(CrystalPostAttackTracker::onToggle)
        SubmoduleBasePlace.onDisabled()
        CrystalAuraDamageOptions.cacheMap.clear()
    }

    override fun onEnabled() {
        SubmoduleCrystalDestroyer.postAttackHandlers.forEach(CrystalPostAttackTracker::onToggle)
    }

    @Suppress("unused")
    private val simulatedTickHandler = handler<RotationUpdateEvent>(1) {
        CrystalAuraDamageOptions.cacheMap.clear()
        if (CombatManager.shouldPauseCombat) {
            return@handler
        }

        targetTracker.selectFirst()
    }

}
