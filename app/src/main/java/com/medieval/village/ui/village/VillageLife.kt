package com.medieval.village.ui.village

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 마을의 동적 포인트: 새·개·고양이·닭·나비·굴뚝 연기·낙엽.
 * animTime(초)에 따라 Canvas에서 매 프레임 위치를 갱신한다.
 */
fun DrawScope.drawVillageLife(animTime: Float) {
    drawChimneySmoke(animTime)
    drawFallingLeaves(animTime)
    drawBirds(animTime)
    drawButterflies(animTime)
    drawDog(animTime)
    drawCat(animTime)
    drawChickens(animTime)
    drawHorse()
}

private fun DrawScope.drawChimneySmoke(t: Float) {
    val chimneys = listOf(
        Village.of(PlaceId.HOME).let { it.left + it.w * 0.78f to it.top + it.h * 0.08f },
        Village.of(PlaceId.INN).let { it.left + it.w * 0.18f to it.top + it.h * 0.10f },
        Village.of(PlaceId.BLACKSMITH).let { it.left + it.w * 0.20f to it.top - it.h * 0.12f },
        Village.of(PlaceId.PUB).let { it.left + it.w * 0.72f to it.top + it.h * 0.16f }
    )
    chimneys.forEachIndexed { i, (bx, by) ->
        for (p in 0..3) {
            val phase = t * (0.35f + i * 0.05f) + p * 0.55f
            val rise = (phase % 3.2f)
            val x = bx + sin(phase * 1.7f) * (6f + p * 2f)
            val y = by - rise * 22f
            val r = 7f + rise * 4f + p
            drawCircle(Color(0x33E8E0D4), r, Offset(x, y))
        }
    }
}

private fun DrawScope.drawFallingLeaves(t: Float) {
    Village.trees.take(5).forEachIndexed { i, (tx, ty, tr) ->
        for (k in 0..2) {
            val phase = t * 0.55f + i * 1.3f + k * 0.9f
            val fall = (phase % 4.5f)
            val x = tx + sin(phase * 2.1f) * (tr * 0.7f) + k * 8f
            val y = ty - tr * 1.6f + fall * (tr * 0.55f)
            val leaf = Path().apply {
                moveTo(x, y)
                quadraticBezierTo(x + 5f, y + 2f, x + 2f, y + 7f)
                quadraticBezierTo(x - 4f, y + 3f, x, y)
                close()
            }
            drawPath(leaf, if ((i + k) % 2 == 0) Color(0xFF7A9B45) else Color(0xFFC4A24A))
        }
    }
}

private fun DrawScope.drawBirds(t: Float) {
    // 교회 위 선회하는 새 무리
    val church = Village.of(PlaceId.CHURCH)
    for (i in 0..4) {
        val a = t * (0.7f + i * 0.08f) + i * 1.1f
        val r = 70f + i * 18f
        val x = church.cx + cos(a) * r
        val y = church.top - 40f + sin(a * 1.4f) * 18f - i * 8f
        drawBird(x, y, flap = t * 14f + i)
    }
    // 나무 사이를 가로지르는 참새
    for (i in 0..2) {
        val progress = ((t * 0.12f + i * 0.33f) % 1f)
        val x = 80f + progress * (Village.W - 160f)
        val y = 420f + sin(progress * PI.toFloat() * 4f + i) * 40f + i * 55f
        drawBird(x, y, flap = t * 16f + i * 2f, scale = 0.85f)
    }
}

private fun DrawScope.drawBird(x: Float, y: Float, flap: Float, scale: Float = 1f) {
    val wing = sin(flap) * 7f * scale
    val body = Color(0xFF3A322C)
    drawOval(body, Offset(x - 5f * scale, y - 2.5f * scale), Size(10f * scale, 5f * scale))
    drawCircle(body, 2.2f * scale, Offset(x + 5f * scale, y - 1f * scale))
    val left = Path().apply {
        moveTo(x, y)
        lineTo(x - 8f * scale, y - 6f * scale - wing)
        lineTo(x - 2f * scale, y)
        close()
    }
    val right = Path().apply {
        moveTo(x, y)
        lineTo(x + 8f * scale, y - 6f * scale - wing)
        lineTo(x + 2f * scale, y)
        close()
    }
    drawPath(left, Color(0xFF5A5048))
    drawPath(right, Color(0xFF5A5048))
}

private fun DrawScope.drawButterflies(t: Float) {
    val spots = listOf(
        352f to 640f,
        640f to 860f,
        300f to 1180f,
        720f to 480f
    )
    spots.forEachIndexed { i, (sx, sy) ->
        val a = t * (1.4f + i * 0.2f) + i
        val x = sx + cos(a) * 28f
        val y = sy + sin(a * 1.7f) * 18f
        val flap = sin(t * 12f + i) * 4f
        val color = if (i % 2 == 0) Color(0xFFE8B45A) else Color(0xFFD98BB0)
        drawOval(color, Offset(x - 5f - flap, y - 3f), Size(5f + flap * 0.3f, 4f))
        drawOval(color, Offset(x + flap * 0.2f, y - 3f), Size(5f + flap * 0.3f, 4f))
        drawCircle(Color(0xFF3A322C), 1.2f, Offset(x, y))
    }
}

