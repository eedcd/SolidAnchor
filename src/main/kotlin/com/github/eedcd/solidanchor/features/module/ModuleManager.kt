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
package com.github.eedcd.solidanchor.features.module

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import com.github.eedcd.solidanchor.config.ConfigSystem
import com.github.eedcd.solidanchor.config.autoconfig.AutoConfig
import com.github.eedcd.solidanchor.config.types.VALUE_NAME_ORDER
import com.github.eedcd.solidanchor.event.EventListener
import com.github.eedcd.solidanchor.event.events.DisconnectEvent
import com.github.eedcd.solidanchor.event.events.KeyboardKeyEvent
import com.github.eedcd.solidanchor.event.events.MouseButtonEvent
import com.github.eedcd.solidanchor.event.events.WorldChangeEvent
import com.github.eedcd.solidanchor.event.handler
import com.github.eedcd.solidanchor.event.sequenceHandler
import com.github.eedcd.solidanchor.event.tickUntil
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleAimbot
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleAutoClicker
import com.github.eedcd.solidanchor.features.module.modules.world.automobheal.AutoMobHeal
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleAutoLeave
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleAutoRod
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleAutoShoot
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleAutoWeapon
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleFakeLag
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleHitbox
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleKeepSprint
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleMaceKill
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleNoMissCooldown
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleSuperKnockback
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleSwordBlock
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleTickBase
import com.github.eedcd.solidanchor.features.module.modules.combat.ModuleTimerRange
import com.github.eedcd.solidanchor.features.module.modules.combat.aimbot.ModuleAutoBow
import com.github.eedcd.solidanchor.features.module.modules.combat.autoarmor.ModuleAutoArmor
import com.github.eedcd.solidanchor.features.module.modules.combat.backtrack.ModuleBacktrack
import com.github.eedcd.solidanchor.features.module.modules.combat.criticals.ModuleCriticals
import com.github.eedcd.solidanchor.features.module.modules.combat.crystalaura.ModuleCrystalAura
import com.github.eedcd.solidanchor.features.module.modules.combat.elytratarget.ModuleElytraTarget
import com.github.eedcd.solidanchor.features.module.modules.combat.killaura.ModuleKillAura
import com.github.eedcd.solidanchor.features.module.modules.combat.spearkill.ModuleSpearKill
import com.github.eedcd.solidanchor.features.module.modules.combat.tpaura.ModuleTpAura
import com.github.eedcd.solidanchor.features.module.modules.combat.velocity.ModuleVelocity
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleAbortBreaking
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleAntiHunger
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleAntiReducedDebugInfo
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleClickTp
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleClip
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleDamage
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleExtendedFirework
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleGhostHand
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleKick
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleMoreCarry
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleMultiActions
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleNameCollector
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleNoPitchLimit
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModulePingSpoof
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModulePlugins
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModulePortalMenu
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleResetVL
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleSleepWalker
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleTimeShift
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleVehicleOneHit
import com.github.eedcd.solidanchor.features.module.modules.exploit.ModuleYggdrasilSignatureFix
import com.github.eedcd.solidanchor.features.module.modules.exploit.disabler.ModuleDisabler
import com.github.eedcd.solidanchor.features.module.modules.exploit.dupe.ModuleDupe
import com.github.eedcd.solidanchor.features.module.modules.exploit.phase.ModulePhase
import com.github.eedcd.solidanchor.features.module.modules.exploit.servercrasher.ModuleServerCrasher
import com.github.eedcd.solidanchor.features.module.modules.`fun`.ModuleDankBobbing
import com.github.eedcd.solidanchor.features.module.modules.`fun`.ModuleDerp
import com.github.eedcd.solidanchor.features.module.modules.`fun`.ModuleHandDerp
import com.github.eedcd.solidanchor.features.module.modules.`fun`.ModuleSkinDerp
import com.github.eedcd.solidanchor.features.module.modules.`fun`.ModuleTwerk
import com.github.eedcd.solidanchor.features.module.modules.`fun`.ModuleVomit
import com.github.eedcd.solidanchor.features.module.modules.`fun`.notebot.ModuleNotebot
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleAntiCheatDetect
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleAutoAccount
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleAutoChatGame
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleAutoPearl
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleBetterTab
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleBookBot
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleEasyPearl
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleElytraSwap
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleFlagCheck
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleGUICloser
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleInventoryTracker
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleItemScroller
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleMacros
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleMiddleClickAction
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleNotifier
import com.github.eedcd.solidanchor.features.module.modules.misc.ModulePacketLogger
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleSpammer
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleTargetLock
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleTeams
import com.github.eedcd.solidanchor.features.module.modules.misc.ModuleTextFieldProtect
import com.github.eedcd.solidanchor.features.module.modules.misc.antibot.ModuleAntiBot
import com.github.eedcd.solidanchor.features.module.modules.misc.betterchat.ModuleBetterChat
import com.github.eedcd.solidanchor.features.module.modules.misc.debugrecorder.ModuleDebugRecorder
import com.github.eedcd.solidanchor.features.module.modules.misc.nameprotect.ModuleNameProtect
import com.github.eedcd.solidanchor.features.module.modules.misc.reporthelper.ModuleReportHelper
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleAirJump
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleAnchor
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleAntiBounce
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleAntiLevitation
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleAvoidHazards
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleBlockBounce
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleBlockWalk
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleElytraRecast
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleEntityControl
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleFreeze
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleNoClip
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleNoJumpDelay
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleNoPose
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleNoPush
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleParkour
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleSafeWalk
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleSneak
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleSprint
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleStrafe
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleTargetStrafe
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleTeleport
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleTridentBoost
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleVehicleBoost
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleVehicleControl
import com.github.eedcd.solidanchor.features.module.modules.movement.ModuleSnapTap
import com.github.eedcd.solidanchor.features.module.modules.movement.autododge.ModuleAutoDodge
import com.github.eedcd.solidanchor.features.module.modules.movement.elytrafly.ModuleElytraFly
import com.github.eedcd.solidanchor.features.module.modules.movement.fly.ModuleFly
import com.github.eedcd.solidanchor.features.module.modules.movement.highjump.ModuleHighJump
import com.github.eedcd.solidanchor.features.module.modules.movement.inventorymove.ModuleInventoryMove
import com.github.eedcd.solidanchor.features.module.modules.movement.liquidwalk.ModuleLiquidWalk
import com.github.eedcd.solidanchor.features.module.modules.movement.longjump.ModuleLongJump
import com.github.eedcd.solidanchor.features.module.modules.movement.noslow.ModuleNoSlow
import com.github.eedcd.solidanchor.features.module.modules.movement.noweb.ModuleNoWeb
import com.github.eedcd.solidanchor.features.module.modules.movement.speed.ModuleSpeed
import com.github.eedcd.solidanchor.features.module.modules.movement.spider.ModuleSpider
import com.github.eedcd.solidanchor.features.module.modules.movement.step.ModuleReverseStep
import com.github.eedcd.solidanchor.features.module.modules.movement.step.ModuleStep
import com.github.eedcd.solidanchor.features.module.modules.movement.terrainspeed.ModuleTerrainSpeed
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAntiAFK
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAntiExploit
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAutoBreak
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAutoFish
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAutoRespawn
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAutoWalk
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAutoWindCharge
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleBlink
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleChestCleaner
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleEagle
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleFastExp
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleFastUse
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleNoBlockInteract
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleNoEntityInteract
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleNoRotateSet
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleNoSlotSet
import com.github.eedcd.solidanchor.features.module.modules.player.ModulePotionSpoof
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleReach
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleReplenish
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleSmartEat
import com.github.eedcd.solidanchor.features.module.modules.player.antivoid.ModuleAntiVoid
import com.github.eedcd.solidanchor.features.module.modules.player.autobuff.ModuleAutoBuff
import com.github.eedcd.solidanchor.features.module.modules.player.ModuleAutoCrafter
import com.github.eedcd.solidanchor.features.module.modules.player.autoqueue.ModuleAutoQueue
import com.github.eedcd.solidanchor.features.module.modules.player.autodeposit.ModuleAutoDeposit
import com.github.eedcd.solidanchor.features.module.modules.player.autoshop.ModuleAutoShop
import com.github.eedcd.solidanchor.features.module.modules.player.cheststealer.ModuleChestStealer
import com.github.eedcd.solidanchor.features.module.modules.player.invcleaner.ModuleInventoryCleaner
import com.github.eedcd.solidanchor.features.module.modules.player.nofall.ModuleNoFall
import com.github.eedcd.solidanchor.features.module.modules.player.offhand.ModuleOffhand
import com.github.eedcd.solidanchor.features.module.modules.render.animations.ModuleAnimations
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleAntiBlind
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleAspect
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleAutoF5
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleBedPlates
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleBetterInventory
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleBlockESP
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleBlockOutline
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleBreadcrumbs
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleChams
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleClickGui
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleCombineMobs
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleCrystalView
import com.github.eedcd.solidanchor.features.module.modules.render.customambience.ModuleCustomAmbience
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleDamageParticles
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleDebug
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleFreeCam
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleFreeLook
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleFullBright
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleHoleESP
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleHud
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleItemChams
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleItemESP
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleItemTags
import com.github.eedcd.solidanchor.features.module.modules.render.jumpeffect.ModuleJumpEffect
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleLogoffSpot
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleMobOwners
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleNewChunks
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleNoBob
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleNoFov
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleNoHurtCam
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleNoSwing
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleParticles
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleProphuntESP
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleProtectionZones
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleQuickPerspectiveSwap
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleRadar
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleRotations
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleSilentHotbar
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleSkinChanger
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleSmoothCamera
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleStorageESP
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleTNTTimer
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleTracers
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleTrueSight
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleVoidESP
import com.github.eedcd.solidanchor.features.module.modules.render.wings.ModuleWings
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleXRay
import com.github.eedcd.solidanchor.features.module.modules.render.ModuleZoom
import com.github.eedcd.solidanchor.features.module.modules.render.cameraclip.ModuleCameraClip
import com.github.eedcd.solidanchor.features.module.modules.render.crosshair.ModuleCrosshair
import com.github.eedcd.solidanchor.features.module.modules.render.esp.ModuleESP
import com.github.eedcd.solidanchor.features.module.modules.render.hats.ModuleHats
import com.github.eedcd.solidanchor.features.module.modules.render.hitfx.ModuleHitFX
import com.github.eedcd.solidanchor.features.module.modules.render.murdermystery.ModuleMurderMystery
import com.github.eedcd.solidanchor.features.module.modules.render.nametags.ModuleNametags
import com.github.eedcd.solidanchor.features.module.modules.render.potionfx.ModulePotionFX
import com.github.eedcd.solidanchor.features.module.modules.render.totemeffect.ModuleTotemEffect
import com.github.eedcd.solidanchor.features.module.modules.render.trajectories.ModuleTrajectories
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleAirPlace
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleAutoDisable
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleAutoTool
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleBedDefender
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleBlockIn
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleBlockTrap
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleExtinguish
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleFastBreak
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleFastPlace
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleHoleFiller
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleLiquidFiller
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleLiquidPlace
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleNoInterpolation
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleNoSlowBreak
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleProjectilePuncher
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleStrongholdFinder
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleSurround
import com.github.eedcd.solidanchor.features.module.modules.world.ModuleTimer
import com.github.eedcd.solidanchor.features.module.modules.world.autobuild.ModuleAutoBuild
import com.github.eedcd.solidanchor.features.module.modules.world.autofarm.ModuleAutoFarm
import com.github.eedcd.solidanchor.features.module.modules.world.fucker.ModuleFucker
import com.github.eedcd.solidanchor.features.module.modules.world.nuker.ModuleNuker
import com.github.eedcd.solidanchor.features.module.modules.world.packetmine.ModulePacketMine
import com.github.eedcd.solidanchor.features.module.modules.world.scaffold.ModuleScaffold
import com.github.eedcd.solidanchor.features.module.modules.world.traps.ModuleAutoTrap
import com.github.eedcd.solidanchor.features.addon.AddonApi
import com.github.eedcd.solidanchor.utils.client.clientStartDurationMs
import com.github.eedcd.solidanchor.utils.client.inGame
import com.github.eedcd.solidanchor.utils.client.logger
import com.github.eedcd.solidanchor.utils.client.mc
import com.github.eedcd.solidanchor.utils.input.InputBind

