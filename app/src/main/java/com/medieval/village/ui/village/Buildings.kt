package com.medieval.village.ui.village

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.medieval.village.model.BuildingStyle
import com.medieval.village.model.Place
import com.medieval.village.model.PlaceId

private val Shadow = Color(0x33000000)
private val DoorWood = Color(0xFF5A3A22)
private val DoorDark = Color(0xFF3E2716)
private val Glass = Color(0xFFF5D98B)
private val Frame = Color(0xFF4A3524)
private val Stone = Color(0xFF9A958B)
private val StoneDark = Color(0xFF77736B)

fun DrawScope.drawPlace(p: Place) {
    // 바닥 그림자
    kOval(Shadow, p.left - 6f, p.bottom - 16f, p.w + 12f, 30f, outline = false)

    when (p.style) {
        BuildingStyle.CAVE -> drawCave(p)
        BuildingStyle.ARENA -> drawArenaGround(p)
        BuildingStyle.CAMP -> drawCamp(p)
        BuildingStyle.TOWER -> drawTower(p)
        BuildingStyle.CHURCH -> drawChurch(p)
        BuildingStyle.FORGE -> drawForge(p)
        BuildingStyle.STORE -> drawHouse(p, awning = true)
        BuildingStyle.INN -> drawHouse(p, twoFloor = true)
        BuildingStyle.PUB -> drawHouse(p, twoFloor = true)
        BuildingStyle.CLINIC -> drawHouse(p, stoneWall = true)
        else -> drawHouse(p)
    }

    drawEmblem(p)
}

/** 대부분의 건물이 공유하는 기본 형태 */
private fun DrawScope.drawHouse(
    p: Place,
    awning: Boolean = false,
    twoFloor: Boolean = false,
    stoneWall: Boolean = false
) {
    val roof = Color(p.roof)
    val wall = if (stoneWall) Kenney.WallStone else Color(p.wall)
    val bodyTop = p.top + p.h * 0.40f
    val bodyH = p.bottom - bodyTop

    // Kenney식 두꺼운 외곽선 건물 본체
    kRect(wall, p.left, bodyTop, p.w, bodyH, stroke = 4f)
    if (stoneWall) {
        for (row in 0..3) {
            val yy = bodyTop + bodyH * (0.18f + row * 0.18f)
            drawLine(Color(0x33000000), Offset(p.left + 4f, yy), Offset(p.right - 4f, yy), 2f)
        }
    } else {
        kRect(Color(0x22FFFFFF), p.left + 3f, bodyTop + 4f, p.w * 0.18f, bodyH - 8f, outline = false)
    }

    val roofPath = Path().apply {
        moveTo(p.left - 14f, bodyTop + 2f)
        lineTo(p.cx, p.top)
        lineTo(p.right + 14f, bodyTop + 2f)
        close()
    }
    kPath(roof, roofPath, stroke = 4.5f)

    val chimX = p.left + p.w * 0.72f
    kRect(StoneDark, chimX, p.top + p.h * 0.04f, p.w * 0.12f, p.h * 0.28f, stroke = 3f)
    kRect(Stone, chimX - 2f, p.top + p.h * 0.02f, p.w * 0.12f + 4f, 6f, stroke = 2.5f)

    if (twoFloor) {
        kRect(Frame, p.left, bodyTop + bodyH * 0.42f, p.w, 5f, stroke = 2.5f)
        drawWarmWindow(p.cx - p.w * 0.34f, bodyTop + bodyH * 0.10f, p.w * 0.16f, bodyH * 0.22f)
        drawWarmWindow(p.cx + p.w * 0.18f, bodyTop + bodyH * 0.10f, p.w * 0.16f, bodyH * 0.22f)
    }

    val doorW = p.w * 0.22f
    val doorH = bodyH * 0.58f
    kRound(DoorWood, p.cx - doorW / 2f, p.bottom - doorH, doorW, doorH, doorW * 0.35f, stroke = 3.5f)
    drawLine(DoorDark, Offset(p.cx, p.bottom - doorH + 4f), Offset(p.cx, p.bottom - 4f), 2.5f)
    kCircle(Color(0xFFD9A441), 3.2f, Offset(p.cx + doorW * 0.30f, p.bottom - doorH * 0.5f), stroke = 2f)

    val winY = p.bottom - bodyH * 0.55f
    val winW = p.w * 0.15f
    val winH = bodyH * 0.26f
    listOf(p.cx - p.w * 0.32f, p.cx + p.w * 0.17f).forEach { wx ->
        drawWarmWindow(wx, winY, winW, winH)
    }

    if (awning) {
        val ay = bodyTop + bodyH * 0.16f
        val aw = p.w * 0.84f
        val ax = p.cx - aw / 2f
        val stripes = 6
        for (i in 0 until stripes) {
            val c = if (i % 2 == 0) Color(0xFFD8503F) else Color(0xFFF2E4C6)
            kRect(c, ax + aw / stripes * i, ay, aw / stripes, p.h * 0.11f, stroke = 2.5f)
        }
    }
}

