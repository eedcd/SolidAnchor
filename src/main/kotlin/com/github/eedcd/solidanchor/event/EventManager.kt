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
package com.github.eedcd.solidanchor.event

import com.github.eedcd.solidanchor.annotations.Tag
import com.github.eedcd.solidanchor.event.events.AccountManagerAdditionResultEvent
import com.github.eedcd.solidanchor.event.events.AccountManagerLoginResultEvent
import com.github.eedcd.solidanchor.event.events.AccountManagerMessageEvent
import com.github.eedcd.solidanchor.event.events.AccountManagerRemovalResultEvent
import com.github.eedcd.solidanchor.event.events.AllowAutoJumpEvent
import com.github.eedcd.solidanchor.event.events.AttackEntityEvent
import com.github.eedcd.solidanchor.event.events.BedStateChangeEvent
import com.github.eedcd.solidanchor.event.events.BlinkPacketEvent
import com.github.eedcd.solidanchor.event.events.BlockAttackEvent
import com.github.eedcd.solidanchor.event.events.BlockBreakingProgressEvent
import com.github.eedcd.solidanchor.event.events.BlockChangeEvent
import com.github.eedcd.solidanchor.event.events.BlockCountChangeEvent
import com.github.eedcd.solidanchor.event.events.BlockShapeEvent
import com.github.eedcd.solidanchor.event.events.BlockSlipperinessMultiplierEvent
import com.github.eedcd.solidanchor.event.events.BlockVelocityMultiplierEvent
import com.github.eedcd.solidanchor.event.events.CancelBlockBreakingEvent
import com.github.eedcd.solidanchor.event.events.ChatReceiveEvent
import com.github.eedcd.solidanchor.event.events.ChatSendEvent
import com.github.eedcd.solidanchor.event.events.ChunkDeltaUpdateEvent
import com.github.eedcd.solidanchor.event.events.ChunkLoadEvent
import com.github.eedcd.solidanchor.event.events.ChunkUnloadEvent
import com.github.eedcd.solidanchor.event.events.ClickGuiScaleChangeEvent
import com.github.eedcd.solidanchor.event.events.ClickGuiValueChangeEvent
import com.github.eedcd.solidanchor.event.events.ClientLanguageChangedEvent
import com.github.eedcd.solidanchor.event.events.ClientShutdownEvent
import com.github.eedcd.solidanchor.event.events.ClientStartEvent
import com.github.eedcd.solidanchor.event.events.ClosedCaptionsEvent
import com.github.eedcd.solidanchor.event.events.DeathEvent
import com.github.eedcd.solidanchor.event.events.DisconnectEvent
import com.github.eedcd.solidanchor.event.events.DrawOutlinesEvent
import com.github.eedcd.solidanchor.event.events.EntityEquipmentChangeEvent
import com.github.eedcd.solidanchor.event.events.EntityHealthUpdateEvent
import com.github.eedcd.solidanchor.event.events.EntityMarginEvent
import com.github.eedcd.solidanchor.event.events.FluidPushEvent
import com.github.eedcd.solidanchor.event.events.FpsChangeEvent
import com.github.eedcd.solidanchor.event.events.FpsLimitEvent
import com.github.eedcd.solidanchor.event.events.FramebufferResizeEvent
import com.github.eedcd.solidanchor.event.events.FriendChangeEvent
import com.github.eedcd.solidanchor.event.events.GameModeChangeEvent
import com.github.eedcd.solidanchor.event.events.GameRenderEvent
import com.github.eedcd.solidanchor.event.events.GameRenderTaskQueueEvent
import com.github.eedcd.solidanchor.event.events.GameTickEvent
import com.github.eedcd.solidanchor.event.events.HealthUpdateEvent
import com.github.eedcd.solidanchor.event.events.InputHandleEvent
import com.github.eedcd.solidanchor.event.events.ItemLoreQueryEvent
import com.github.eedcd.solidanchor.event.events.KeyEvent
import com.github.eedcd.solidanchor.event.events.KeybindChangeEvent
import com.github.eedcd.solidanchor.event.events.KeybindIsPressedEvent
import com.github.eedcd.solidanchor.event.events.KeyboardCharEvent
import com.github.eedcd.solidanchor.event.events.KeyboardKeyEvent
import com.github.eedcd.solidanchor.event.events.ModuleActivationEvent
import com.github.eedcd.solidanchor.event.events.ModuleToggleEvent
import com.github.eedcd.solidanchor.event.events.MouseButtonEvent
import com.github.eedcd.solidanchor.event.events.MouseCursorEvent
import com.github.eedcd.solidanchor.event.events.MouseRotationEvent
import com.github.eedcd.solidanchor.event.events.MouseScrollEvent
import com.github.eedcd.solidanchor.event.events.MouseScrollInHotbarEvent
import com.github.eedcd.solidanchor.event.events.MovementInputEvent
import com.github.eedcd.solidanchor.event.events.NotificationEvent
import com.github.eedcd.solidanchor.event.events.OverlayMessageEvent
import com.github.eedcd.solidanchor.event.events.OverlayRenderEvent
import com.github.eedcd.solidanchor.event.events.PacketEvent
import com.github.eedcd.solidanchor.event.events.PerspectiveEvent
import com.github.eedcd.solidanchor.event.events.PipelineEvent
import com.github.eedcd.solidanchor.event.events.PlayerAfterJumpEvent
import com.github.eedcd.solidanchor.event.events.PlayerContainerInputEvent
import com.github.eedcd.solidanchor.event.events.PlayerFluidCollisionCheckEvent
import com.github.eedcd.solidanchor.event.events.PlayerInteractItemEvent
import com.github.eedcd.solidanchor.event.events.PlayerInteractedItemEvent
import com.github.eedcd.solidanchor.event.events.PlayerJumpEvent
import com.github.eedcd.solidanchor.event.events.PlayerMoveEvent
import com.github.eedcd.solidanchor.event.events.PlayerMovementTickEvent
import com.github.eedcd.solidanchor.event.events.PlayerNetworkMovementTickEvent
import com.github.eedcd.solidanchor.event.events.PlayerPostTickEvent
import com.github.eedcd.solidanchor.event.events.PlayerPushOutEvent
import com.github.eedcd.solidanchor.event.events.PlayerSafeWalkEvent
import com.github.eedcd.solidanchor.event.events.PlayerSneakMultiplier
import com.github.eedcd.solidanchor.event.events.PlayerStepEvent
import com.github.eedcd.solidanchor.event.events.PlayerStepSuccessEvent
import com.github.eedcd.solidanchor.event.events.PlayerStrideEvent
import com.github.eedcd.solidanchor.event.events.PlayerTickEvent
import com.github.eedcd.solidanchor.event.events.PlayerUseMultiplier
import com.github.eedcd.solidanchor.event.events.PlayerVelocityStrafe
import com.github.eedcd.solidanchor.event.events.RefreshArrayListEvent
import com.github.eedcd.solidanchor.event.events.ResourceReloadEvent
import com.github.eedcd.solidanchor.event.events.RotationUpdateEvent
import com.github.eedcd.solidanchor.event.events.ScaleFactorChangeEvent
import com.github.eedcd.solidanchor.event.events.ScheduleInventoryActionEvent
import com.github.eedcd.solidanchor.event.events.ScreenEvent
import com.github.eedcd.solidanchor.event.events.ScreenRenderEvent
import com.github.eedcd.solidanchor.event.events.SelectHotbarSlotSilentlyEvent
import com.github.eedcd.solidanchor.event.events.ServerConnectEvent
import com.github.eedcd.solidanchor.event.events.ServerPingedEvent
import com.github.eedcd.solidanchor.event.events.SessionEvent
import com.github.eedcd.solidanchor.event.events.SpaceSeperatedNamesChangeEvent
import com.github.eedcd.solidanchor.event.events.SprintEvent
import com.github.eedcd.solidanchor.event.events.TagEntityEvent
import com.github.eedcd.solidanchor.event.events.TargetChangeEvent
import com.github.eedcd.solidanchor.event.events.TickPacketProcessEvent
import com.github.eedcd.solidanchor.event.events.TitleEvent
import com.github.eedcd.solidanchor.event.events.UseCooldownEvent
import com.github.eedcd.solidanchor.event.events.ValueChangedEvent
import com.github.eedcd.solidanchor.event.events.WindowResizeEvent
import com.github.eedcd.solidanchor.event.events.WindowTitleEvent
import com.github.eedcd.solidanchor.event.events.WorldChangeEvent
import com.github.eedcd.solidanchor.event.events.WorldEntityRemoveEvent
import com.github.eedcd.solidanchor.event.events.WorldFeatureSubmitEvent
import com.github.eedcd.solidanchor.event.events.WorldRenderEvent
import com.github.eedcd.solidanchor.features.addon.AddonApi
import com.github.eedcd.solidanchor.features.misc.SelfDestruct.isDestructed
import com.github.eedcd.solidanchor.utils.client.error.ErrorHandler
import com.github.eedcd.solidanchor.utils.client.logger
import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.minecraft.ReportedException