private val modules = ObjectRBTreeSet<ClientModule>(VALUE_NAME_ORDER)

/**
 * A fairly simple module manager
 */
object ModuleManager : EventListener, Collection<ClientModule> by modules {

    val modulesConfig = ConfigSystem.root("modules", modules)

    private const val SMART_MOUSE_HOLD_THRESHOLD_MS = 200L

    private enum class SmartBindKeyboardState {
        PENDING_ENABLED, PENDING_DISABLED, HOLDING,
    }
    private class SmartBindMouseState(val pendingEnabled: Boolean, val pressTimestamp: Long)

    private val smartKeyboardStates = Reference2ObjectArrayMap<ClientModule, SmartBindKeyboardState>()
    private val smartMouseStates = Reference2ObjectArrayMap<ClientModule, SmartBindMouseState>()

    private fun modulesWithOwnBinds() = modules.filterNot(ClientModule::externalBind)

    /**
     * Handles keystrokes for module binds.
     * This also runs in GUIs, so that if a GUI is opened while a key is pressed,
     * any modules that need to be disabled on key release will be properly disabled.
     */
    @Suppress("unused")
    private val keyboardKeyHandler = handler<KeyboardKeyEvent> { event ->
        if (event.isPressed) {
            if (mc.gui.screen() == null) {
                // Usually nobody actually wants a module to activate when they press the Minecraft debug key combo.
                if (mc.options.keyDebugModifier.isDown) return@handler
                for (m in modulesWithOwnBinds()) {
                    if (!m.bind.matchesKeyPress(event)) {
                        continue
                    }

                    when (m.bind.action) {
                        InputBind.BindAction.TOGGLE -> m.enabled = !m.enabled
                        InputBind.BindAction.HOLD -> m.enabled = true
                        InputBind.BindAction.SMART -> {
                            smartKeyboardStates[m] = if (m.enabled) {
                                SmartBindKeyboardState.PENDING_ENABLED
                            } else {
                                SmartBindKeyboardState.PENDING_DISABLED
                            }
                            m.enabled = true
                        }
                    }
                }
            }
        } else if (event.isRepeat) {
            for (m in modulesWithOwnBinds()) {
                if (m.bind.action != InputBind.BindAction.SMART ||
                    !m.bind.matchesKey(event.scanCode) ||
                    m !in smartKeyboardStates
                ) {
                    continue
                }

                smartKeyboardStates[m] = SmartBindKeyboardState.HOLDING
            }
        } else if (event.isReleased) {
            for (m in modulesWithOwnBinds()) {
                if (!m.bind.matchesKeyRelease(event)) {
                    continue
                }

                when (m.bind.action) {
                    InputBind.BindAction.HOLD -> m.enabled = false

                    InputBind.BindAction.SMART -> {
                        val stateBeforePress = smartKeyboardStates.remove(m) ?: continue
                        m.enabled = stateBeforePress == SmartBindKeyboardState.PENDING_DISABLED
                    }

                    InputBind.BindAction.TOGGLE -> {}
                }
            }
        }
    }

