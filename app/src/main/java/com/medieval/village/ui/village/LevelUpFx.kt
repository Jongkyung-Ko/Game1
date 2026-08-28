package com.medieval.village.ui.village

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * 레벨업 캐릭터 특수 연출 — 상승 빛줄기·링·반짝임.
 * @param progress 0→1 (연출 진행)
 */
fun DrawScope.drawLevelUpBurst(
    cx: Float,
    footY: Float,
    progress: Float,
    animTime: Float,
) {
    if (progress !in 0f..1.05f) return
    val fade = (1f - progress).coerceIn(0f, 1f)
    val rise = progress * 70f
    val bodyY = footY - 48f - rise * 0.35f
    val gold = Color(0xFFFFE08A).copy(alpha = 0.55f + 0.4f * fade)
    val white = Color(0xFFFFF8E0).copy(alpha = 0.35f + 0.5f * fade)

    // 확장 링
    val ringR = 18f + progress * 42f
    drawCircle(
        color = gold,
        radius = ringR,
        center = Offset(cx, bodyY),
        style = Stroke(width = 3.2f * fade),
    )
    drawCircle(
        color = white,
        radius = ringR * 0.62f,
        center = Offset(cx, bodyY),
        style = Stroke(width = 2f * fade),
    )

    // 상승 빛줄기
    for (i in 0 until 8) {
        val ang = i * (Math.PI.toFloat() * 2f / 8f) + animTime * 2.8f
        val len = 22f + progress * 36f
        val x2 = cx + cos(ang) * len
        val y2 = bodyY + sin(ang) * len * 0.55f - rise * 0.2f
        drawLine(
            color = gold,
            start = Offset(cx, bodyY),
            end = Offset(x2, y2),
            strokeWidth = 2.4f * fade,
        )
    }

    // 반짝 파티클
    for (i in 0 until 10) {
        val ang = i * 0.62f + animTime * 3.4f
        val dist = 12f + (i % 4) * 9f + progress * 28f
        val px = cx + cos(ang) * dist
        val py = bodyY - 10f - progress * (20f + i * 3f) + sin(ang * 1.7f) * 4f
        drawCircle(
            color = if (i % 2 == 0) white else gold,
            radius = 2.2f + (i % 3) * 0.8f,
            center = Offset(px, py),
        )
    }

    // 발밑 글로우
    drawCircle(
        color = Color(0xFFFFD56A).copy(alpha = 0.25f * fade),
        radius = 28f + progress * 16f,
        center = Offset(cx, footY - 4f),
    )
}
