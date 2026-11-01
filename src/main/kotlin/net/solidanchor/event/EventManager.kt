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
package net.solidanchor.event

import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.solidanchor.event.events.AccountManagerAdditionResultEvent
import net.solidanchor.event.events.AccountManagerLoginResultEvent
import net.solidanchor.event.events.AccountManagerMessageEvent
import net.solidanchor.event.events.AccountManagerRemovalResultEvent
import net.solidanchor.event.events.AllowAutoJumpEvent
import net.solidanchor.event.events.AttackEntityEvent
import net.solidanchor.event.events.BedStateChangeEvent
import net.solidanchor.event.events.BlinkPacketEvent
import net.solidanchor.event.events.BlockAttackEvent
import net.solidanchor.event.events.BlockBreakingProgressEvent
import net.solidanchor.event.events.BlockChangeEvent
import net.solidanchor.event.events.BlockCountChangeEvent
import net.solidanchor.event.events.BlockShapeEvent
import net.solidanchor.event.events.BlockSlipperinessMultiplierEvent
import net.solidanchor.event.events.BlockVelocityMultiplierEvent
import net.solidanchor.event.events.CancelBlockBreakingEvent
import net.solidanchor.event.events.ChatReceiveEvent
import net.solidanchor.event.events.ChatSendEvent
import net.solidanchor.event.events.ChunkDeltaUpdateEvent
import net.solidanchor.event.events.ChunkLoadEvent
import net.solidanchor.event.events.ChunkUnloadEvent
import net.solidanchor.event.events.ClickGuiScaleChangeEvent
import net.solidanchor.event.events.ClickGuiValueChangeEvent
import net.solidanchor.event.events.ClientLanguageChangedEvent
import net.solidanchor.event.events.ClientShutdownEvent
import net.solidanchor.event.events.ClientStartEvent
import net.solidanchor.event.events.ClosedCaptionsEvent
import net.solidanchor.event.events.DeathEvent
import net.solidanchor.event.events.DisconnectEvent
import net.solidanchor.event.events.DrawOutlinesEvent
import net.solidanchor.event.events.EntityEquipmentChangeEvent
import net.solidanchor.event.events.EntityHealthUpdateEvent
import net.solidanchor.event.events.EntityMarginEvent
import net.solidanchor.event.events.FluidPushEvent
import net.solidanchor.event.events.FpsChangeEvent
import net.solidanchor.event.events.FpsLimitEvent
import net.solidanchor.event.events.FramebufferResizeEvent
import net.solidanchor.event.events.GameModeChangeEvent
import net.solidanchor.event.events.GameRenderEvent
import net.solidanchor.event.events.GameRenderTaskQueueEvent
import net.solidanchor.event.events.GameTickEvent
import net.solidanchor.event.events.HealthUpdateEvent
import net.solidanchor.event.events.InputHandleEvent
import net.solidanchor.event.events.ItemLoreQueryEvent
import net.solidanchor.event.events.KeyEvent
import net.solidanchor.event.events.KeybindChangeEvent
import net.solidanchor.event.events.KeybindIsPressedEvent
import net.solidanchor.event.events.KeyboardCharEvent
import net.solidanchor.event.events.KeyboardKeyEvent
import net.solidanchor.event.events.ModuleActivationEvent
import net.solidanchor.event.events.FriendChangeEvent
import net.solidanchor.event.events.ModuleToggleEvent
import net.solidanchor.event.events.MouseButtonEvent
import net.solidanchor.event.events.MouseCursorEvent
import net.solidanchor.event.events.MouseRotationEvent
import net.solidanchor.event.events.MouseScrollEvent
import net.solidanchor.event.events.MouseScrollInHotbarEvent
import net.solidanchor.event.events.MovementInputEvent
import net.solidanchor.event.events.NotificationEvent
import net.solidanchor.event.events.OverlayMessageEvent
import net.solidanchor.event.events.OverlayRenderEvent
import net.solidanchor.event.events.PacketEvent
import net.solidanchor.event.events.PerspectiveEvent
import net.solidanchor.event.events.PipelineEvent
import net.solidanchor.event.events.PlayerAfterJumpEvent
import net.solidanchor.event.events.PlayerContainerInputEvent
import net.solidanchor.event.events.PlayerFluidCollisionCheckEvent
import net.solidanchor.event.events.PlayerInteractItemEvent
import net.solidanchor.event.events.PlayerInteractedItemEvent
import net.solidanchor.event.events.PlayerJumpEvent
import net.solidanchor.event.events.PlayerMoveEvent
import net.solidanchor.event.events.PlayerMovementTickEvent
import net.solidanchor.event.events.PlayerNetworkMovementTickEvent
import net.solidanchor.event.events.PlayerPostTickEvent
import net.solidanchor.event.events.PlayerPushOutEvent
import net.solidanchor.event.events.PlayerSafeWalkEvent
import net.solidanchor.event.events.PlayerSneakMultiplier
import net.solidanchor.event.events.PlayerStepEvent
import net.solidanchor.event.events.PlayerStepSuccessEvent
import net.solidanchor.event.events.PlayerStrideEvent
import net.solidanchor.event.events.PlayerTickEvent
import net.solidanchor.event.events.PlayerUseMultiplier
import net.solidanchor.event.events.PlayerVelocityStrafe
import net.solidanchor.event.events.RefreshArrayListEvent
import net.solidanchor.event.events.ResourceReloadEvent
import net.solidanchor.event.events.RotationUpdateEvent
import net.solidanchor.event.events.ScaleFactorChangeEvent
import net.solidanchor.event.events.ScheduleInventoryActionEvent
import net.solidanchor.event.events.ScreenEvent
import net.solidanchor.event.events.ScreenRenderEvent
import net.solidanchor.event.events.SelectHotbarSlotSilentlyEvent
import net.solidanchor.event.events.ServerConnectEvent
import net.solidanchor.event.events.ServerPingedEvent
import net.solidanchor.event.events.SessionEvent
import net.solidanchor.event.events.SpaceSeperatedNamesChangeEvent
import net.solidanchor.event.events.SprintEvent
import net.solidanchor.event.events.TagEntityEvent
import net.solidanchor.event.events.TargetChangeEvent
import net.solidanchor.event.events.TickPacketProcessEvent
import net.solidanchor.event.events.TitleEvent
import net.solidanchor.event.events.UseCooldownEvent
import net.solidanchor.event.events.ValueChangedEvent
import net.solidanchor.event.events.WindowResizeEvent
import net.solidanchor.event.events.WindowTitleEvent
import net.solidanchor.event.events.WorldChangeEvent
import net.solidanchor.event.events.WorldEntityRemoveEvent
import net.solidanchor.event.events.WorldFeatureSubmitEvent
import net.solidanchor.event.events.WorldRenderEvent
import net.solidanchor.annotations.Tag
import net.solidanchor.features.addon.AddonApi
import net.solidanchor.features.misc.SelfDestruct.isDestructed
import net.solidanchor.utils.client.error.ErrorHandler
import net.solidanchor.utils.client.logger
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