private fun DrawScope.drawWarmWindow(x: Float, y: Float, w: Float, h: Float) {
    kRect(Color(0x33F7D46A), x - 3f, y - 3f, w + 6f, h + 6f, outline = false)
    kRect(Glass, x, y, w, h, stroke = 3f)
    drawLine(Frame, Offset(x + w / 2f, y), Offset(x + w / 2f, y + h), strokeWidth = 2.5f)
    drawLine(Frame, Offset(x, y + h / 2f), Offset(x + w, y + h / 2f), strokeWidth = 2.5f)
}

/** 교회: 첨탑 + 십자가 + 아치창 */
private fun DrawScope.drawChurch(p: Place) {
    val wall = Color(p.wall)
    val roof = Color(p.roof)
    val bodyTop = p.top + p.h * 0.46f
    val bodyH = p.bottom - bodyTop

    drawRect(wall, Offset(p.left, bodyTop), Size(p.w, bodyH))
    val roofPath = Path().apply {
        moveTo(p.left - 10f, bodyTop + 2f)
        lineTo(p.cx, p.top + p.h * 0.18f)
        lineTo(p.right + 10f, bodyTop + 2f)
        close()
    }
    drawPath(roofPath, roof)

    // 첨탑
    val tw = p.w * 0.20f
    val tx = p.cx - tw / 2f
    drawRect(wall, Offset(tx, p.top + p.h * 0.10f), Size(tw, bodyTop - p.top))
    val spire = Path().apply {
        moveTo(tx - 6f, p.top + p.h * 0.12f)
        lineTo(p.cx, p.top - p.h * 0.18f)
        lineTo(tx + tw + 6f, p.top + p.h * 0.12f)
        close()
    }
    drawPath(spire, roof)
    // 십자가
    drawRect(Color(0xFFE9CE7C), Offset(p.cx - 2.5f, p.top - p.h * 0.36f), Size(5f, p.h * 0.20f))
    drawRect(Color(0xFFE9CE7C), Offset(p.cx - 11f, p.top - p.h * 0.30f), Size(22f, 5f))

    // 아치 스테인드글라스
    listOf(p.cx - p.w * 0.30f, p.cx + p.w * 0.22f).forEach { wx ->
        val w = p.w * 0.14f
        drawArc(
            Color(0xFF6E8FC4),
            180f, 180f, true,
            topLeft = Offset(wx, bodyTop + bodyH * 0.14f),
            size = Size(w, w)
        )
        drawRect(Color(0xFF6E8FC4), Offset(wx, bodyTop + bodyH * 0.14f + w / 2f), Size(w, bodyH * 0.34f))
    }

    // 문
    val doorW = p.w * 0.20f
    val doorH = bodyH * 0.62f
    drawArc(
        DoorWood, 180f, 180f, true,
        topLeft = Offset(p.cx - doorW / 2f, p.bottom - doorH),
        size = Size(doorW, doorW)
    )
    drawRect(DoorWood, Offset(p.cx - doorW / 2f, p.bottom - doorH + doorW / 2f), Size(doorW, doorH - doorW / 2f))
}

/** 마법학교: 원통형 탑 + 원뿔 지붕 + 별 */
private fun DrawScope.drawTower(p: Place) {
    val wall = Color(p.wall)
    val roof = Color(p.roof)
    val bodyTop = p.top + p.h * 0.34f

    // 본관
    drawRect(wall, Offset(p.left, p.top + p.h * 0.52f), Size(p.w, p.bottom - (p.top + p.h * 0.52f)))
    val mainRoof = Path().apply {
        moveTo(p.left - 8f, p.top + p.h * 0.54f)
        lineTo(p.cx, p.top + p.h * 0.30f)
        lineTo(p.right + 8f, p.top + p.h * 0.54f)
        close()
    }
    drawPath(mainRoof, roof)

    // 탑
    val tw = p.w * 0.30f
    val tx = p.right - tw - p.w * 0.06f
    drawRect(Color(0xFFE0DAF0), Offset(tx, bodyTop), Size(tw, p.bottom - bodyTop))
    val cone = Path().apply {
        moveTo(tx - 8f, bodyTop + 2f)
        lineTo(tx + tw / 2f, p.top - p.h * 0.22f)
        lineTo(tx + tw + 8f, bodyTop + 2f)
        close()
    }
    drawPath(cone, roof)
    drawStar(tx + tw / 2f, p.top - p.h * 0.30f, 11f, Color(0xFFF3D96B))

    // 탑 창문
    drawCircle(Color(0xFF7C6BC0), tw * 0.16f, Offset(tx + tw / 2f, bodyTop + p.h * 0.22f))

    // 문
    val doorW = p.w * 0.18f
    val doorH = p.h * 0.28f
    drawRoundRect(
        DoorWood,
        Offset(p.cx - p.w * 0.22f, p.bottom - doorH),
        Size(doorW, doorH),
        CornerRadius(doorW * 0.4f, doorW * 0.4f)
    )
}