private fun DrawScope.drawDog(t: Float) {
    // 광장 우물 주변을 빙글 도는 개
    val a = t * 0.45f
    val x = Village.WELL_X + cos(a) * 95f
    val y = Village.WELL_Y + sin(a) * 55f + 10f
    val facingRight = cos(a + PI.toFloat() / 2f) > 0f
    val bob = sin(t * 6f) * 1.5f
    drawOval(Color(0x33000000), Offset(x - 16f, y - 4f), Size(32f, 10f))
    drawOval(Color(0xFF8B6A42), Offset(x - 14f, y - 18f + bob), Size(26f, 14f))
    drawCircle(Color(0xFF8B6A42), 7f, Offset(if (facingRight) x + 12f else x - 12f, y - 20f + bob))
    drawCircle(Color(0xFF3A322C), 1.6f, Offset(if (facingRight) x + 15f else x - 15f, y - 21f + bob))
    // 다리
    val step = sin(t * 7f) * 3f
    drawRect(Color(0xFF6B4B2E), Offset(x - 10f, y - 8f + step), Size(4f, 10f))
    drawRect(Color(0xFF6B4B2E), Offset(x - 2f, y - 8f - step), Size(4f, 10f))
    drawRect(Color(0xFF6B4B2E), Offset(x + 6f, y - 8f + step), Size(4f, 10f))
    // 꼬리
    drawLine(
        Color(0xFF6B4B2E),
        Offset(if (facingRight) x - 14f else x + 14f, y - 14f + bob),
        Offset(if (facingRight) x - 22f else x + 22f, y - 22f + bob + sin(t * 8f) * 3f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawCat(t: Float) {
    // 무기점 지붕 위 고양이
    val shop = Village.of(PlaceId.WEAPON_SHOP)
    val pace = sin(t * 0.35f)
    val x = shop.cx + pace * (shop.w * 0.28f)
    val y = shop.top + shop.h * 0.34f
    val facingRight = cos(t * 0.35f) > 0f
    drawOval(Color(0xFF4A4A52), Offset(x - 10f, y - 10f), Size(18f, 9f))
    drawCircle(Color(0xFF4A4A52), 5.5f, Offset(if (facingRight) x + 8f else x - 8f, y - 12f))
    // 귀
    val earX = if (facingRight) x + 6f else x - 10f
    val ear = Path().apply {
        moveTo(earX, y - 14f)
        lineTo(earX + 4f, y - 20f)
        lineTo(earX + 7f, y - 13f)
        close()
    }
    drawPath(ear, Color(0xFF4A4A52))
    drawLine(
        Color(0xFF3A3A42),
        Offset(if (facingRight) x - 10f else x + 10f, y - 8f),
        Offset(if (facingRight) x - 18f else x + 18f, y - 14f + sin(t * 3f) * 2f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawChickens(t: Float) {
    val home = Village.of(PlaceId.HOME)
    val baseX = home.left - 30f
    val baseY = home.doorY + 8f
    for (i in 0..2) {
        val peck = sin(t * 5f + i * 2f)
        val x = baseX + i * 28f + sin(t * 0.8f + i) * 12f
        val y = baseY + cos(t * 0.6f + i) * 6f
        drawOval(Color(0x33000000), Offset(x - 8f, y - 3f), Size(16f, 7f))
        drawOval(Color(0xFFF2E4C6), Offset(x - 8f, y - 12f + peck), Size(14f, 10f))
        drawCircle(Color(0xFFF2E4C6), 4.5f, Offset(x + 6f, y - 14f + peck * 1.4f))
        drawCircle(Color(0xFFE07A3A), 2f, Offset(x + 10f, y - 13f + peck * 1.4f))
        drawCircle(Color(0xFFC0392B), 2.5f, Offset(x + 5f, y - 18f + peck))
    }
}

private fun DrawScope.drawHorse() {
    val camp = Village.of(PlaceId.MERCENARY)
    val x = camp.left - 36f
    val y = camp.doorY + 6f
    drawOval(Color(0x33000000), Offset(x - 22f, y - 6f), Size(48f, 14f))
    drawOval(Color(0xFF6B4B2E), Offset(x - 18f, y - 34f), Size(36f, 22f))
    drawRect(Color(0xFF6B4B2E), Offset(x - 16f, y - 18f), Size(6f, 18f))
    drawRect(Color(0xFF6B4B2E), Offset(x - 4f, y - 18f), Size(6f, 18f))
    drawRect(Color(0xFF6B4B2E), Offset(x + 6f, y - 18f), Size(6f, 18f))
    drawRect(Color(0xFF6B4B2E), Offset(x + 14f, y - 18f), Size(6f, 18f))
    drawOval(Color(0xFF5A3A22), Offset(x + 12f, y - 48f), Size(18f, 16f))
    drawLine(Color(0xFF4A3524), Offset(x + 28f, y - 42f), Offset(x + 36f, y - 30f), 3f)
    drawLine(Color(0xFF8A5A2B), Offset(x + 20f, y - 28f), Offset(camp.left + 8f, camp.doorY - 20f), 2f)
    drawCircle(Color(0xFF3A322C), 2f, Offset(x + 26f, y - 44f))
}