/**
 * Contains all classes of events. Used to create lookup tables ahead of time
 */
@JvmField
internal val ALL_EVENT_CLASSES: Array<Class<out Event>> = arrayOf(
    GameTickEvent::class.java,
    GameRenderTaskQueueEvent::class.java,
    TickPacketProcessEvent::class.java,
    BlockChangeEvent::class.java,
    ChunkLoadEvent::class.java,
    ChunkDeltaUpdateEvent::class.java,
    ChunkUnloadEvent::class.java,
    DisconnectEvent::class.java,
    GameRenderEvent::class.java,
    WorldFeatureSubmitEvent::class.java,
    WorldRenderEvent::class.java,
    OverlayRenderEvent::class.java,
    ScreenRenderEvent::class.java,
    WindowResizeEvent::class.java,
    FramebufferResizeEvent::class.java,
    WindowTitleEvent::class.java,
    MouseButtonEvent::class.java,
    MouseScrollEvent::class.java,
    MouseCursorEvent::class.java,
    KeyboardKeyEvent::class.java,
    KeyboardCharEvent::class.java,
    InputHandleEvent::class.java,
    MovementInputEvent::class.java,
    SprintEvent::class.java,
    KeyEvent::class.java,
    MouseRotationEvent::class.java,
    KeybindChangeEvent::class.java,
    KeybindIsPressedEvent::class.java,
    AttackEntityEvent::class.java,
    SessionEvent::class.java,
    ScreenEvent::class.java,
    ChatSendEvent::class.java,
    ChatReceiveEvent::class.java,
    UseCooldownEvent::class.java,
    BlockShapeEvent::class.java,
    BlockBreakingProgressEvent::class.java,
    BlockVelocityMultiplierEvent::class.java,
    BlockSlipperinessMultiplierEvent::class.java,
    EntityMarginEvent::class.java,
    EntityHealthUpdateEvent::class.java,
    HealthUpdateEvent::class.java,
    DeathEvent::class.java,
    PlayerTickEvent::class.java,
    PlayerPostTickEvent::class.java,
    PlayerMovementTickEvent::class.java,
    PlayerNetworkMovementTickEvent::class.java,
    PlayerPushOutEvent::class.java,
    PlayerMoveEvent::class.java,
    PlayerJumpEvent::class.java,
    PlayerAfterJumpEvent::class.java,
    PlayerUseMultiplier::class.java,
    PlayerInteractItemEvent::class.java,
    PlayerInteractedItemEvent::class.java,
    PlayerVelocityStrafe::class.java,
    PlayerStrideEvent::class.java,
    PlayerSafeWalkEvent::class.java,
    CancelBlockBreakingEvent::class.java,
    PlayerStepEvent::class.java,
    PlayerStepSuccessEvent::class.java,
    FluidPushEvent::class.java,
    PipelineEvent::class.java,
    PacketEvent::class.java,
    ClientStartEvent::class.java,
    ClientShutdownEvent::class.java,
    ClientLanguageChangedEvent::class.java,
    ValueChangedEvent::class.java,
    ModuleActivationEvent::class.java,
    ModuleToggleEvent::class.java,
    FriendChangeEvent::class.java,
    NotificationEvent::class.java,
    WorldChangeEvent::class.java,
    AccountManagerMessageEvent::class.java,
    AccountManagerAdditionResultEvent::class.java,
    AccountManagerRemovalResultEvent::class.java,
    AccountManagerLoginResultEvent::class.java,
    FpsChangeEvent::class.java,
    FpsLimitEvent::class.java,
    RotationUpdateEvent::class.java,
    RefreshArrayListEvent::class.java,
    ServerConnectEvent::class.java,
    ServerPingedEvent::class.java,
    TargetChangeEvent::class.java,
    BlockCountChangeEvent::class.java,
    BedStateChangeEvent::class.java,
    GameModeChangeEvent::class.java,
    ResourceReloadEvent::class.java,
    ScaleFactorChangeEvent::class.java,
    DrawOutlinesEvent::class.java,
    OverlayMessageEvent::class.java,
    ScheduleInventoryActionEvent::class.java,
    SelectHotbarSlotSilentlyEvent::class.java,
    SpaceSeperatedNamesChangeEvent::class.java,
    ClickGuiScaleChangeEvent::class.java,
    TagEntityEvent::class.java,
    MouseScrollInHotbarEvent::class.java,
    PlayerFluidCollisionCheckEvent::class.java,
    PlayerContainerInputEvent::class.java,
    PlayerSneakMultiplier::class.java,
    PerspectiveEvent::class.java,
    ItemLoreQueryEvent::class.java,
    EntityEquipmentChangeEvent::class.java,
    ClickGuiValueChangeEvent::class.java,
    BlockAttackEvent::class.java,
    BlinkPacketEvent::class.java,
    AllowAutoJumpEvent::class.java,
    WorldEntityRemoveEvent::class.java,
    TitleEvent.Title::class.java,
    TitleEvent.Subtitle::class.java,
    TitleEvent.Fade::class.java,
    TitleEvent.Clear::class.java,
    ClosedCaptionsEvent::class.java,
)

