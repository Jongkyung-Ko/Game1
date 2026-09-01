package com.medieval.village.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.HeroJob
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.rememberCustomArtOrNull

/** 첫 시작·새 게임에서 주인공 직업을 고른다. */
@Composable
fun ClassSelectOverlay(vm: GameViewModel, modifier: Modifier = Modifier) {
    if (!vm.awaitingClassSelect) return
    ClassSelectContent(vm, modifier)
}

@Composable
private fun ClassSelectContent(vm: GameViewModel, modifier: Modifier = Modifier) {
    val art = rememberCustomArtOrNull()
    var selected by remember { mutableStateOf(HeroJob.WARRIOR) }
    val jobs = HeroJob.entries

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEE120C07))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.WoodDark, RoundedCornerShape(14.dp))
                .border(2.dp, Palette.Gold, RoundedCornerShape(14.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "직업을 고르시오",
                color = Palette.Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "한 번 정하면 이 여정의 길이 달라진다.",
                color = Palette.ParchmentDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            jobs.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { job ->
                        val selectedNow = selected == job
                        val sprite = art?.heroSpriteOrNull("portrait", job)
                            ?: art?.heroSpriteOrNull("front", job)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selectedNow) Color(0xFF3A2A18) else Color(0xFF24180F),
                                    RoundedCornerShape(10.dp),
                                )
                                .border(
                                    width = if (selectedNow) 2.5.dp else 1.dp,
                                    color = if (selectedNow) Palette.Gold else Palette.Gold.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { selected = job }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(118.dp)
                                    .background(Color(0xFF1B120A), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                if (sprite != null) {
                                    Image(
                                        bitmap = sprite,
                                        contentDescription = job.label,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                    )
                                } else {
                                    Text(
                                        job.label.take(1),
                                        color = Palette.Gold,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            Text(
                                job.label,
                                color = Palette.Gold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                job.blurb,
                                color = Palette.Parchment,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            Text(
                                job.starterLine,
                                color = Palette.ParchmentDim,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                job.pathLine,
                                color = Palette.Gold.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                selected.title,
                color = Palette.Parchment,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WoodButton(if (vm.hasStartedRun) "취소" else "뒤로") {
                    vm.cancelClassSelect()
                }
                WoodButton("이 직업으로 출발", highlight = true) {
                    vm.confirmHeroJob(selected)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