    @Suppress("unused")
    private val mouseButtonHandler = handler<MouseButtonEvent> { event ->
        if (event.isPressed) {
            if (mc.gui.screen() == null) {
                for (m in modulesWithOwnBinds()) {
                    if (!m.bind.matchesMousePress(event)) {
                        continue
                    }

                    when (m.bind.action) {
                        InputBind.BindAction.TOGGLE -> m.enabled = !m.enabled
                        InputBind.BindAction.HOLD -> m.enabled = true
                        InputBind.BindAction.SMART -> {
                            smartMouseStates[m] = SmartBindMouseState(m.enabled, clientStartDurationMs)
                            m.enabled = true
                        }
                    }
                }
            }
        } else if (event.isReleased) {
            for (m in modulesWithOwnBinds()) {
                if (!m.bind.matchesMouseRelease(event)) {
                    continue
                }

                when (m.bind.action) {
                    InputBind.BindAction.HOLD -> m.enabled = false

                    InputBind.BindAction.SMART -> {
                        val state = smartMouseStates.remove(m) ?: continue

                        // Mouse button events do not emit SDL repeat, so SMART falls back to:
                        // - hold if the press was long enough
                        // - toggle otherwise
                        val shouldFallbackToHold =
                            clientStartDurationMs - state.pressTimestamp >= SMART_MOUSE_HOLD_THRESHOLD_MS

                        if (shouldFallbackToHold) {
                            m.enabled = false
                        } else {
                            m.enabled = !state.pendingEnabled
                        }
                    }

                    InputBind.BindAction.TOGGLE -> {}
                }
            }
        }
    }

