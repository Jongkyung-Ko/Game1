package com.medieval.village.ui.place

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.medieval.village.game.Facing
import com.medieval.village.model.Mercenary
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawMercenary
import com.medieval.village.ui.village.drawStar

/** 장소별 실내(혹은 입구) 배경. w, h 는 캔버스 픽셀 크기. */
fun DrawScope.drawInterior(id: PlaceId, w: Float, h: Float, companions: List<Mercenary> = emptyList()) {
    when (id) {
        PlaceId.DUNGEON -> dungeonMouth(w, h)
        PlaceId.ARENA -> arenaYard(w, h)
        PlaceId.MERCENARY -> campInterior(w, h)
        else -> roomBase(id, w, h)
    }

    companions.forEachIndexed { index, mercenary ->
        val k = h * 0.39f / 88f
        withTransform({
            translate(w * (0.30f + index * 0.11f), h * (0.93f - index * 0.02f))
            scale(k, k, Offset.Zero)
        }) {
            drawMercenary(mercenary, 0f, 0f, Facing.RIGHT, false, index.toFloat())
        }
    }

    // 주인공
    val k = h * 0.46f / 104f
    withTransform({
        translate(w * 0.20f, h * 0.93f)
        scale(k, k, Offset.Zero)
    }) {
        drawHero(0f, 0f, Facing.RIGHT, false, 0f)
    }
}

private fun DrawScope.roomBase(id: PlaceId, w: Float, h: Float) {
    val wallColor = when (id) {
        PlaceId.CHURCH -> Color(0xFFD7D2C2)
        PlaceId.HOSPITAL -> Color(0xFFE4E7E2)
        PlaceId.MAGIC_SCHOOL -> Color(0xFF3C3560)
        PlaceId.BLACKSMITH -> Color(0xFF4A423A)
        else -> Color(0xFFC8AC7E)
    }
    val floorColor = when (id) {
        PlaceId.CHURCH, PlaceId.HOSPITAL -> Color(0xFF9E9A90)
        PlaceId.MAGIC_SCHOOL -> Color(0xFF2A2440)
        else -> Color(0xFF7A5533)
    }
    val floorY = h * 0.62f

    drawRect(wallColor, Offset(0f, 0f), Size(w, floorY))
    drawRect(floorColor, Offset(0f, floorY), Size(w, h - floorY))
    // 바닥 판자
    var x = 0f
    while (x < w) {
        drawLine(Color(0x22000000), Offset(x, floorY), Offset(x - h * 0.10f, h), strokeWidth = 2f)
        x += w * 0.11f
    }
    drawLine(Color(0x44000000), Offset(0f, floorY), Offset(w, floorY), strokeWidth = 3f)

    when (id) {
        PlaceId.HOME -> homeProps(w, h, floorY)
        PlaceId.SHOP -> shopProps(w, h, floorY)
        PlaceId.WEAPON_SHOP -> weaponProps(w, h, floorY)
        PlaceId.HOSPITAL -> hospitalProps(w, h, floorY)
        PlaceId.CHURCH -> churchProps(w, h, floorY)
        PlaceId.INN -> innProps(w, h, floorY)
        PlaceId.PUB -> innProps(w, h, floorY)
        PlaceId.BLACKSMITH -> forgeProps(w, h, floorY)
        PlaceId.MAGIC_SCHOOL -> magicProps(w, h, floorY)
        else -> Unit
    }
}

private fun DrawScope.window(x: Float, y: Float, w: Float, h: Float) {
    drawRect(Color(0xFF6E8FC4), Offset(x, y), Size(w, h))
    drawRect(Color(0xFF4A3524), Offset(x, y), Size(w, h), style = Stroke(width = 4f))
    drawLine(Color(0xFF4A3524), Offset(x + w / 2f, y), Offset(x + w / 2f, y + h), strokeWidth = 3f)
    drawLine(Color(0xFF4A3524), Offset(x, y + h / 2f), Offset(x + w, y + h / 2f), strokeWidth = 3f)
}

