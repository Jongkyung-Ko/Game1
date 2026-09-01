package com.medieval.village.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.ui.theme.ClassicType
import com.medieval.village.ui.theme.Palette

/** 직업 선택 전에 보이는 영문 로고 타이틀. */
@Composable
fun TitleOverlay(vm: GameViewModel, modifier: Modifier = Modifier) {
    if (!vm.awaitingTitle) return
    val context = LocalContext.current
    val logo = remember(context) {
        runCatching {
            context.assets.open("ui/title_logo.jpg").use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }.getOrNull()
    }
    val saveTick = vm.saveRevision
    val slots = remember(saveTick) { vm.saveSlotInfos() }
    val hasSave = slots.any { !it.empty }
    var pickingSlot by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF50C0805))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (logo != null) {
                Image(
                    bitmap = logo,
                    contentDescription = "MEDIEVAL VILLAGE",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(bottom = 8.dp),
                )
            } else {
                Text(
                    "MEDIEVAL VILLAGE",
                    style = ClassicType.Title.copy(fontSize = 28.sp, letterSpacing = 3.sp),
                    color = Palette.Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 28.dp),
                )
            }
            Text(
                "A tale of cursed hamlets and returning hearths",
                style = ClassicType.Scroll.copy(fontSize = 11.sp),
                color = Palette.ParchmentDim,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            if (!pickingSlot) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WoodButton("시작하기", highlight = true) { vm.startFromTitle() }
                    WoodButton("이어하기", enabled = hasSave) {
                        val filled = slots.filter { !it.empty }
                        if (filled.size == 1) {
                            vm.continueFromTitle(filled.first().slot)
                        } else {
                            pickingSlot = true
                        }
                    }
                }
                if (!hasSave) {
                    Text(
                        "저장된 모험이 없습니다",
                        color = Palette.ParchmentDim,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            } else {
                Text(
                    "불러올 슬롯을 고르시오",
                    color = Palette.Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                slots.forEach { slot ->
                    val summary = if (slot.empty) {
                        "비어 있음"
                    } else {
                        "${slot.playerName} · Lv.${slot.level} · ${slot.settlementName}"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFF24180F), RoundedCornerShape(8.dp))
                            .border(1.dp, Palette.Gold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !slot.empty) { vm.continueFromTitle(slot.slot) }
                            .padding(10.dp),
                    ) {
                        Column {
                            Text("슬롯 ${slot.slot}", color = Palette.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(summary, color = Palette.Parchment, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                WoodButton("뒤로") { pickingSlot = false }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