/** 대장간: 굴뚝 연기 + 모루 + 화덕 */
private fun DrawScope.drawForge(p: Place) {
    val wall = Color(p.wall)
    val roof = Color(p.roof)
    val bodyTop = p.top + p.h * 0.42f
    val bodyH = p.bottom - bodyTop

    drawRect(wall, Offset(p.left, bodyTop), Size(p.w, bodyH))
    val roofPath = Path().apply {
        moveTo(p.left - 12f, bodyTop + 2f)
        lineTo(p.cx, p.top + p.h * 0.06f)
        lineTo(p.right + 12f, bodyTop + 2f)
        close()
    }
    drawPath(roofPath, roof)

    // 굴뚝
    drawRect(StoneDark, Offset(p.left + p.w * 0.12f, p.top - p.h * 0.10f), Size(p.w * 0.14f, p.h * 0.40f))
    // 연기
    drawCircle(Color(0x55D8D2C6), 12f, Offset(p.left + p.w * 0.20f, p.top - p.h * 0.20f))
    drawCircle(Color(0x44D8D2C6), 16f, Offset(p.left + p.w * 0.27f, p.top - p.h * 0.34f))

    // 열린 작업장 + 화덕 불빛
    val ow = p.w * 0.42f
    val oh = bodyH * 0.62f
    drawRect(Color(0xFF2B211A), Offset(p.cx - ow * 0.15f, p.bottom - oh), Size(ow, oh))
    drawCircle(Color(0xFFE8843A), oh * 0.20f, Offset(p.cx + ow * 0.22f, p.bottom - oh * 0.40f))
    drawCircle(Color(0xFFF7D46A), oh * 0.10f, Offset(p.cx + ow * 0.22f, p.bottom - oh * 0.40f))

    // 모루
    drawRect(Color(0xFF4E4A45), Offset(p.left + p.w * 0.10f, p.bottom - bodyH * 0.26f), Size(p.w * 0.20f, bodyH * 0.10f))
    drawRect(Color(0xFF3A3733), Offset(p.left + p.w * 0.15f, p.bottom - bodyH * 0.17f), Size(p.w * 0.10f, bodyH * 0.15f))
}

/** 던전 입구: 바위 언덕과 동굴 아치 */
private fun DrawScope.drawCave(p: Place) {
    val rock = Color(p.wall)
    val rockDark = Color(p.roof)

    val hill = Path().apply {
        moveTo(p.left - 16f, p.bottom)
        cubicTo(
            p.left + p.w * 0.05f, p.top + p.h * 0.10f,
            p.right - p.w * 0.05f, p.top + p.h * 0.10f,
            p.right + 16f, p.bottom
        )
        close()
    }
    drawPath(hill, rock)
    drawPath(hill, Color(0x33000000), style = Stroke(width = 3f))

    // 바위 결
    drawCircle(rockDark.copy(alpha = 0.5f), p.w * 0.10f, Offset(p.left + p.w * 0.22f, p.cy + p.h * 0.10f))
    drawCircle(rockDark.copy(alpha = 0.4f), p.w * 0.08f, Offset(p.right - p.w * 0.20f, p.cy + p.h * 0.18f))

    // 동굴 입구
    val mw = p.w * 0.36f
    val mh = p.h * 0.58f
    drawArc(
        Color(0xFF15110E), 180f, 180f, true,
        topLeft = Offset(p.cx - mw / 2f, p.bottom - mh),
        size = Size(mw, mw)
    )
    drawRect(Color(0xFF15110E), Offset(p.cx - mw / 2f, p.bottom - mh + mw / 2f), Size(mw, mh - mw / 2f))

    // 이끼와 횃불
    drawCircle(Color(0x664E6B3A), p.w * 0.08f, Offset(p.left + p.w * 0.18f, p.cy))
    drawCircle(Color(0x554E6B3A), p.w * 0.06f, Offset(p.right - p.w * 0.16f, p.cy + 10f))
    listOf(p.cx - mw * 0.85f, p.cx + mw * 0.85f).forEach { tx ->
        drawRect(DoorWood, Offset(tx - 3f, p.bottom - p.h * 0.34f), Size(6f, p.h * 0.30f))
        drawCircle(Color(0x55E8843A), 16f, Offset(tx, p.bottom - p.h * 0.36f))
        drawCircle(Color(0xFFE8843A), 9f, Offset(tx, p.bottom - p.h * 0.36f))
        drawCircle(Color(0xFFF9DE85), 4.5f, Offset(tx, p.bottom - p.h * 0.37f))
    }

    // 팻말
    drawRect(DoorWood, Offset(p.left + p.w * 0.04f, p.bottom - p.h * 0.20f), Size(p.w * 0.16f, 5f))
}