inline fun <reified E : Event> eventFlow(): SharedFlow<E> =
    EventManager.eventFlow(E::class.java)

/**
 * Swapped as one object, so readers never see the tables disagree.
 */
private class EventTables(@JvmField val classes: Set<Class<out Event>>, previous: EventTables?) {

    @JvmField
    val registry: Map<Class<out Event>, EventHookRegistry<in Event>> = classes.associateWithTo(
        Reference2ObjectOpenHashMap(classes.size)
    ) { previous?.registry?.get(it) ?: EventHookRegistry() }

    @JvmField
    val flows: Map<Class<out Event>, MutableSharedFlow<Event>> = classes.associateWithTo(
        Reference2ObjectOpenHashMap(classes.size)
    ) { previous?.flows?.get(it) ?: MutableSharedFlow(replay = 0, extraBufferCapacity = 0) }

    @JvmField
    val classToName: Map<Class<out Event>, String> =
        Reference2ObjectOpenHashMap<Class<out Event>, String>(classes.size).apply {
            classes.forEach { eventClass ->
                eventClass.getAnnotation(Tag::class.java)?.let { put(eventClass, it.name) }
            }
        }

    @JvmField
    val nameToClass: Map<String, Class<out Event>> =
        Object2ReferenceRBTreeMap<String, Class<out Event>>(String.CASE_INSENSITIVE_ORDER).apply {
            classToName.forEach { (eventClass, name) -> put(name, eventClass) }
        }

}

