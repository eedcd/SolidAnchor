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
package net.solidanchor

import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.future
import kotlinx.coroutines.internal.isMissing
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.solidanchor.api.core.httpException
import net.solidanchor.api.thirdparty.IpInfoApi
import net.solidanchor.config.ConfigSystem
import net.solidanchor.config.types.Config
import net.solidanchor.event.EventListener
import net.solidanchor.event.EventManager
import net.solidanchor.features.addon.AddonManager
import net.solidanchor.event.events.ClientShutdownEvent
import net.solidanchor.event.events.ClientStartEvent
import net.solidanchor.event.handler
import net.solidanchor.features.account.AccountManager
import net.solidanchor.features.blink.BlinkManager
import net.solidanchor.features.command.CommandManager
import net.solidanchor.features.creativetab.tabs.HeadsCreativeModeTab
import net.solidanchor.features.global.GlobalManager
import net.solidanchor.features.misc.FriendManager
import net.solidanchor.features.module.ModuleManager
import net.solidanchor.features.spoofer.SpooferManager
import net.solidanchor.lang.LanguageManager
import net.solidanchor.render.FontManager
import net.solidanchor.render.HAS_AMD_VEGA_APU
import net.solidanchor.render.atlas.ItemImageAtlas
import net.solidanchor.render.engine.BlurEffectRenderer
import net.solidanchor.render.gui.ItemStackListRenderer
import net.solidanchor.render.gui.indicator.IndicatorRenderer
import net.solidanchor.utils.aiming.PostRotationExecutor
import net.solidanchor.utils.aiming.RotationManager
import net.solidanchor.utils.block.ChunkScanner
import net.solidanchor.utils.client.GitInfo
import net.solidanchor.utils.client.InteractionTracker
import net.solidanchor.utils.client.ServerObserver
import net.solidanchor.utils.client.clientIdentifier
import net.solidanchor.utils.client.error.ErrorHandler
import net.solidanchor.utils.client.mc
import net.solidanchor.utils.combat.CombatManager
import net.solidanchor.utils.entity.RenderedEntities
import net.solidanchor.utils.input.InputTracker
import net.solidanchor.utils.inventory.EnderChestInventoryTracker
import net.solidanchor.utils.inventory.InventoryManager
import net.solidanchor.utils.io.readText
import net.solidanchor.utils.network.LocalPlayerFallDamageTracker
import net.solidanchor.utils.network.PositionPacketSeparator
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ReloadableResourceManager
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * SolidAnchor
 *
 * A free mixin-based injection hacked-client for Minecraft using FabricMC.
 *
 * @author kawaiinekololis (@team CCBlueX)
 */
object SolidAnchor : EventListener {

    /**
     * CLIENT INFORMATION
     *
     * WARNING: Please read the GNU General Public License
     */
    const val CLIENT_NAME = "SolidAnchor"
    const val CLIENT_AUTHOR = "eedcd"

    private object Client : Config("Client") {
        val version = text("Version", GitInfo.version())
            .immutable()
        val commit = text("Commit", GitInfo.get("git.commit.id.abbrev")?.let { "git-$it" } ?: "unknown")
            .immutable()
        val branch = text("Branch", GitInfo.branch())
            .immutable()

        init {
            ConfigSystem.root(this)

            version.onChange { previousVersion ->
                runCatching {
                    ConfigSystem.backup("automatic_${previousVersion}-${version.inner}")
                }.onFailure {
                    logger.error("Unable to create backup", it)
                }

                previousVersion
            }
        }
    }

    val clientVersion by Client.version
    val clientCommit by Client.commit
    val clientBranch by Client.branch

    /**
     * Defines if the client is in development mode.
     * This will enable update checking on commit time instead of semantic versioning.
     *
     * TODO: Replace this approach with full semantic versioning.
     */
    const val IN_DEVELOPMENT = false

    /**
     * Client logger to print out console messages
     */
    val logger get() = net.solidanchor.utils.client.logger

    var isInitialized = false
        private set