private fun DrawScope.homeProps(w: Float, h: Float, floorY: Float) {
    window(w * 0.10f, h * 0.14f, w * 0.16f, h * 0.26f)
    // 침대
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.60f, floorY - h * 0.06f), Size(w * 0.33f, h * 0.30f))
    drawRect(Color(0xFFE6DCC4), Offset(w * 0.62f, floorY - h * 0.02f), Size(w * 0.29f, h * 0.18f))
    drawRect(Color(0xFF8C2F28), Offset(w * 0.62f, floorY + h * 0.06f), Size(w * 0.29f, h * 0.10f))
    // 벽난로
    drawRect(Color(0xFF7D766C), Offset(w * 0.36f, h * 0.22f), Size(w * 0.16f, h * 0.40f))
    drawRect(Color(0xFF2B211A), Offset(w * 0.39f, h * 0.38f), Size(w * 0.10f, h * 0.24f))
    drawCircle(Color(0xFFE8843A), h * 0.06f, Offset(w * 0.44f, h * 0.55f))
    drawCircle(Color(0xFFF7D46A), h * 0.03f, Offset(w * 0.44f, h * 0.56f))
    // 나무 상자
    drawRect(Color(0xFF8A5A2B), Offset(w * 0.30f, floorY + h * 0.16f), Size(w * 0.12f, h * 0.16f))
}

private fun DrawScope.shopProps(w: Float, h: Float, floorY: Float) {
    // 선반
    for (i in 0..2) {
        val sy = h * (0.14f + i * 0.15f)
        drawRect(Color(0xFF6B4B2E), Offset(w * 0.36f, sy), Size(w * 0.56f, h * 0.03f))
        for (j in 0..4) {
            val cx = w * (0.40f + j * 0.11f)
            val c = listOf(
                Color(0xFFC0392B), Color(0xFFE0A430), Color(0xFF6A8F3C),
                Color(0xFF4A7FC1), Color(0xFFB07CC6)
            )[(i + j) % 5]
            drawCircle(c, h * 0.035f, Offset(cx, sy - h * 0.04f))
        }
    }
    // 계산대
    drawRect(Color(0xFF8A5A2B), Offset(w * 0.42f, floorY + h * 0.04f), Size(w * 0.42f, h * 0.24f))
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.42f, floorY + h * 0.04f), Size(w * 0.42f, h * 0.04f))
}

private fun DrawScope.weaponProps(w: Float, h: Float, floorY: Float) {
    // 무기 거치대
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.38f, h * 0.14f), Size(w * 0.54f, h * 0.04f))
    val steel = Color(0xFFBFC5CC)
    for (j in 0..3) {
        val x = w * (0.44f + j * 0.13f)
        drawRect(steel, Offset(x - 3f, h * 0.18f), Size(6f, h * 0.26f))
        drawRect(Color(0xFF7A5230), Offset(x - 9f, h * 0.44f), Size(18f, h * 0.05f))
    }
    // 방패
    val shield = Path().apply {
        moveTo(w * 0.86f, h * 0.56f)
        lineTo(w * 0.96f, h * 0.60f)
        lineTo(w * 0.91f, h * 0.80f)
        lineTo(w * 0.81f, h * 0.60f)
        close()
    }
    drawPath(shield, Color(0xFF9B3B2E))
    drawPath(shield, Color(0xFFD9A441), style = Stroke(width = 3f))
    // 작업대
    drawRect(Color(0xFF8A5A2B), Offset(w * 0.40f, floorY + h * 0.08f), Size(w * 0.34f, h * 0.20f))
}