    /**
     * Handles world change and enables modules that are not enabled yet
     */
    @Suppress("unused")
    private val handleWorldChange = sequenceHandler<WorldChangeEvent> { event ->
        // Delayed start handling
        if (event.world != null) {
            tickUntil { inGame }
            AutoConfig.withLoading {
                for (module in modules) {
                    if (!module.enabled || module.calledSinceStartup) continue

                    try {
                        module.calledSinceStartup = true
                        // inGame is false here, so use onToggle0
                        module.onToggled(true)
                    } catch (e: Exception) {
                        logger.error("Failed to enable module ${module.name}", e)
                    }
                }
            }
        }

        // Store modules configuration after world change, happens on disconnect as well
        ConfigSystem.store(modulesConfig)
    }

    /**
     * Handles disconnect and if [ClientModule.disableOnQuit] is true disables module
     */
    @Suppress("unused")
    private val handleDisconnect = handler<DisconnectEvent> {
        for (module in modules) {
            if (module.disableOnQuit) {
                try {
                    module.enabled = false
                } catch (e: Exception) {
                    logger.error("Failed to disable module ${module.name}", e)
                }
            }
        }
    }

    /**
     * Register inbuilt client modules
     */
    @Suppress("LongMethod")
    fun registerInbuilt() {
        val builtin = arrayOf(
            // Combat
            ModuleAimbot,
            ModuleAutoArmor,
            ModuleAutoBow,
            ModuleAutoClicker,
            ModuleAutoLeave,
            ModuleAutoBuff,
            ModuleAutoRod,
            ModuleAutoWeapon,
            ModuleFakeLag,
            ModuleCriticals,
            ModuleHitbox,
            ModuleKillAura,
            ModuleTpAura,
            ModuleSuperKnockback,
            ModuleTimerRange,
            ModuleTickBase,
            ModuleVelocity,
            ModuleBacktrack,
            ModuleSwordBlock,
            ModuleAutoShoot,
            ModuleKeepSprint,
            ModuleMaceKill,
            ModuleSpearKill,
            ModuleNoMissCooldown,

            // Exploit
            ModuleAbortBreaking,
            ModuleAntiReducedDebugInfo,
            ModuleAntiHunger,
            ModuleClip,
            ModuleExtendedFirework,
            ModuleResetVL,
            ModuleDamage,
            ModuleDisabler,
            ModuleGhostHand,
            ModuleKick,
            ModuleMoreCarry,
            ModuleMultiActions,
            ModuleNewChunks,
            ModuleNameCollector,
            ModuleNoPitchLimit,
            ModulePingSpoof,
            ModulePlugins,
            ModulePortalMenu,
            ModuleSleepWalker,
            ModuleVehicleOneHit,
            ModuleServerCrasher,
            ModuleDupe,
            ModuleClickTp,
            ModuleTimeShift,
            ModuleTeleport,
            ModulePhase,
            ModuleYggdrasilSignatureFix,

            // Fun
            ModuleDankBobbing,
            ModuleDerp,
            ModuleNotebot,
            ModuleSkinDerp,
            ModuleHandDerp,
            ModuleTwerk,
            ModuleVomit,

            // Misc
            ModuleGUICloser,
            ModuleBookBot,
            ModuleAntiBot,
            ModuleBetterTab,
            ModuleItemScroller,
            ModuleBetterChat,
            ModuleElytraTarget,
            ModuleMacros,
            ModuleMiddleClickAction,
            ModuleInventoryTracker,
            ModuleNameProtect,
            ModuleTextFieldProtect,
            ModuleNotifier,
            ModuleSpammer,
            ModuleAutoAccount,
            ModuleTeams,
            ModuleElytraSwap,
            ModuleAutoChatGame,
            ModuleReportHelper,
            ModuleTargetLock,
            ModuleAutoPearl,
            ModuleFlagCheck,
            ModulePacketLogger,
            ModuleDebugRecorder,
            ModuleAntiCheatDetect,
            ModuleEasyPearl,

            // Movement
            ModuleAirJump,
            ModuleAntiBounce,
            ModuleAntiLevitation,
            ModuleAutoDodge,
            ModuleAvoidHazards,
            ModuleBlockBounce,
            ModuleBlockWalk,
            ModuleElytraRecast,
            ModuleElytraFly,
            ModuleFly,
            ModuleFreeze,
            ModuleHighJump,
            ModuleInventoryMove,
            ModuleLiquidWalk,
            ModuleLongJump,
            ModuleNoClip,
            ModuleNoJumpDelay,
            ModuleNoPose,
            ModuleNoPush,
            ModuleNoSlow,
            ModuleNoWeb,
            ModuleParkour,
            ModuleEntityControl,
            ModuleSafeWalk,
            ModuleSneak,
            ModuleSpeed,
            ModuleSprint,
            ModuleStep,
            ModuleReverseStep,
            ModuleStrafe,
            ModuleTerrainSpeed,
            ModuleTridentBoost,
            ModuleVehicleBoost,
            ModuleVehicleControl,
            ModuleSpider,
            ModuleTargetStrafe,
            ModuleAnchor,
            ModuleSnapTap,

            // Player
            ModuleAntiVoid,
            ModuleAntiAFK,
            ModuleAntiExploit,
            ModuleAutoBreak,
            ModuleAutoCrafter,
            ModuleAutoFish,
            ModuleAutoRespawn,
            ModuleAutoWindCharge,
            ModuleOffhand,
            ModuleAutoShop,
            ModuleAutoWalk,
            ModuleBlink,
            ModuleChestCleaner,
            ModuleChestStealer,
            ModuleAutoDeposit,
            ModuleEagle,
            ModuleFastExp,
            ModuleFastUse,
            ModuleInventoryCleaner,
            ModuleNoBlockInteract,
            ModuleNoEntityInteract,
            ModuleNoFall,
            ModuleNoRotateSet,
            ModuleNoSlotSet,
            ModuleReach,
            ModuleAutoQueue,
            ModuleSmartEat,
            ModuleReplenish,
            ModulePotionSpoof,

            // Render
            ModuleAnimations,
            ModuleAntiBlind,
            ModuleBetterInventory,
            ModuleBlockESP,
            ModuleBlockOutline,
            ModuleBreadcrumbs,
            ModuleCameraClip,
            ModuleClickGui,
            ModuleDamageParticles,
            ModuleParticles,
            ModuleESP,
            ModuleLogoffSpot,
            ModuleFreeCam,
            ModuleSmoothCamera,
            ModuleFreeLook,
            ModuleFullBright,
            ModuleHoleESP,
            ModuleHud,
            ModuleHats,
            ModuleItemESP,
            ModuleItemTags,
            ModuleJumpEffect,
            ModuleMobOwners,
            ModuleMurderMystery,
            ModuleHitFX,
            ModuleNametags,
            ModuleCombineMobs,
            ModuleAspect,
            ModuleAutoF5,
            ModuleChams,
            ModuleBedPlates,
            ModuleNoBob,
            ModuleNoFov,
            ModuleNoHurtCam,
            ModuleNoSwing,
            ModuleCustomAmbience,
            ModuleProphuntESP,
            ModuleQuickPerspectiveSwap,
            ModuleRadar,
            ModuleRotations,
            ModuleSilentHotbar,
            ModuleStorageESP,
            ModuleTNTTimer,
            ModuleTracers,
            ModuleTrajectories,
            ModuleTrueSight,
            ModuleVoidESP,
            ModuleXRay,
            ModuleDebug,
            ModuleZoom,
            ModuleItemChams,
            ModuleCrystalView,
            ModuleSkinChanger,
            ModuleProtectionZones,
            ModuleCrosshair,
            ModuleWings,
            ModulePotionFX,
            ModuleTotemEffect,

            // World
            AutoMobHeal,
            ModuleAirPlace,
            ModuleAutoBuild,
            ModuleAutoDisable,
            ModuleAutoFarm,
            ModuleAutoTool,
            ModuleCrystalAura,
            ModuleFastBreak,
            ModuleFastPlace,
            ModuleFucker,
            ModuleAutoTrap,
            ModuleBlockTrap,
            ModuleNoSlowBreak,
            ModuleLiquidFiller,
            ModuleLiquidPlace,
            ModuleProjectilePuncher,
            ModuleScaffold,
            ModuleTimer,
            ModuleNuker,
            ModuleExtinguish,
            ModuleBedDefender,
            ModuleBlockIn,
            ModuleSurround,
            ModulePacketMine,
            ModuleHoleFiller,
            ModuleStrongholdFinder,
            ModuleNoInterpolation,
        )

        builtin.forEach { module ->
            addModule(module)
            module.walkKeyPath()
            module.verifyFallbackDescription()
        }
    }

    fun addModule(module: ClientModule) {
        if (!modules.add(module)) {
            error("Module '${module.name}' is already registered.")
        }

        runCatching {
            module.walkInit()
            module.onRegistration()
        }.onFailure {
            modules.remove(module)
        }.getOrThrow()
    }

    fun removeModule(module: ClientModule) {
        // The set compares by name, so check identity.
        check(any { it === module }) { "Module '${module.name}' is not registered." }
        modules.remove(module)

        if (module.enabled) {
            module.enabled = false
        }
        module.unregister()
    }

    fun clear() {
        modules.clear()
    }

    @AddonApi
    operator fun get(moduleName: String) = modules.find { it.name.equals(moduleName, true) }

}
