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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.Prologue
import com.medieval.village.ui.theme.Palette

/** 새 게임 시작 시 다섯 장의 삽화와 하단 설명을 넘긴다. */
@Composable
fun PrologueOverlay(vm: GameViewModel, modifier: Modifier = Modifier) {
    if (!vm.awaitingPrologue) return
    val slides = Prologue.slides
    val page = vm.prologuePage.coerceIn(0, slides.lastIndex)
    val slide = slides[page]
    val context = LocalContext.current
    val art = remember(context, slide.asset) {
        runCatching {
            context.assets.open(slide.asset).use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF50C0805))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { vm.advancePrologue() },
                contentAlignment = Alignment.Center,
            ) {
                if (art != null) {
                    Image(
                        bitmap = art,
                        contentDescription = slide.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        slide.title,
                        color = Palette.Gold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            CaptionPanel(
                page = page,
                pageCount = slides.size,
                title = slide.title,
                body = slide.body,
                onSkip = { vm.skipPrologue() },
                onNext = { vm.advancePrologue() },
            )
        }
    }
}

@Composable
private fun CaptionPanel(
    page: Int,
    pageCount: Int,
    title: String,
    body: String,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF21A120C))
            .border(1.dp, Palette.Gold.copy(alpha = 0.45f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == page) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == page) Palette.Gold
                            else Palette.Gold.copy(alpha = 0.28f),
                        ),
                )
            }
        }
        Text(
            "${page + 1} / $pageCount",
            color = Palette.ParchmentDim,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        )
        Text(
            title,
            color = Palette.Gold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        Text(
            body,
            color = Palette.Parchment,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            WoodButton("건너뛰기", modifier = Modifier.weight(1f), onClick = onSkip)
            WoodButton(
                text = if (page >= pageCount - 1) "시작" else "다음",
                modifier = Modifier.weight(1f),
                highlight = true,
                onClick = onNext,
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}