private fun DrawScope.hospitalProps(w: Float, h: Float, floorY: Float) {
    // 붉은 십자
    drawRect(Color(0xFFB5453A), Offset(w * 0.46f, h * 0.10f), Size(w * 0.05f, h * 0.24f))
    drawRect(Color(0xFFB5453A), Offset(w * 0.40f, h * 0.17f), Size(w * 0.17f, h * 0.05f))
    // 침대
    drawRect(Color(0xFFB9B3A6), Offset(w * 0.62f, floorY - h * 0.04f), Size(w * 0.32f, h * 0.28f))
    drawRect(Color(0xFFFFFFFF), Offset(w * 0.64f, floorY), Size(w * 0.28f, h * 0.16f))
    drawRect(Color(0xFF7FA7C9), Offset(w * 0.64f, floorY + h * 0.08f), Size(w * 0.28f, h * 0.09f))
    // 약장
    drawRect(Color(0xFF8A7A62), Offset(w * 0.34f, h * 0.36f), Size(w * 0.16f, h * 0.26f))
    drawCircle(Color(0xFF6FBF73), h * 0.025f, Offset(w * 0.38f, h * 0.44f))
    drawCircle(Color(0xFFD86A6A), h * 0.025f, Offset(w * 0.45f, h * 0.44f))
}

private fun DrawScope.churchProps(w: Float, h: Float, floorY: Float) {
    // 스테인드글라스
    listOf(0.34f, 0.60f, 0.86f).forEach { fx ->
        val x = w * fx - w * 0.05f
        drawArc(
            Color(0xFF6E8FC4), 180f, 180f, true,
            topLeft = Offset(x, h * 0.08f), size = Size(w * 0.10f, w * 0.10f)
        )
        drawRect(Color(0xFF8B6FC0), Offset(x, h * 0.08f + w * 0.05f), Size(w * 0.10f, h * 0.24f))
        drawRect(Color(0x44000000), Offset(x, h * 0.08f), Size(w * 0.10f, h * 0.29f), style = Stroke(3f))
    }
    // 제단
    drawRect(Color(0xFFE6DCC4), Offset(w * 0.56f, floorY + h * 0.02f), Size(w * 0.30f, h * 0.22f))
    drawRect(Color(0xFFC9A227), Offset(w * 0.69f, floorY - h * 0.14f), Size(w * 0.02f, h * 0.16f))
    drawRect(Color(0xFFC9A227), Offset(w * 0.65f, floorY - h * 0.10f), Size(w * 0.10f, h * 0.02f))
    // 촛대
    listOf(0.58f, 0.84f).forEach { fx ->
        drawRect(Color(0xFFF2E4C6), Offset(w * fx, floorY - h * 0.06f), Size(w * 0.012f, h * 0.08f))
        drawCircle(Color(0xFFF7D46A), h * 0.016f, Offset(w * fx + w * 0.006f, floorY - h * 0.07f))
    }
}

private fun DrawScope.innProps(w: Float, h: Float, floorY: Float) {
    // 바 카운터
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.42f, floorY - h * 0.02f), Size(w * 0.50f, h * 0.30f))
    drawRect(Color(0xFF8A5A2B), Offset(w * 0.42f, floorY - h * 0.02f), Size(w * 0.50f, h * 0.05f))
    // 술통과 잔
    drawRect(Color(0xFF7A5230), Offset(w * 0.36f, h * 0.30f), Size(w * 0.12f, h * 0.20f))
    listOf(0.52f, 0.62f, 0.72f).forEach { fx ->
        drawRect(Color(0xFFD9CDB4), Offset(w * fx, floorY - h * 0.10f), Size(w * 0.05f, h * 0.08f))
    }
    // 계단
    for (i in 0..3) {
        drawRect(
            Color(0xFF5A3F27),
            Offset(w * 0.80f, h * (0.20f + i * 0.09f)),
            Size(w * 0.18f, h * 0.05f)
        )
    }
}

