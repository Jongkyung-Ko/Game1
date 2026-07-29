package com.medieval.village.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
