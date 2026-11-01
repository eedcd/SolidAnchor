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

package net.solidanchor.event.events

import net.solidanchor.annotations.Tag
import net.solidanchor.config.types.Value
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.event.CancellableEvent
import net.solidanchor.event.Event
import net.solidanchor.features.addon.AddonApi
import net.solidanchor.utils.block.bed.BedState
import net.solidanchor.utils.inventory.InventoryAction
import net.solidanchor.utils.inventory.InventoryConstraints
import net.solidanchor.utils.kotlin.Priority
import net.solidanchor.utils.kotlin.unmodifiable
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Block

@Deprecated(
    "The `clickGuiScaleChange` event has been deprecated.",
    ReplaceWith("ClickGuiScaleChangeEvent"),
    DeprecationLevel.WARNING
)
@Tag("clickGuiScaleChange")
class ClickGuiScaleChangeEvent(val value: Float) : Event()

@Tag("clickGuiValueChange")
class ClickGuiValueChangeEvent(val configurable: ValueGroup) : Event()

@Tag("spaceSeperatedNamesChange")
class SpaceSeperatedNamesChangeEvent(val value: Boolean) : Event()

@Tag("clientStart")
object ClientStartEvent : Event()

@Tag("clientShutdown")
object ClientShutdownEvent : Event()

@Tag("clientLanguageChanged")
class ClientLanguageChangedEvent : Event()

@Tag("valueChanged")
class ValueChangedEvent(val value: Value<*>) : Event()

@Tag("moduleActivation")
class ModuleActivationEvent(val moduleName: String) : Event()

@AddonApi
@Tag("moduleToggle")
class ModuleToggleEvent(val moduleName: String, val hidden: Boolean, val enabled: Boolean) : Event()

@AddonApi
@Tag("refreshArrayList")
object RefreshArrayListEvent : Event()

@AddonApi
@Tag("friendChange")
class FriendChangeEvent(val name: String, val added: Boolean) : Event()

@AddonApi
@Tag("notification")
class NotificationEvent(val title: String, val message: String, val severity: Severity) : Event() {
    @AddonApi
    enum class Severity {
        INFO, SUCCESS, ERROR, ENABLED, DISABLED
    }
}

@Tag("gameModeChange")
class GameModeChangeEvent(val gameMode: GameType) : Event()

@Tag("targetChange")
class TargetChangeEvent(val target: LivingEntity?) : Event()

@Tag("blockCountChange")
class BlockCountChangeEvent(val nextBlock: Block?, val count: Int?) : Event()

@Tag("bedStateChange")
class BedStateChangeEvent(val bedStates: Collection<BedState>) : Event()

@Tag("accountManagerMessage")
class AccountManagerMessageEvent(val message: String) : Event()

@Tag("accountManagerLogin")
class AccountManagerLoginResultEvent(val username: String? = null, val error: String? = null) : Event()

@Tag("accountManagerAddition")
class AccountManagerAdditionResultEvent(
    val username: String? = null, val error: String? = null
) : Event()

@Tag("accountManagerRemoval")
class AccountManagerRemovalResultEvent(val username: String?) : Event()

@Tag("serverPinged")
class ServerPingedEvent(val server: ServerData) : Event()

@Tag("rotationUpdate")
object RotationUpdateEvent : Event()

@Tag("resourceReload")
object ResourceReloadEvent : Event()

@Tag("scaleFactorChange")
class ScaleFactorChangeEvent(val scaleFactor: Int) : Event()

@Tag("scheduleInventoryAction")
class ScheduleInventoryActionEvent(val schedule: MutableList<InventoryAction.Chain> = mutableListOf()) : Event() {

    fun schedule(
        constrains: InventoryConstraints,
        action: InventoryAction,
        priority: Priority = Priority.NORMAL
    ) {
        this.schedule.add(InventoryAction.Chain(constrains, listOf(action), priority))
    }

    fun schedule(
        constrains: InventoryConstraints,
        vararg actions: InventoryAction,
        priority: Priority = Priority.NORMAL
    ) {
        this.schedule.add(InventoryAction.Chain(constrains, actions.unmodifiable(), priority))
    }

    fun schedule(
        constrains: InventoryConstraints,
        actions: List<InventoryAction>,
        priority: Priority = Priority.NORMAL
    ) {
        this.schedule.add(InventoryAction.Chain(constrains, actions, priority))
    }
}

@Tag("selectHotbarSlotSilently")
class SelectHotbarSlotSilentlyEvent(val requester: Any?, val slot: Int): CancellableEvent()