/** 대련소: 울타리로 둘러싼 흙바닥 링 */
private fun DrawScope.drawArenaGround(p: Place) {
    val sand = Color(0xFFD9BE8B)
    drawOval(sand, Offset(p.left, p.top + p.h * 0.10f), Size(p.w, p.h * 0.90f))
    drawOval(
        Color(0xFFB99B67),
        Offset(p.left + p.w * 0.10f, p.top + p.h * 0.22f),
        Size(p.w * 0.80f, p.h * 0.66f)
    )

    // 울타리
    val fence = Color(0xFF7A5230)
    val n = 12
    for (i in 0 until n) {
        val a = Math.PI * 2 * i / n
        val fx = p.cx + (p.w / 2f - 6f) * kotlin.math.cos(a).toFloat()
        val fy = (p.cy + p.h * 0.05f) + (p.h / 2f - 10f) * kotlin.math.sin(a).toFloat()
        drawRect(fence, Offset(fx - 4f, fy - 16f), Size(8f, 22f))
    }

    // 무기 거치대
    drawLine(fence, Offset(p.cx - p.w * 0.16f, p.cy), Offset(p.cx + p.w * 0.16f, p.cy - p.h * 0.16f), strokeWidth = 5f)
    drawLine(Color(0xFFBFC5CC), Offset(p.cx - p.w * 0.16f, p.cy - p.h * 0.16f), Offset(p.cx + p.w * 0.16f, p.cy), strokeWidth = 5f)
}

/** 용병고용소: 천막 야영지 + 모닥불 */
private fun DrawScope.drawCamp(p: Place) {
    val canvasColor = Color(p.wall)
    val poleColor = Color(0xFF5A4231)

    val tent = Path().apply {
        moveTo(p.left + p.w * 0.05f, p.bottom)
        lineTo(p.cx - p.w * 0.06f, p.top + p.h * 0.14f)
        lineTo(p.right - p.w * 0.22f, p.bottom)
        close()
    }
    drawPath(tent, canvasColor)
    drawPath(tent, Color(0x44000000), style = Stroke(width = 3f))

    // 천막 입구
    val flap = Path().apply {
        moveTo(p.cx - p.w * 0.16f, p.bottom)
        lineTo(p.cx - p.w * 0.06f, p.top + p.h * 0.40f)
        lineTo(p.cx + p.w * 0.02f, p.bottom)
        close()
    }
    drawPath(flap, Color(0xFF2F3324))

    // 깃대와 깃발
    drawRect(poleColor, Offset(p.right - p.w * 0.14f, p.top + p.h * 0.02f), Size(5f, p.h * 0.90f))
    val flag = Path().apply {
        moveTo(p.right - p.w * 0.14f + 5f, p.top + p.h * 0.04f)
        lineTo(p.right + p.w * 0.02f, p.top + p.h * 0.14f)
        lineTo(p.right - p.w * 0.14f + 5f, p.top + p.h * 0.24f)
        close()
    }
    drawPath(flag, Color(0xFF9B3B2E))

    // 모닥불
    val fx = p.right - p.w * 0.28f
    val fy = p.bottom - p.h * 0.12f
    drawCircle(Color(0xFF4A3524), 13f, Offset(fx, fy))
    drawCircle(Color(0xFFE8843A), 9f, Offset(fx, fy - 5f))
    drawCircle(Color(0xFFF9DE85), 4.5f, Offset(fx, fy - 7f))
}

