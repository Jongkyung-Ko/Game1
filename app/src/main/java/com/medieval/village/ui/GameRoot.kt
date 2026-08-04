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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.medieval.village.audio.GameAudioEngine
import com.medieval.village.audio.MusicMood
import com.medieval.village.audio.Sfx
import com.medieval.village.game.GameViewModel
import com.medieval.village.game.MenuTab
import com.medieval.village.game.Scene
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.menu.MenuOverlay
import com.medieval.village.ui.place.PlaceScreen
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.VillageScene

@Composable
fun GameRoot(modifier: Modifier = Modifier) {
    val vm: GameViewModel = viewModel()
    val context = LocalContext.current
    val audio = remember(context) { GameAudioEngine(context) }
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

    LaunchedEffect(vm.scene, vm.currentPlace) {
        val mood = when {
            vm.scene == Scene.VILLAGE -> MusicMood.VILLAGE
            vm.currentPlace in setOf(PlaceId.HOME, PlaceId.INN, PlaceId.PUB) -> MusicMood.COZY
            vm.currentPlace in setOf(PlaceId.DUNGEON, PlaceId.ARENA) -> MusicMood.TENSE
            else -> MusicMood.VILLAGE
        }
        audio.playMusic(mood)
    }

    LaunchedEffect(vm.walking, vm.pubWalking, vm.dungeonWalking) {
        audio.setWalking(vm.walking || vm.pubWalking || vm.dungeonWalking)
    }

    LaunchedEffect(vm.sfxSignal) {
        if (vm.sfxSignal == 0) return@LaunchedEffect
        when (vm.lastSfx) {
            "hit" -> audio.playSfx(Sfx.HIT)
            "door" -> audio.playSfx(Sfx.DOOR)
            "click" -> audio.playSfx(Sfx.CLICK)
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

    BackHandler(enabled = vm.menuTab != MenuTab.NONE || vm.scene == Scene.INTERIOR) {
        when {
            vm.menuTab != MenuTab.NONE -> vm.menuTab = MenuTab.NONE
            vm.scene == Scene.INTERIOR && vm.currentPlace == PlaceId.DUNGEON -> vm.escapeDungeon()
            vm.scene == Scene.INTERIOR -> vm.leavePlace()
        }
    }

    Column(modifier = modifier.background(Palette.WoodDark)) {
        TopMenuBar(vm)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (vm.scene) {
                Scene.VILLAGE -> VillageScene(vm, Modifier.fillMaxSize())
                Scene.INTERIOR -> PlaceScreen(
                    vm = vm,
                    id = vm.currentPlace ?: PlaceId.HOME,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MenuOverlay(vm)
        }
    }
}