/**
 * A modern and fast event handler using lambda handlers
 */
@AddonApi
object EventManager {

    @Volatile
    private var tables = EventTables(ALL_EVENT_CLASSES.toCollection(LinkedHashSet()), previous = null)

    val knownEventClasses: Set<Class<out Event>>
        get() = tables.classes

    /**
     * Looks up by [Tag] name, ignoring case.
     */
    fun eventClassByName(name: String): Class<out Event>? = tables.nameToClass[name]

    internal fun eventNameOrNull(eventClass: Class<out Event>): String? = tables.classToName[eventClass]

    @Synchronized
    fun registerEventClass(eventClass: Class<out Event>): Boolean {
        val current = tables
        if (eventClass in current.classes) {
            return false
        }

        eventClass.getAnnotation(Tag::class.java)?.let { tag ->
            val owner = current.nameToClass[tag.name]
            require(owner == null) {
                "Event name '${tag.name}' is already taken by ${owner!!.name}, " +
                    "cannot register ${eventClass.name}"
            }
        }

        tables = EventTables(LinkedHashSet(current.classes).apply { add(eventClass) }, current)
        return true
    }

    private fun tablesContaining(eventClass: Class<out Event>): EventTables {
        val current = tables
        if (eventClass in current.classes) {
            return current
        }

        registerEventClass(eventClass)
        return tables
    }

