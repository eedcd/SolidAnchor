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

package net.solidanchor.features.module.modules.player.autobuff

import net.solidanchor.config.types.group.ToggleableValueGroup
import net.solidanchor.config.types.list.Tagged
import net.solidanchor.event.events.ScheduleInventoryActionEvent
import net.solidanchor.event.handler
import net.solidanchor.event.tickHandler
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.modules.player.autobuff.features.Drink
import net.solidanchor.features.module.modules.player.autobuff.features.Gapple
import net.solidanchor.features.module.modules.player.autobuff.features.Head
import net.solidanchor.features.module.modules.player.autobuff.features.Pot
import net.solidanchor.features.module.modules.player.autobuff.features.Refill
import net.solidanchor.features.module.modules.player.autobuff.features.Soup
import net.solidanchor.utils.aiming.RotationsValueGroup
import net.solidanchor.utils.client.SilentHotbar
import net.solidanchor.utils.combat.CombatManager

object ModuleAutoBuff : ClientModule(
    name = "AutoBuff",
    category = ModuleCategories.PLAYER,
    aliases = listOf("AutoPot", "AutoGapple", "AutoSoup")
) {

    /**
     * All buff features
     */
    private val features = arrayOf(
        Soup,
        Head,
        Pot,
        Drink,
        Gapple
    )

    init {
        // Register features to configurable
        features.forEach(this::tree)
    }

    /**
     * Auto Swap will automatically swap your selected slot to the best item for the situation.
     * For example, if you're low on health, it will swap to the next health pot.
     *
     * It also allows to customize the delay between each swap.
     */
    internal object AutoSwap : ToggleableValueGroup(ModuleAutoBuff, "AutoSwap", true) {

        /**
         * How long should we wait after swapping to the item?
         */
        val delayIn by intRange("DelayIn", 1..1, 0..20, "ticks")

        /**
         * How long should we wait after using the item?
         */
        val delayOut by intRange("DelayOut", 1..1, 0..20, "ticks")

    }

    init {
        tree(AutoSwap)
        tree(Refill)
    }

    /**
     * Rotation Configurable for every feature that depends on rotation change
     */
    internal object Rotations : RotationsValueGroup(this) {

        val rotationTiming by enumChoice("RotationTiming", RotationTimingMode.NORMAL)

        enum class RotationTimingMode(override val tag: String) : Tagged {
            NORMAL("Normal"),
            ON_TICK("OnTick"),
            ON_USE("OnUse")
        }

    }

    init {
        tree(Rotations)
    }

    internal val combatPauseTime by int("CombatPauseTime", 0, 0..40, "ticks")
    private val notDuringCombat by boolean("NotDuringCombat", false)

    internal val activeFeatures
        get() = features.filter { it.enabled }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (notDuringCombat && CombatManager.isInCombat) {
            return@tickHandler
        }

        if (player.isDeadOrDying || player.isCreative || player.isSpectator || player.tickCount < 20) {
            return@tickHandler
        }

        for (feature in activeFeatures) {
            with(feature) {
                if (runIfPossible()) {
                    return@tickHandler
                }
            }
        }
    }

    @Suppress("unused")
    private val refiller = handler<ScheduleInventoryActionEvent> {
        // Move valid buff items from the inventory into empty hotbar slots whenever inventory actions are scheduled
        if (Refill.enabled) {
            Refill.execute(it)
        }
    }

    override fun onDisabled() {
        SilentHotbar.resetSlot(ModuleAutoBuff)
        super.onDisabled()
    }

}
