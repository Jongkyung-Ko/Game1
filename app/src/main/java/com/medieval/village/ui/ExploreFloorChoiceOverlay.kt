package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.ui.theme.Palette

/** 10층 단위 클리어 후 던전 재진입 시 출발 층을 고른다. */
@Composable
fun ExploreFloorChoiceOverlay(vm: GameViewModel) {
    val choice = vm.pendingExploreChoice ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC120C08))
            .padding(18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.WoodDark, RoundedCornerShape(14.dp))
                .border(2.dp, Palette.Gold, RoundedCornerShape(14.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = choice.title,
                color = Palette.Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "중간 보스를 쓰러뜨린 ${choice.floorWord}까지 열려 있다.\n어디서부터 내려가겠는가?",
                color = Palette.Parchment,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                WoodButton(
                    text = "1부터",
                    modifier = Modifier.weight(1f),
                ) { vm.chooseExploreFloor(startFromOne = true) }
                WoodButton(
                    text = "${choice.floorWord}으로",
                    modifier = Modifier.weight(1f),
                    highlight = true,
                ) { vm.chooseExploreFloor(startFromOne = false) }
            }
            Spacer(Modifier.height(8.dp))
            WoodButton("마을로 돌아간다") { vm.cancelExploreFloorChoice() }
        }
    }
}