private fun DrawScope.forgeProps(w: Float, h: Float, floorY: Float) {
    // 화덕
    drawRect(Color(0xFF6E655B), Offset(w * 0.56f, h * 0.14f), Size(w * 0.34f, h * 0.44f))
    drawRect(Color(0xFF17110C), Offset(w * 0.61f, h * 0.24f), Size(w * 0.24f, h * 0.30f))
    drawCircle(Color(0xFFE8843A), h * 0.09f, Offset(w * 0.73f, h * 0.44f))
    drawCircle(Color(0xFFF7D46A), h * 0.045f, Offset(w * 0.73f, h * 0.45f))
    // 모루
    drawRect(Color(0xFF4E4A45), Offset(w * 0.40f, floorY + h * 0.06f), Size(w * 0.18f, h * 0.07f))
    drawRect(Color(0xFF3A3733), Offset(w * 0.45f, floorY + h * 0.13f), Size(w * 0.08f, h * 0.13f))
    // 망치
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.30f, floorY + h * 0.02f), Size(w * 0.015f, h * 0.18f))
    drawRect(Color(0xFF7E858C), Offset(w * 0.26f, floorY + h * 0.00f), Size(w * 0.09f, h * 0.05f))
}

private fun DrawScope.magicProps(w: Float, h: Float, floorY: Float) {
    // 책장
    drawRect(Color(0xFF4A3524), Offset(w * 0.34f, h * 0.08f), Size(w * 0.26f, h * 0.52f))
    for (i in 0..2) {
        val sy = h * (0.14f + i * 0.16f)
        for (j in 0..4) {
            val c = listOf(
                Color(0xFFC0392B), Color(0xFFE0A430), Color(0xFF4A7FC1),
                Color(0xFF6A8F3C), Color(0xFFB07CC6)
            )[(i * 2 + j) % 5]
            drawRect(c, Offset(w * (0.36f + j * 0.045f), sy), Size(w * 0.03f, h * 0.11f))
        }
    }
    // 마법진
    drawCircle(Color(0x664A7FC1), h * 0.14f, Offset(w * 0.74f, floorY + h * 0.16f))
    drawCircle(Color(0xAA7FB6E8), h * 0.14f, Offset(w * 0.74f, floorY + h * 0.16f), style = Stroke(3f))
    drawStar(w * 0.74f, floorY + h * 0.16f, h * 0.10f, Color(0x99CFE4FF))
    // 수정구
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.64f, h * 0.44f), Size(w * 0.07f, h * 0.16f))
    drawCircle(Color(0xFF9B7FD4), h * 0.06f, Offset(w * 0.675f, h * 0.40f))
    drawCircle(Color(0xCCE6DBFF), h * 0.02f, Offset(w * 0.66f, h * 0.38f))
}

private fun DrawScope.dungeonMouth(w: Float, h: Float) {
    drawRect(Color(0xFF2A2621), Offset(0f, 0f), Size(w, h))
    // 바위 아치
    val arch = Path().apply {
        moveTo(w * 0.12f, h)
        cubicTo(w * 0.18f, h * 0.10f, w * 0.82f, h * 0.10f, w * 0.88f, h)
        close()
    }
    drawPath(arch, Color(0xFF56504A))
    // 어둠
    val hole = Path().apply {
        moveTo(w * 0.26f, h)
        cubicTo(w * 0.30f, h * 0.26f, w * 0.70f, h * 0.26f, w * 0.74f, h)
        close()
    }
    drawPath(hole, Color(0xFF0D0B09))
    // 계단
    for (i in 0..3) {
        drawRect(
            Color(0xFF3E3932),
            Offset(w * (0.34f + i * 0.02f), h * (0.72f + i * 0.07f)),
            Size(w * (0.32f - i * 0.04f), h * 0.04f)
        )
    }
    // 횃불
    listOf(0.20f, 0.80f).forEach { fx ->
        drawRect(Color(0xFF5A3A22), Offset(w * fx - 4f, h * 0.42f), Size(8f, h * 0.26f))
        drawCircle(Color(0xFFE8843A), h * 0.055f, Offset(w * fx, h * 0.40f))
        drawCircle(Color(0xFFF9DE85), h * 0.026f, Offset(w * fx, h * 0.41f))
        drawCircle(Color(0x33E8843A), h * 0.13f, Offset(w * fx, h * 0.40f))
    }
}

