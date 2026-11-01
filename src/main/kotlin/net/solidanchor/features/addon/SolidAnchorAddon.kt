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
package net.solidanchor.features.addon

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.solidanchor.config.ConfigSystem
import net.solidanchor.config.types.Config
import net.solidanchor.config.types.group.Mode
import net.solidanchor.config.types.group.ModeValueGroup
import net.solidanchor.config.types.group.ValueGroup
import net.solidanchor.event.EventListener
import net.solidanchor.event.EventManager
import net.solidanchor.event.events.RefreshArrayListEvent
import net.solidanchor.features.command.CommandManager
import net.solidanchor.features.command.CommandRegistrar
import net.solidanchor.features.command.brigadier.ClientCommandSource
import net.solidanchor.features.module.ClientModule
import net.solidanchor.features.module.MinecraftShortcuts
import net.solidanchor.features.module.ModuleCategories
import net.solidanchor.features.module.ModuleCategory
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.features.module.modules.render.ModuleClickGui
import net.solidanchor.utils.client.clientLogger
import net.solidanchor.utils.client.error.QuickFix
import net.solidanchor.utils.client.error.QuickFixes
import net.fabricmc.loader.api.ModContainer

@AddonApi
enum class AddonState {
    DISCOVERED,
    LOADED,
    ERRORED,

    /** Skipped by `-Dsolidanchor.disableAddons`. */
    DISABLED,
}

/**
 * Declared under the `solidanchor` entrypoint in `fabric.mod.json`:
 *
 * ```json
 * "entrypoints": { "solidanchor": ["com.example.addon.ExampleAddon"] }
 * ```
 *
 * Register through the `register*` helpers, not [ModuleManager] directly, so a failing add-on can
 * be rolled back.
 */
@AddonApi
@Suppress("TooManyFunctions")
abstract class SolidAnchorAddon : EventListener, MinecraftShortcuts {

    internal lateinit var container: ModContainer

    val metadata: AddonMetadata by lazy { AddonMetadata(container) }

    val id: String get() = metadata.id
    val version: String get() = metadata.version
    val authors: List<String> get() = metadata.authors
    val description: String get() = metadata.description
    val color get() = metadata.color

    open val displayName: String get() = metadata.name

    var state: AddonState = AddonState.DISCOVERED
        internal set

    override val running: Boolean
        get() = super.running && state == AddonState.LOADED

    val logger by lazy { clientLogger("Addon/$id") }

    internal val registeredListeners = mutableListOf<EventListener>()
    internal val registeredModules = mutableListOf<ClientModule>()
    internal val registeredNodes = mutableListOf<LiteralCommandNode<ClientCommandSource>>()
    internal val registeredCategories = mutableListOf<ModuleCategory>()
    internal val registeredModes = mutableListOf<Pair<ModeValueGroup<*>, Mode>>()
    internal val registeredConfigs = mutableListOf<Config>()
    internal val registeredQuickFixes = mutableListOf<QuickFix>()

    /**
     * Registered for all add-ons before any [onInitialize] runs.
     */
    open val categories: List<ModuleCategory> get() = emptyList()

    /**
     * Runs before configs are loaded, so only what is registered here gets its settings restored.
     */
    abstract fun onInitialize()

    /**
     * Runs once configs are loaded.
     */
    open fun onStarted() {}

    /**
     * Runs before configs are written back to disk.
     */
    open fun onStopping() {}

    /**
     * Only tracks [listeners] for rollback; modules and modes are tracked already.
     */
    fun registerListeners(vararg listeners: EventListener) {
        registeredListeners += listeners
    }

    /**
     * For categories that are only known once the add-on runs. Declare the rest in [categories].
     */
    fun registerCategory(category: ModuleCategory): ModuleCategory {
        ModuleCategories.register(category)
        registeredCategories += category
        return category
    }

    fun registerModules(vararg modules: ClientModule) {
        for (module in modules) {
            check(ModuleCategories.byName(module.category.tag) === module.category) {
                "Category '${module.category.tag}' of module '${module.name}' is not registered, " +
                    "declare it in categories"
            }
            ModuleManager.addModule(module)
            registeredModules += module
            // As in registerInbuilt; without it the module has no translation key.
            module.walkKeyPath()
            module.verifyFallbackDescription()
        }
        refreshModuleList()
    }

    fun unregisterModules(vararg modules: ClientModule) {
        for (module in modules) {
            if (registeredModules.remove(module)) {
                ModuleManager.removeModule(module)
            }
        }
        refreshModuleList()
    }

    private fun refreshModuleList() {
        EventManager.callEvent(RefreshArrayListEvent)
        ModuleClickGui.sync()
    }

    fun registerCommand(registrar: CommandRegistrar) {
        // Not CommandManager.register: Brigadier would silently merge a clashing root literal.
        val scratch = CommandDispatcher<ClientCommandSource>()
        registrar.register(scratch)
        registerCommandNodes(scratch.root.children.filterIsInstance<LiteralCommandNode<ClientCommandSource>>())
    }

    fun registerCommandNodes(nodes: Collection<LiteralCommandNode<ClientCommandSource>>) {
        CommandManager.registerNodes(nodes)
        registeredNodes += nodes
    }

    fun <T : Mode> registerMode(parent: ModeValueGroup<T>, mode: T) {
        parent.addMode(mode)
        registeredModes += parent to mode
    }

    /**
     * Offers [quickFix] on the fatal error screen for the errors it matches.
     */
    fun registerQuickFix(quickFix: QuickFix): QuickFix {
        QuickFixes.register(quickFix)
        registeredQuickFixes += quickFix
        return quickFix
    }

    @JvmOverloads
    fun config(
        name: String = id,
        tree: MutableCollection<out ValueGroup> = mutableListOf(),
    ): Config = ConfigSystem.root(name, tree).also { registeredConfigs += it }

    override fun toString(): String = "Addon[$id]"

}
