package com.medieval.village.ui.village

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 마을의 동적 포인트: 새·개·고양이·닭·나비·말·굴뚝 연기·낙엽.
 * Kenney A스타일 스프라이트로 매 프레임 움직인다.
 */
fun DrawScope.drawVillageLife(animTime: Float) {
    drawChimneySmoke(animTime)
    drawFallingLeaves(animTime)
    drawBirds(animTime)
    drawButterflies(animTime)
    drawDog(animTime)
    drawCat(animTime)
    drawChickens(animTime)
    drawHorse(animTime)
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
                quadraticTo(x + 5f, y + 2f, x + 2f, y + 7f)
                quadraticTo(x - 4f, y + 3f, x, y)
                close()
            }
            drawPath(leaf, if ((i + k) % 2 == 0) Color(0xFF7A9B45) else Color(0xFFC4A24A))
        }
    }
}

private fun DrawScope.drawBirds(t: Float) {
    val church = Village.of(PlaceId.CHURCH)
    for (i in 0..4) {
        val a = t * (0.7f + i * 0.08f) + i * 1.1f
        val r = 70f + i * 18f
        val x = church.cx + cos(a) * r
        val y = church.top - 40f + sin(a * 1.4f) * 18f - i * 8f
        val facingRight = cos(a + 0.2f) > 0f
        drawKenneyBird(x, y, t + i, facingRight)
    }
    for (i in 0..2) {
        val progress = ((t * 0.12f + i * 0.33f) % 1f)
        val x = 80f + progress * (Village.W - 160f)
        val y = 420f + sin(progress * PI.toFloat() * 4f + i) * 40f + i * 55f
        drawKenneyBird(x, y, t * 1.2f + i * 2f, facingRight = true)
    }
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
        drawKenneyButterfly(sx + cos(a) * 28f, sy + sin(a * 1.7f) * 18f, t + i)
    }
}

private fun DrawScope.drawDog(t: Float) {
    val a = t * 0.45f
    val x = Village.WELL_X + cos(a) * 95f
    val y = Village.WELL_Y + sin(a) * 55f + 10f
    val facingRight = cos(a + PI.toFloat() / 2f) > 0f
    drawKenneyDog(x, y, t, facingRight)
}

private fun DrawScope.drawCat(t: Float) {
    val shop = Village.of(PlaceId.WEAPON_SHOP)
    val pace = sin(t * 0.35f)
    val x = shop.cx + pace * (shop.w * 0.28f)
    val y = shop.top + shop.h * 0.34f
    val facingRight = cos(t * 0.35f) > 0f
    drawKenneyCat(x, y, t, facingRight)
}

private fun DrawScope.drawChickens(t: Float) {
    val home = Village.of(PlaceId.HOME)
    val baseX = home.left - 30f
    val baseY = home.doorY + 8f
    for (i in 0..2) {
        val x = baseX + i * 28f + sin(t * 0.8f + i) * 12f
        val y = baseY + cos(t * 0.6f + i) * 6f
        drawKenneyChicken(x, y, t + i * 0.7f, facingRight = sin(t * 0.8f + i) > 0f)
    }
}

private fun DrawScope.drawHorse(t: Float) {
    val camp = Village.of(PlaceId.MERCENARY)
    drawKenneyHorse(camp.left - 36f, camp.doorY + 6f, t)
}