private fun DrawScope.arenaYard(w: Float, h: Float) {
    drawRect(Color(0xFF8FB8D8), Offset(0f, 0f), Size(w, h * 0.42f))
    drawCircle(Color(0xFFF7E9A8), h * 0.10f, Offset(w * 0.84f, h * 0.12f))
    drawRect(Color(0xFFD9BE8B), Offset(0f, h * 0.42f), Size(w, h * 0.58f))
    drawOval(Color(0xFFC7A876), Offset(w * 0.06f, h * 0.50f), Size(w * 0.88f, h * 0.44f))

    // 울타리
    for (i in 0..11) {
        val x = w * (0.03f + i * 0.086f)
        drawRect(Color(0xFF7A5230), Offset(x, h * 0.34f), Size(w * 0.018f, h * 0.14f))
    }
    drawRect(Color(0xFF7A5230), Offset(0f, h * 0.38f), Size(w, h * 0.02f))

    // 허수아비 표적
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.70f, h * 0.50f), Size(w * 0.02f, h * 0.36f))
    drawRect(Color(0xFF6B4B2E), Offset(w * 0.63f, h * 0.58f), Size(w * 0.16f, h * 0.02f))
    drawCircle(Color(0xFFD9C08A), h * 0.06f, Offset(w * 0.71f, h * 0.52f))
    // 무기 거치대
    drawRect(Color(0xFF7A5230), Offset(w * 0.44f, h * 0.60f), Size(w * 0.14f, h * 0.02f))
    drawRect(Color(0xFFBFC5CC), Offset(w * 0.47f, h * 0.46f), Size(5f, h * 0.16f))
    drawRect(Color(0xFFBFC5CC), Offset(w * 0.54f, h * 0.46f), Size(5f, h * 0.16f))
}

private fun DrawScope.campInterior(w: Float, h: Float) {
    drawRect(Color(0xFF8FB8D8), Offset(0f, 0f), Size(w, h * 0.46f))
    drawRect(Color(0xFF6F9A54), Offset(0f, h * 0.46f), Size(w, h * 0.54f))

    // 천막들
    listOf(0.42f to 0.9f, 0.68f to 0.72f, 0.88f to 0.6f).forEach { (fx, sc) ->
        val bx = w * fx
        val bw = w * 0.20f * sc
        val bh = h * 0.42f * sc
        val tent = Path().apply {
            moveTo(bx - bw, h * 0.86f)
            lineTo(bx, h * 0.86f - bh)
            lineTo(bx + bw, h * 0.86f)
            close()
        }
        drawPath(tent, Color(0xFF8B9668))
        drawPath(tent, Color(0x44000000), style = Stroke(3f))
        val flap = Path().apply {
            moveTo(bx - bw * 0.30f, h * 0.86f)
            lineTo(bx, h * 0.86f - bh * 0.55f)
            lineTo(bx + bw * 0.30f, h * 0.86f)
            close()
        }
        drawPath(flap, Color(0xFF2F3324))
    }

    // 모닥불
    drawCircle(Color(0xFF4A3524), h * 0.07f, Offset(w * 0.52f, h * 0.92f))
    drawCircle(Color(0xFFE8843A), h * 0.05f, Offset(w * 0.52f, h * 0.90f))
    drawCircle(Color(0xFFF9DE85), h * 0.025f, Offset(w * 0.52f, h * 0.90f))

    // 깃발
    drawRect(Color(0xFF5A4231), Offset(w * 0.34f, h * 0.30f), Size(5f, h * 0.52f))
    val flag = Path().apply {
        moveTo(w * 0.34f + 5f, h * 0.32f)
        lineTo(w * 0.46f, h * 0.38f)
        lineTo(w * 0.34f + 5f, h * 0.44f)
        close()
    }
    drawPath(flag, Color(0xFF9B3B2E))
}
