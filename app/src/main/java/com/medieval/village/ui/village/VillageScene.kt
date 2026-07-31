package com.medieval.village.ui.village

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import com.medieval.village.ui.theme.Palette
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val grassBlobs = Random(7).let { r ->
    List(28) { Triple(r.nextFloat() * Village.W, r.nextFloat() * Village.H, 26f + r.nextFloat() * 58f) }
}
private val flowers = Random(21).let { r ->
    List(70) { Triple(r.nextFloat() * Village.W, r.nextFloat() * Village.H, r.nextInt(3)) }
}
private val pebbles = Random(33).let { r ->
    List(90) { Triple(r.nextFloat(), r.nextFloat(), 1.6f + r.nextFloat() * 2.4f) }
}

@Composable
fun VillageScene(vm: GameViewModel, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.background(Palette.GrassDark)) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val s = min(wPx / Village.W, hPx / Village.H)
        val ox = (wPx - Village.W * s) / 2f
        val oy = (hPx - Village.H * s) / 2f
        // composition 단계에서 읽어 Canvas가 매 프레임 다시 그리게 한다
        val animTime = vm.animTime
        val heroX = vm.heroX
        val heroY = vm.heroY
        val facing = vm.facing
        val walking = vm.walking
        val party = vm.activeParty

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(s, ox, oy) {
                    detectTapGestures { tap ->
                        val wx = (tap.x - ox) / s
                        val wy = (tap.y - oy) / s
                        val hit = Village.places.firstOrNull { p ->
                            wx >= p.left - 12f && wx <= p.right + 12f &&
                                wy >= p.top - 20f && wy <= p.doorY + 16f
                        }
                        if (hit != null) vm.goToPlace(hit) else vm.walkTo(wx, wy)
                    }
                }
        ) {
            // 따뜻한 석양빛 하늘 기운 (화면 여백)
            drawRect(Palette.SkyWarm.copy(alpha = 0.35f), size = size)
            drawRect(Palette.Grass, size = size)
            withTransform({
                translate(ox, oy)
                scale(s, s, Offset.Zero)
            }) {
                drawGround()
                drawRoads()
                drawScenery()
                Village.places.sortedBy { it.bottom }.forEach { drawPlace(it) }
                drawVillageLife(animTime)
                party.forEachIndexed { index, mercenary ->
                    val side = if (index == 0) -1f else 1f
                    drawMercenary(
                        mercenary = mercenary,
                        x = heroX + side * 54f,
                        y = heroY + 52f + index * 12f,
                        facing = facing,
                        walking = walking,
                        animTime = animTime + index * 0.4f
                    )
                }
                drawHero(heroX, heroY, facing, walking, animTime)
            }
        }

        // 건물 이름표
        Village.places.forEach { p ->
            val labelWorldY = if (p.id == PlaceId.CHURCH) p.bottom + 2f else p.top - 34f
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (ox + p.left * s).roundToInt(),
                            (oy + labelWorldY * s).roundToInt()
                        )
                    }
                    .width(with(density) { (p.w * s).toDp() }),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xCC2E2015), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = p.name,
                        color = Palette.Parchment,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // 문 앞에 서 있으면 입장 버튼
        val near = Village.places.firstOrNull {
            hypot(vm.heroX - it.doorX, vm.heroY - it.doorY) < 42f
        }
        if (near != null && !vm.walking) {
            Button(
                onClick = { vm.enterPlace(near.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.Gold,
                    contentColor = Palette.Ink
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            ) {
                Text("${near.name} 들어가기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------------------------------------------------------------------ 지면

private fun DrawScope.drawGround() {
    // Kenney식 타일 느낌의 잔디 지대
    drawRect(Kenney.Grass, Offset(0f, 0f), Size(Village.W, Village.H))
    drawOval(Palette.SkyGlow, Offset(-80f, -120f), Size(Village.W * 0.7f, 280f))
    grassBlobs.forEach { (x, y, r) ->
        kOval(Kenney.GrassDark.copy(alpha = 0.55f), x - r, y - r * 0.5f, r * 2f, r, outline = false)
        kOval(Kenney.GrassLight.copy(alpha = 0.28f), x - r * 0.55f, y - r * 0.35f, r * 1.1f, r * 0.55f, outline = false)
    }
    flowers.forEach { (x, y, kind) ->
        val c = when (kind) {
            0 -> Color(0xFFE8D26A)
            1 -> Color(0xFFE3E0DA)
            else -> Color(0xFFD98BB0)
        }
        drawCircle(Color(0xFF4E6B3A), 1.6f, Offset(x, y + 2f))
        kCircle(c, 3.4f, Offset(x, y), stroke = 1.5f)
    }
}

private fun DrawScope.drawRoads() {
    val hw = Village.ROAD_W / 2f

    // 세로 대로
    roadSegment(
        Village.ROAD_X - hw,
        Village.ROAD_TOP - hw,
        Village.ROAD_W,
        Village.BOTTOM_ROAD_Y - Village.ROAD_TOP + Village.ROAD_W
    )
    // 좌우 건물로 이어지는 가로길
    Village.rowRoads.forEach { y ->
        roadSegment(
            Village.ROW_ROAD_LEFT - hw,
            y - hw,
            (Village.ROW_ROAD_RIGHT - Village.ROW_ROAD_LEFT) + Village.ROAD_W,
            Village.ROAD_W
        )
    }
    // 하단 길
    roadSegment(
        Village.BOTTOM_ROAD_LEFT - hw,
        Village.BOTTOM_ROAD_Y - hw,
        (Village.BOTTOM_ROAD_RIGHT - Village.BOTTOM_ROAD_LEFT) + Village.ROAD_W,
        Village.ROAD_W
    )

    // 광장 (세로 대로와 3번째 가로길이 만나는 곳)
    val plazaR = 132f
    val py = Village.rowRoads[2]
    drawCircle(Palette.Stone.copy(alpha = 0.9f), plazaR, Offset(Village.ROAD_X, py))
    drawCircle(Color(0x33000000), plazaR, Offset(Village.ROAD_X, py), style = Stroke(width = 4f))
    for (i in 0 until 12) {
        val a = Math.PI * 2 * i / 12
        drawLine(
            Color(0x22000000),
            Offset(Village.ROAD_X, py),
            Offset(
                Village.ROAD_X + plazaR * cos(a).toFloat(),
                py + plazaR * sin(a).toFloat()
            ),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.roadSegment(x: Float, y: Float, w: Float, h: Float) {
    kRound(Kenney.DirtDark, x - 5f, y - 5f, w + 10f, h + 10f, 18f, stroke = 4f)
    kRound(Kenney.Dirt, x, y, w, h, 16f, stroke = 3.5f)
    kRound(Kenney.DirtLight.copy(alpha = 0.45f), x + 4f, y + 3f, w - 8f, 5f, 8f, outline = false)
    pebbles.forEach { (fx, fy, r) ->
        drawCircle(Kenney.DirtDark.copy(alpha = 0.55f), r, Offset(x + fx * w, y + fy * h))
    }
}

// ------------------------------------------------------------------ 소품

private fun DrawScope.drawScenery() {
    Village.trees.forEach { (x, y, r) -> drawTree(x, y, r) }
    Village.lamps.forEach { (x, y) -> drawLamp(x, y) }
    drawWell(Village.WELL_X, Village.WELL_Y)
    Village.stalls.forEach { (x, y, kind) -> drawStall(x, y, kind) }
}

private fun DrawScope.drawTree(x: Float, y: Float, r: Float) {
    kOval(Kenney.Shadow, x - r * 0.8f, y - r * 0.16f, r * 1.6f, r * 0.5f, outline = false)
    kRect(Kenney.Wood, x - r * 0.14f, y - r * 1.05f, r * 0.28f, r * 1.05f, stroke = 3f)
    kCircle(Color(0xFF3F6B34), r * 0.78f, Offset(x, y - r * 1.35f))
    kCircle(Color(0xFF4E8341), r * 0.62f, Offset(x - r * 0.34f, y - r * 1.55f))
    kCircle(Kenney.GrassLight, r * 0.42f, Offset(x, y - r * 1.85f))
}

private fun DrawScope.drawLamp(x: Float, y: Float) {
    drawOval(Color(0x33000000), Offset(x - 12f, y - 5f), Size(24f, 10f))
    drawRect(Color(0xFF3B3733), Offset(x - 3f, y - 62f), Size(6f, 62f))
    drawRect(Color(0xFF2E2A26), Offset(x - 10f, y - 80f), Size(20f, 20f))
    drawRect(Color(0xFF4A4038), Offset(x - 11f, y - 82f), Size(22f, 4f))
    drawCircle(Color(0xFFF7D46A), 7f, Offset(x, y - 70f))
    drawCircle(Color(0x66F7D46A), 16f, Offset(x, y - 70f))
    drawCircle(Color(0x33F0C48A), 28f, Offset(x, y - 70f))
}

private fun DrawScope.drawWell(x: Float, y: Float) {
    drawOval(Color(0x33000000), Offset(x - 36f, y - 10f), Size(72f, 26f))
    drawOval(Palette.Stone, Offset(x - 34f, y - 28f), Size(68f, 42f))
    drawOval(Palette.StoneLight, Offset(x - 28f, y - 30f), Size(56f, 18f))
    drawOval(Color(0xFF2C3B4A), Offset(x - 23f, y - 20f), Size(46f, 27f))
    drawOval(Palette.Water, Offset(x - 18f, y - 15f), Size(36f, 18f))
    drawOval(Color(0x55FFFFFF), Offset(x - 10f, y - 16f), Size(14f, 6f))
    drawRect(Color(0xFF6B4B2E), Offset(x - 30f, y - 74f), Size(6f, 52f))
    drawRect(Color(0xFF6B4B2E), Offset(x + 24f, y - 74f), Size(6f, 52f))
    val roof = androidx.compose.ui.graphics.Path().apply {
        moveTo(x - 42f, y - 70f)
        lineTo(x, y - 100f)
        lineTo(x + 42f, y - 70f)
        close()
    }
    drawPath(roof, Color(0xFF8A5A2B))
    drawPath(roof, Color(0x33000000), style = Stroke(2.5f))
    drawLine(Color(0xFF4A3524), Offset(x - 26f, y - 72f), Offset(x + 26f, y - 72f), strokeWidth = 4f)
    // 두레박
    drawLine(Color(0xFF3B2A1A), Offset(x, y - 72f), Offset(x, y - 28f), 2f)
    drawRect(Color(0xFF6B4B2E), Offset(x - 7f, y - 34f), Size(14f, 12f))
}

private fun DrawScope.drawStall(x: Float, y: Float, kind: Int) {
    val cloth = if (kind == 0) Color(0xFFD8503F) else Color(0xFF3E6B8A)
    drawOval(Color(0x33000000), Offset(x - 44f, y - 6f), Size(88f, 20f))
    // 다리
    drawRect(Color(0xFF6B4B2E), Offset(x - 40f, y - 46f), Size(5f, 46f))
    drawRect(Color(0xFF6B4B2E), Offset(x + 35f, y - 46f), Size(5f, 46f))
    // 상판
    drawRect(Color(0xFF8A5A2B), Offset(x - 44f, y - 52f), Size(88f, 10f))
    // 차양
    val awn = androidx.compose.ui.graphics.Path().apply {
        moveTo(x - 50f, y - 72f)
        lineTo(x, y - 96f)
        lineTo(x + 50f, y - 72f)
        close()
    }
    drawPath(awn, cloth)
    // 진열품
    drawCircle(Color(0xFFC0392B), 7f, Offset(x - 22f, y - 59f))
    drawCircle(Color(0xFFE0A430), 7f, Offset(x - 4f, y - 59f))
    drawCircle(Color(0xFF6A8F3C), 7f, Offset(x + 16f, y - 59f))
}
