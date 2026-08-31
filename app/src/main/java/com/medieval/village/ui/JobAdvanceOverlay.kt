package com.medieval.village.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.rememberCustomArtOrNull

/** 레벨 5/10/15/20 전직·각성 연출 */
@Composable
fun JobAdvanceOverlay(vm: GameViewModel) {
    val offer = vm.pendingJobAdvance ?: return
    JobAdvanceContent(vm, offer)
}

@Composable
private fun JobAdvanceContent(
    vm: GameViewModel,
    offer: com.medieval.village.model.JobAdvanceOffer,
) {
    val art = rememberCustomArtOrNull()
    val fromRank = if (offer.awakening) offer.spriteRank else (offer.spriteRank - 1).coerceAtLeast(0)
    val fromSprite = art?.heroSpriteOrNull("portrait", offer.job, fromRank)
        ?: art?.heroSpriteOrNull("front", offer.job, fromRank)
    val toSprite = art?.heroSpriteOrNull("portrait", offer.job, offer.spriteRank)
        ?: art?.heroSpriteOrNull("front", offer.job, offer.spriteRank)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD120C07))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
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
                if (offer.awakening) "각성" else "전직",
                color = Palette.Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Lv.${offer.newLevel}",
                color = Palette.ParchmentDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RankPortrait(fromSprite, offer.fromTitle)
                Text("→", color = Palette.Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                RankPortrait(toSprite, offer.toTitle)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (offer.awakening) {
                    "『${offer.toTitle}』의 힘이 깨어났다."
                } else {
                    "『${offer.toTitle}』이(가) 되었다."
                },
                color = Palette.Parchment,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                "능력 상승이 이전의 ${offer.growthMult}배가 된다.",
                color = Palette.ParchmentDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
            )
            WoodButton("확인", highlight = true) { vm.dismissJobAdvance() }
        }
    }
}

@Composable
private fun RankPortrait(
    sprite: androidx.compose.ui.graphics.ImageBitmap?,
    caption: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(110.dp, 150.dp)
                .background(Color(0xFF1B120A), RoundedCornerShape(8.dp))
                .border(1.dp, Palette.Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (sprite != null) {
                Image(
                    bitmap = sprite,
                    contentDescription = caption,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
            }
        }
        Text(
            caption,
            color = Palette.Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