    /**
     * Creates an [net.minecraft.resources.Identifier] starts with [CLIENT_NAME].
     *
     * Warning: Use [clientIdentifier] to prevent silent `<clinit>` invocation
     */
    @JvmStatic
    fun identifier(path: String): Identifier = clientIdentifier(path)

    /**
     * Gets client resource.
     *
     * @param path prefix `/resources/solidanchor/`
     * @throws IllegalArgumentException if the resource is not found
     */
    @JvmStatic
    fun resource(path: String): InputStream =
        SolidAnchor::class.java.getResourceAsStream("/resources/solidanchor/$path")
            ?: throw IllegalArgumentException("Resource $path not found")

    /**
     * Gets client resource as string.
     *
     * @param path prefix `/resources/solidanchor/`
     * @throws IllegalArgumentException if the resource is not found
     */
    @JvmStatic
    fun resourceToString(path: String): String =
        resource(path).use { it.readText() }

    /**
     * Initializes the client, called when
     * we reached the last stage of the splash screen.
     *
     * The thread should be the main render thread.
     */
    @OptIn(InternalCoroutinesApi::class)
    private fun initializeClient(
        workerDispatcher: CoroutineDispatcher,
        renderThreadDispatcher: CoroutineDispatcher,
    ): CompletableFuture<Void?> = CoroutineScope(
        renderThreadDispatcher + CoroutineName("$CLIENT_NAME Initializer")
    ).future<Void?> {
        if (isInitialized) {
            return@future null
        }

        // Ensure we are on the render thread
        RenderSystem.assertOnRenderThread()
        check(!Dispatchers.Main.isMissing())

        // Initialize managers and features
        Client
        initializeManagers(renderThreadDispatcher)
        initializeFeatures()
        initializeResources(workerDispatcher)
        prepareGuiStage(renderThreadDispatcher)

        // Register shutdown hook in case [ClientShutdownEvent] is not called
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownClient))

        // Check for AMD Vega iGPU
        if (HAS_AMD_VEGA_APU) {
            logger.info("AMD Vega iGPU detected, enabling different line smooth handling.")
        }

        // Do backup before loading configs
        if (!ConfigSystem.isFirstLaunch && !Client.jsonFile.exists()) {
            runCatching {
                ConfigSystem.backup("automatic_${Client.version.inner}")
            }.onFailure {
                logger.error("Unable to create backup", it)
            }
        }

        // Load all configurations
        ConfigSystem.loadAll()
        AddonManager.notifyStarted()

        isInitialized = true
        logger.info("$CLIENT_NAME has been successfully initialized.")
        null
    }.exceptionally { throwable ->
        ErrorHandler.fatal(throwable, additionalMessage = "$CLIENT_NAME initializer")
    }

    /**
     * Initializes managers for Event Listener registration.
     */
    private suspend fun initializeManagers(
        renderThreadDispatcher: CoroutineDispatcher,
    ) = withContext(renderThreadDispatcher) {
        // Config
        ConfigSystem

        // Utility
        RenderedEntities
        ChunkScanner
        InputTracker

        // Feature managers
        ModuleManager
        CommandManager
        AccountManager

        // Utility managers
        RotationManager
        BlinkManager
        LocalPlayerFallDamageTracker
        PositionPacketSeparator
        InteractionTracker
        CombatManager
        FriendManager
        InventoryManager
        EnderChestInventoryTracker
        ConfigSystem.root(SpooferManager)
        ConfigSystem.root(GlobalManager)
        PostRotationExecutor
        ServerObserver
        ItemImageAtlas
        ItemStackListRenderer
        IndicatorRenderer

        AddonManager.discover()
    }

    /**
     * Initializes in-built and add-on features.
     */
    private fun initializeFeatures() {
        // Register commands and modules
        CommandManager.registerInbuilt()
        ModuleManager.registerInbuilt()

        AddonManager.registerCategories()
        AddonManager.initializeAddons()
    }

    /**
     * Simultaneously initializes resources
     * such as translations, cosmetics, player heads, configs and so on,
     * which do not rely on the main thread.
     */
    private suspend fun initializeResources(
        dispatcher: CoroutineDispatcher,
    ) = withContext(dispatcher) {
        logger.info("Initializing API...")

        supervisorScope {
            launch {
                // Load translations
                LanguageManager.loadDefault()
            }
            launch {
                // Download player heads
                HeadsCreativeModeTab.heads.getFinalState()
            }
            launch {
                IpInfoApi.original
            }
        }

        logger.info("API initialization done.")
    }

    /**
     * Prepares the GUI stage of the client.
     */
    private suspend fun prepareGuiStage(
        dispatcher: CoroutineDispatcher
    ) = withContext(dispatcher) {
        RenderSystem.assertOnRenderThread()

        BlurEffectRenderer

        ConfigSystem.load(GlobalManager)

        // Prepare glyph manager
        val duration = measureTime {
            FontManager.createGlyphManager()
        }
        logger.info("Completed loading fonts in ${duration.inWholeMilliseconds} ms.")
        logger.info("Fonts: [ ${FontManager.fontFaces.keys.joinToString()} ]")
    }

    /**
     * Shuts down the client. This will save all configurations and stop all running tasks.
     */
    private fun shutdownClient() {
        if (!isInitialized) {
            return
        }
        isInitialized = false
        logger.info("Shutting down client...")

        // Unregister all event listener and stop all running tasks
        ChunkScanner.stopThread()
        FontManager.closeGlyphManager()
        EventManager.unregisterAll()

        AddonManager.notifyStopping()

        // Save all configurations
        ConfigSystem.storeAll()
    }

    /**
     * Should be executed to start the client.
     */
    @Suppress("unused")
    private val startHandler = handler<ClientStartEvent> {
        runCatching {
            logger.info("Launching $CLIENT_NAME v$clientVersion by $CLIENT_AUTHOR")
            // Print client information
            logger.info("Client Version: $clientVersion ($clientCommit)")
            logger.info("Client Branch: $clientBranch")
            logger.info("Operating System: ${System.getProperty("os.name")} (${System.getProperty("os.version")})")
            logger.info("Java Version: ${System.getProperty("java.version")}")
            logger.info("Screen Resolution: ${mc.window.screenWidth}x${mc.window.screenHeight}")
            logger.info("Refresh Rate: ${mc.window.activeVideoMode?.refreshRate} Hz")

            // Initialize event manager
            EventManager

            // Register resource reloader
            val resourceManager = mc.resourceManager
            if (resourceManager is ReloadableResourceManager) {
                resourceManager.registerReloadListener(ClientResourceReloader)
            } else {
                logger.warn("Failed to register resource reloader!")

                // Run resource reloader directly as fallback
                initializeClient(
                    workerDispatcher = Dispatchers.Default,
                    renderThreadDispatcher = Dispatchers.Main,
                )
            }
        }.onFailure {
            ErrorHandler.fatal(it, additionalMessage = "Client start")
        }
    }

    /**
     * Resource reloader which is executed on client start and reload.
     * This is used to run async tasks without blocking the main thread.
     *
     * For now this is only used to check for updates and request additional information from the internet.
     *
     * @see net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener
     * @see PreparableReloadListener
     */
    private object ClientResourceReloader : PreparableReloadListener {
        override fun reload(
            store: PreparableReloadListener.SharedState,
            prepareExecutor: Executor,
            synchronizer: PreparableReloadListener.PreparationBarrier,
            applyExecutor: Executor
        ): CompletableFuture<Void> {
            return synchronizer.wait(net.minecraft.util.Unit.INSTANCE)
                .thenCompose {
                    val prepareDispatcher = prepareExecutor.asCoroutineDispatcher()
                    val applyDispatcher = applyExecutor.asCoroutineDispatcher()
                    initializeClient(
                        workerDispatcher = prepareDispatcher,
                        renderThreadDispatcher = applyDispatcher,
                    )
                }
        }

        override fun getName() = CLIENT_NAME
    }

    /**
     * Should be executed to stop the client.
     */
    @Suppress("unused")
    private val shutdownHandler = handler<ClientShutdownEvent> {
        shutdownClient()
    }

}