    /**
     * Used by handler methods
     */
    fun <T : Event> registerEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>): EventHook<T> {
        val handlers = tablesContaining(eventClass).registry.getValue(eventClass)

        @Suppress("UNCHECKED_CAST")
        val hook = eventHook as EventHook<in Event>

        handlers.addIfAbsent(hook)

        return eventHook
    }

    /**
     * Unregisters a handler.
     */
    fun <T : Event> unregisterEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>) {
        @Suppress("UNCHECKED_CAST")
        tables.registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    fun unregisterEventHandler(eventListener: EventListener) {
        tables.registry.values.forEach {
            it.remove(eventListener)
        }
    }

    fun unregisterAll() {
        tables.registry.values.forEach {
            it.clear()
        }
    }

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun <T : Event> callEvent(event: T): T {
        if (isDestructed) {
            return event
        }

        val eventType = event.javaClass
        val snapshot = tables
        val target = snapshot.registry[eventType] ?: return event

        event.isCompleted = false
        for (eventHook in target.snapshot) {
            @Suppress("UNCHECKED_CAST")
            eventHook as EventHook<T>
            if (!eventHook.handlerClass.running) {
                continue
            }

            try {
                eventHook.handler.accept(event)
            } catch (e: ReportedException) {
                ErrorHandler.fatal(
                    error = e,
                    needToReport = true,
                    additionalMessage = "Event (${eventType.simpleName}) handler of ${eventHook.handlerClass}"
                )
            } catch (e: Throwable) {
                logger.error(
                    "Exception while executing event handler of {}, event={}",
                    eventHook.handlerClass.javaClass.simpleName,
                    event,
                    e,
                )
            }
        }
        event.isCompleted = true

        @Suppress("UNCHECKED_CAST")
        (snapshot.flows.getValue(eventType) as MutableSharedFlow<T>).tryEmit(event)

        return event
    }

    /**
     * Gets a [SharedFlow] for the given event class.
     * The flow receives the event instances after all [EventHook]s are executed.
     * So the [Event.isCompleted] will be true when the event is emitted.
     */
    fun <T : Event> eventFlow(eventClass: Class<T>): SharedFlow<T> {
        @Suppress("UNCHECKED_CAST")
        return tablesContaining(eventClass).flows.getValue(eventClass) as SharedFlow<T>
    }
}