/** 문 위 간판 - 장소별 상징 */
private fun DrawScope.drawEmblem(p: Place) {
    if (p.style == BuildingStyle.CAVE || p.style == BuildingStyle.ARENA) return

    val cx = p.cx + p.w * 0.30f
    val cy = p.top + p.h * 0.62f
    val r = p.w * 0.10f

    drawCircle(Color(0xFF3A2A1A), r + 2.5f, Offset(cx, cy))
    drawCircle(Color(0xFFE9DCBB), r, Offset(cx, cy))

    when (p.id) {
        PlaceId.SHOP -> {
            drawRect(Color(0xFF8A5A2B), Offset(cx - r * 0.45f, cy - r * 0.35f), Size(r * 0.9f, r * 0.8f))
            drawLine(Color(0xFF5A3A22), Offset(cx - r * 0.45f, cy), Offset(cx + r * 0.45f, cy), strokeWidth = 2f)
        }
        PlaceId.WEAPON_SHOP -> {
            drawLine(Color(0xFF7E858C), Offset(cx - r * 0.5f, cy + r * 0.5f), Offset(cx + r * 0.5f, cy - r * 0.5f), strokeWidth = 3f)
            drawLine(Color(0xFF9B3B2E), Offset(cx - r * 0.5f, cy - r * 0.5f), Offset(cx + r * 0.5f, cy + r * 0.5f), strokeWidth = 3f)
        }
        PlaceId.HOSPITAL -> {
            drawRect(Color(0xFFB5453A), Offset(cx - r * 0.18f, cy - r * 0.6f), Size(r * 0.36f, r * 1.2f))
            drawRect(Color(0xFFB5453A), Offset(cx - r * 0.6f, cy - r * 0.18f), Size(r * 1.2f, r * 0.36f))
        }
        PlaceId.CHURCH -> {
            drawRect(Color(0xFFC9A227), Offset(cx - r * 0.14f, cy - r * 0.6f), Size(r * 0.28f, r * 1.2f))
            drawRect(Color(0xFFC9A227), Offset(cx - r * 0.5f, cy - r * 0.24f), Size(r * 1.0f, r * 0.28f))
        }
        PlaceId.INN -> {
            drawRect(Color(0xFF8A5A2B), Offset(cx - r * 0.5f, cy - r * 0.1f), Size(r * 1.0f, r * 0.5f))
            drawRect(Color(0xFFF2E4C6), Offset(cx - r * 0.5f, cy - r * 0.35f), Size(r * 0.45f, r * 0.3f))
        }
        PlaceId.PUB -> {
            drawRect(Color(0xFF7A5230), Offset(cx - r * 0.46f, cy - r * 0.18f), Size(r * 0.76f, r * 0.55f))
            drawArc(
                Color(0xFFE5C878),
                0f,
                180f,
                false,
                Offset(cx + r * 0.14f, cy - r * 0.12f),
                Size(r * 0.38f, r * 0.46f),
                style = Stroke(width = 2.5f)
            )
            drawCircle(Color(0xFFF2E4C6), r * 0.10f, Offset(cx - r * 0.20f, cy - r * 0.28f))
        }
        PlaceId.BLACKSMITH -> {
            drawRect(Color(0xFF5A3A22), Offset(cx - r * 0.1f, cy - r * 0.1f), Size(r * 0.2f, r * 0.7f))
            drawRect(Color(0xFF7E858C), Offset(cx - r * 0.5f, cy - r * 0.45f), Size(r * 1.0f, r * 0.35f))
        }
        PlaceId.MAGIC_SCHOOL -> drawStar(cx, cy, r * 0.85f, Color(0xFF6A4FB5))
        PlaceId.MERCENARY -> {
            drawRect(Color(0xFF5A3A22), Offset(cx - r * 0.05f, cy - r * 0.6f), Size(r * 0.12f, r * 1.2f))
            val f = Path().apply {
                moveTo(cx + r * 0.07f, cy - r * 0.55f)
                lineTo(cx + r * 0.6f, cy - r * 0.25f)
                lineTo(cx + r * 0.07f, cy + r * 0.05f)
                close()
            }
            drawPath(f, Color(0xFF9B3B2E))
        }
        PlaceId.HOME -> {
            val roofP = Path().apply {
                moveTo(cx - r * 0.55f, cy + r * 0.1f)
                lineTo(cx, cy - r * 0.55f)
                lineTo(cx + r * 0.55f, cy + r * 0.1f)
                close()
            }
            drawPath(roofP, Color(0xFF9C4A34))
            drawRect(Color(0xFF8A5A2B), Offset(cx - r * 0.35f, cy + r * 0.1f), Size(r * 0.7f, r * 0.45f))
        }
        else -> Unit
    }
}

fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.44f
        val a = -Math.PI / 2 + Math.PI * i / 5
        val x = cx + rad * kotlin.math.cos(a).toFloat()
        val y = cy + rad * kotlin.math.sin(a).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
