package com.medieval.village.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.medieval.village.audio.GameAudioEngine
import com.medieval.village.audio.Sfx
import com.medieval.village.audio.resolveMusicMood
import com.medieval.village.game.GameViewModel
import com.medieval.village.game.MenuTab
import com.medieval.village.game.Scene
import com.medieval.village.game.isExplorePlace
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.map.WorldMapOverlay
import com.medieval.village.ui.menu.MenuOverlay
import com.medieval.village.ui.place.PlaceScreen
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.VillageScene

@Composable
fun GameRoot(modifier: Modifier = Modifier) {
    val vm: GameViewModel = viewModel()
    val context = LocalContext.current
    val audio = remember(context) { GameAudioEngine(context) }
    val haptics = remember(context) { GameHaptics(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> audio.resume()
                Lifecycle.Event.ON_PAUSE -> audio.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            audio.release()
        }
    }

    LaunchedEffect(vm.player.bgmVolume, vm.player.sfxVolume) {
        audio.setUserVolume(vm.player.bgmVolume, vm.player.sfxVolume)
    }

    LaunchedEffect(
        vm.scene,
        vm.currentPlace,
        vm.currentSettlement,
        vm.player.castleCleared,
        vm.player.iglooCleared,
        vm.player.seasideCleared,
        vm.player.winterCleared,
    ) {
        audio.playMusic(
            resolveMusicMood(
                inVillage = vm.scene == Scene.VILLAGE,
                place = vm.currentPlace,
                settlement = vm.currentSettlement,
                flags = vm.player.worldFlags,
            )
        )
    }

    LaunchedEffect(vm.walking, vm.pubWalking, vm.dungeonWalking) {
        audio.setWalking(vm.walking || vm.pubWalking || vm.dungeonWalking)
    }

    LaunchedEffect(vm.hapticSignal) {
        if (vm.hapticSignal == 0) return@LaunchedEffect
        haptics.hit(vm.hapticStrong)
    }

    LaunchedEffect(vm.sfxSignal) {
        if (vm.sfxSignal == 0) return@LaunchedEffect
        when (vm.lastSfx) {
            "hit" -> audio.playSfx(Sfx.HIT)
            "arrow_hit" -> audio.playSfx(Sfx.ARROW_HIT)
            "magic_hit" -> audio.playSfx(Sfx.MAGIC_HIT)
            "door" -> audio.playSfx(Sfx.DOOR)
            "click" -> audio.playSfx(Sfx.CLICK)
            "level_up" -> audio.playSfx(Sfx.LEVEL_UP)
            "skill_smash" -> audio.playSfx(Sfx.SKILL_SMASH)
            "skill_slash" -> audio.playSfx(Sfx.SKILL_SLASH)
            "skill_charge" -> audio.playSfx(Sfx.SKILL_CHARGE)
            "skill_bow" -> audio.playSfx(Sfx.SKILL_BOW)
            "skill_fire" -> audio.playSfx(Sfx.SKILL_FIRE)
            "skill_ice" -> audio.playSfx(Sfx.SKILL_ICE)
            "skill_lightning" -> audio.playSfx(Sfx.SKILL_LIGHTNING)
            "skill_holy" -> audio.playSfx(Sfx.SKILL_HOLY)
            "skill_crit" -> audio.playSfx(Sfx.SKILL_CRIT)
            "skill_spin" -> audio.playSfx(Sfx.SKILL_SPIN)
            "skill_bash" -> audio.playSfx(Sfx.SKILL_BASH)
            "skill_execute" -> audio.playSfx(Sfx.SKILL_EXECUTE)
            "skill_orb" -> audio.playSfx(Sfx.SKILL_ORB)
            "skill_smoke" -> audio.playSfx(Sfx.SKILL_SMOKE)
            "skill_quake" -> audio.playSfx(Sfx.SKILL_QUAKE)
            "skill_finisher" -> audio.playSfx(Sfx.SKILL_FINISHER)
        }
    }

    // 프레임 루프 - 주인공 이동 처리
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dt = ((now - last) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
                    vm.tick(dt)
                }
                last = now
            }
        }
    }

    BackHandler(
        enabled = vm.awaitingClassSelect ||
            vm.levelUpSkillOffer != null ||
            vm.pendingExploreChoice != null ||
            vm.menuTab != MenuTab.NONE ||
            vm.scene == Scene.INTERIOR,
    ) {
        when {
            vm.awaitingClassSelect -> vm.cancelClassSelect()
            vm.levelUpSkillOffer != null -> vm.dismissLevelUpSkillOffer()
            vm.pendingExploreChoice != null -> vm.cancelExploreFloorChoice()
            vm.menuTab != MenuTab.NONE -> vm.menuTab = MenuTab.NONE
            vm.interiorPanelOpen -> vm.closeInteriorPanel()
            vm.scene == Scene.INTERIOR && vm.currentPlace.isExplorePlace() -> vm.escapeDungeon()
            vm.scene == Scene.INTERIOR -> vm.leavePlace()
        }
    }

    Box(modifier = modifier.background(Palette.WoodDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 메뉴·HP/MP는 씬 밖 고정 영역 — 던전 카메라 스크롤과 겹치지 않음
            TopMenuBar(vm, Modifier.zIndex(2f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .zIndex(1f)
            ) {
                when (vm.scene) {
                    Scene.VILLAGE -> VillageScene(vm, Modifier.fillMaxSize())
                    Scene.INTERIOR -> PlaceScreen(
                        vm = vm,
                        id = vm.currentPlace ?: PlaceId.HOME,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MenuOverlay(vm)
                WorldMapOverlay(vm)
                LevelUpSkillOverlay(vm)
                ExploreFloorChoiceOverlay(vm)
            }
        }
        ClassSelectOverlay(vm, Modifier.fillMaxSize().zIndex(20f))
    }
}
