package com.medieval.village.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.medieval.village.model.Mercenary
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.rememberCustomArtOrNull

/** 용병 얼굴 초상화 — 고용 목록·Status에서 이름 옆에 표시. */
@Composable
fun MercPortrait(
    merc: Mercenary,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val art = rememberCustomArtOrNull()
    val sprite = art?.npcSpriteOrNull(merc.spriteKey)
    val shape = RoundedCornerShape(8.dp)
    val frame = modifier
        .size(size)
        .background(roleBg(merc.role), shape)
        .border(1.5.dp, Palette.Gold.copy(alpha = 0.55f), shape)

    if (sprite != null) {
        Box(modifier = frame, contentAlignment = Alignment.TopCenter) {
            Image(
                bitmap = sprite,
                contentDescription = merc.name,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Canvas(modifier = frame) {
            drawFallbackMercFace(merc)
        }
    }
}

private fun roleBg(role: String): Color = when (role) {
    "전사" -> Color(0xFF2A3848)
    "도적" -> Color(0xFF243828)
    "성기사" -> Color(0xFF383840)
    "마법사" -> Color(0xFF322848)
    else -> Color(0xFF2A2018)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFallbackMercFace(merc: Mercenary) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val roleColor = when (merc.role) {
        "전사" -> Color(0xFF496A8A)
        "도적" -> Color(0xFF4E753F)
        "성기사" -> Color(0xFF777D86)
        "마법사" -> Color(0xFF684A8F)
        else -> Color(0xFF684A8F)
    }
    val hair = when (merc.id) {
        "elara" -> Color(0xFFD4B36A)
        "bern" -> Color(0xFF2F241C)
        "shade" -> Color(0xFF3A2A40)
        "aldric" -> Color(0xFFB8B3C8)
        else -> Color(0xFF6B3F28)
    }
    // shoulders
    drawRoundRect(roleColor, Offset(w * 0.12f, h * 0.62f), Size(w * 0.76f, h * 0.45f), androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    // head
    drawCircle(Color(0xFFE1AF83), w * 0.28f, Offset(cx, h * 0.42f))
    // hair
    drawArc(hair, 180f, 180f, true, Offset(cx - w * 0.30f, h * 0.18f), Size(w * 0.60f, h * 0.42f))
    // eyes
    drawCircle(Color(0xFF2C1E12), w * 0.035f, Offset(cx - w * 0.09f, h * 0.42f))
    drawCircle(Color(0xFF2C1E12), w * 0.035f, Offset(cx + w * 0.09f, h * 0.42f))
    when (merc.role) {
        "성기사" -> {
            // helm rim
            drawArc(Color(0xFFD9A441), 200f, 140f, false, Offset(cx - w * 0.28f, h * 0.22f), Size(w * 0.56f, h * 0.4f), style = Stroke(3f))
        }
        "마법사" -> {
            val hat = Path().apply {
                moveTo(cx, h * 0.06f)
                lineTo(cx + w * 0.28f, h * 0.38f)
                lineTo(cx - w * 0.28f, h * 0.38f)
                close()
            }
            drawPath(hat, Color(0xFF4A3080))
        }
        "도적" -> {
            drawRoundRect(Color(0xFF2A3A28), Offset(cx - w * 0.22f, h * 0.36f), Size(w * 0.44f, h * 0.10f), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        }
        else -> Unit
    }
}
