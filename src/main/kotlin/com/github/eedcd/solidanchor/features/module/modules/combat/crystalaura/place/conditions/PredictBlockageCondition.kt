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
package net.solidanchor.features.module.modules.combat.crystalaura.place.conditions

import net.solidanchor.features.module.modules.combat.crystalaura.PredictFeature
import net.solidanchor.features.module.modules.combat.crystalaura.place.CandidateCache
import net.solidanchor.features.module.modules.combat.crystalaura.place.PlacementCondition
import net.solidanchor.features.module.modules.combat.crystalaura.place.PlacementContext
import net.solidanchor.utils.math.plus
import net.minecraft.core.BlockPos

/**
 * Uses the prediction to check if the future crystal will be blocked by players in the next ticks.
 */
object PredictBlockageCondition : PlacementCondition {

    override fun isValid(context: PlacementContext, cache: CandidateCache, candidate: BlockPos): Boolean {
        val up = cache.up
        return !PredictFeature.willBeBlocked(
            context.expectedCrystal + up,
            context.target,
            !cache.canPlace
        )
    }

}
