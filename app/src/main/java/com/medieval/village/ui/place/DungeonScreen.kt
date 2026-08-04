package com.medieval.village.ui.place

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.Facing
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.DungeonCell
import com.medieval.village.model.DungeonFloor
import com.medieval.village.model.DungeonLayout
import com.medieval.village.ui.Chip
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawMercenary
import kotlin.math.min
import kotlin.math.sin

private val Rock = Color(0xFF5A544C)
private val RockLight = Color(0xFF726B61)
private val RockDark = Color(0xFF3B3630)
private val RockOutline = Color(0xFF231E19)
private val FloorA = Color(0xFFC9A876)
private val FloorB = Color(0xFFB8955F)
private val FloorShadow = Color(0x33231810)
private val TorchOrange = Color(0xFFE8843A)
private val TorchYellow = Color(0xFFF9DE85)
private val ParchmentFrame = Color(0xFFEFE0C0)
private val WoodFrame = Color(0xFF4A3524)

@Composable
fun DungeonScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val floorNum = vm.player.dungeonDepth + 1
    val layout = remember(floorNum) { DungeonLayout.forFloor(floorNum) }
    var animPhase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dt = ((now - last) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
                    animPhase += dt * 4f
                }
                last = now
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Palette.WoodDark)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "잊혀진 지하 · ${floorNum}층",
                    color = Palette.Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "횃불이 비추는 돌복도를 따라 내려간다",
                    color = Palette.ParchmentDim,
                    fontSize = 11.sp
                )
            }
            Chip("최고 ${vm.player.dungeonDepth}층", Palette.WoodLight)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color(0xFF1A1510), RoundedCornerShape(12.dp))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val scale = min(widthPx / DungeonLayout.WORLD_W, heightPx / DungeonLayout.WORLD_H)
            val offsetX = (widthPx - DungeonLayout.WORLD_W * scale) / 2f
            val offsetY = (heightPx - DungeonLayout.WORLD_H * scale) / 2f
            val party = vm.activeParty
            val phase = animPhase

            Canvas(modifier = Modifier.fillMaxSize()) {
                withTransform({
                    translate(offsetX, offsetY)
                    scale(scale, scale, Offset.Zero)
                }) {
                    drawCartoonDungeonMap(layout, phase)
                    // 동료
                    party.forEachIndexed { index, merc ->
                        val spot = layout.companions.getOrElse(index) { layout.companions.last() }
                        val (cx, cy) = DungeonLayout.cellCenter(spot.x, spot.y)
                        withTransform({
                            translate(cx, cy + 8f)
                            scale(0.42f, 0.42f, Offset.Zero)
                        }) {
                            drawMercenary(merc, 0f, 0f, Facing.UP, false, phase + index)
                        }
                    }
                    // 주인공
                    val (hx, hy) = DungeonLayout.cellCenter(layout.hero.x, layout.hero.y)
                    withTransform({
                        translate(hx, hy + 10f)
                        scale(0.48f, 0.48f, Offset.Zero)
                    }) {
                        drawHero(0f, 0f, Facing.UP, false, phase)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("활성 동료 ${vm.activeParty.size}명", Palette.Moss)
                Chip("공격 ${vm.totalAtk + vm.partyPower}", Palette.Blood)
            }
            Spacer(modifier.height(6.dp))
            MessageLog(vm.log, Modifier.height(78.dp))
            Spacer(modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val potion = vm.inventory.toList().firstOrNull { it.item.healHp > 0 }
                if (potion != null) {
                    WoodButton("물약") { vm.useItem(potion.item) }
                }
                WoodButton(
                    "탐험하기",
                    modifier = Modifier.weight(1f),
                    highlight = true
                ) { vm.exploreDungeon() }
            }
            Spacer(modifier.height(7.dp))
            WoodButton("마을로 나가기", Modifier.fillMaxWidth(), highlight = true) {
                vm.leavePlace()
            }
        }
    }
}

/** 양피지 프레임 + 두꺼운 외곽선 돌미로 만화 지도 */
private fun DrawScope.drawCartoonDungeonMap(floor: DungeonFloor, phase: Float) {
    val w = DungeonLayout.WORLD_W
    val h = DungeonLayout.WORLD_H
    val cell = DungeonLayout.CELL
    val padX = DungeonLayout.PAD_X
    val padY = DungeonLayout.PAD_Y

    // 어두운 배경
    drawRect(Color(0xFF1E1914), size = Size(w, h))

    // 양피지 보드
    val boardL = padX - 28f
    val boardT = padY - 48f
    val boardW = floor.cols * cell + 56f
    val boardH = floor.rows * cell + 72f
    drawRoundRect(WoodFrame, Offset(boardL - 10f, boardT - 10f), Size(boardW + 20f, boardH + 20f), CornerRadius(22f))
    drawRoundRect(ParchmentFrame, Offset(boardL, boardT), Size(boardW, boardH), CornerRadius(16f))
    drawRoundRect(Color(0xFFD9C8A4), Offset(boardL + 8f, boardT + 8f), Size(boardW - 16f, boardH - 16f), CornerRadius(12f))

    // 제목 띠
    drawRoundRect(
        Color(0xFF3B2A1A),
        Offset(w * 0.28f, boardT + 10f),
        Size(w * 0.44f, 36f),
        CornerRadius(10f)
    )
    drawLabel("지하 ${floor.floor}층", w * 0.36f, boardT + 36f, 26f, Palette.Gold)

    // 바닥 타일
    for (y in 0 until floor.rows) {
        for (x in 0 until floor.cols) {
            if (floor.cell(x, y) == DungeonCell.WALL) continue
            val left = padX + x * cell
            val top = padY + y * cell
            val tone = if ((x + y) % 2 == 0) FloorA else FloorB
            drawRoundRect(tone, Offset(left + 2f, top + 2f), Size(cell - 4f, cell - 4f), CornerRadius(6f))
            drawRoundRect(FloorShadow, Offset(left + 2f, top + cell * 0.62f), Size(cell - 4f, cell * 0.28f), CornerRadius(4f))
        }
    }

    // 돌벽 (만화 블롭)
    for (y in 0 until floor.rows) {
        for (x in 0 until floor.cols) {
            if (floor.cell(x, y) != DungeonCell.WALL) continue
            if (!touchesFloor(floor, x, y)) continue
            drawCartoonWall(padX + x * cell, padY + y * cell, cell, (x * 13 + y * 7) % 3)
        }
    }

    // 벽 외곽선 (통로 가장자리)
    for (y in 0 until floor.rows) {
        for (x in 0 until floor.cols) {
            if (floor.cell(x, y) == DungeonCell.WALL) continue
            val left = padX + x * cell
            val top = padY + y * cell
            if (floor.cell(x, y - 1) == DungeonCell.WALL) {
                drawLine(RockOutline, Offset(left + 4f, top + 3f), Offset(left + cell - 4f, top + 3f), 5f)
            }
            if (floor.cell(x, y + 1) == DungeonCell.WALL) {
                drawLine(RockOutline, Offset(left + 4f, top + cell - 3f), Offset(left + cell - 4f, top + cell - 3f), 5f)
            }
            if (floor.cell(x - 1, y) == DungeonCell.WALL) {
                drawLine(RockOutline, Offset(left + 3f, top + 4f), Offset(left + 3f, top + cell - 4f), 5f)
            }
            if (floor.cell(x + 1, y) == DungeonCell.WALL) {
                drawLine(RockOutline, Offset(left + cell - 3f, top + 4f), Offset(left + cell - 3f, top + cell - 4f), 5f)
            }
        }
    }

    // 특수 오브젝트
    for (y in 0 until floor.rows) {
        for (x in 0 until floor.cols) {
            val (cx, cy) = DungeonLayout.cellCenter(x, y)
            when (floor.cell(x, y)) {
                DungeonCell.ENTRANCE -> drawStairsUp(cx, cy, cell)
                DungeonCell.EXIT -> drawStairsDown(cx, cy, cell)
                DungeonCell.CHEST -> drawTreasureChest(cx, cy, cell)
                DungeonCell.GATE -> drawIronGate(cx, cy, cell)
                DungeonCell.TORCH -> drawWallTorch(cx, cy, cell, phase + x + y)
                else -> Unit
            }
        }
    }

    floor.slime?.let { s ->
        val (sx, sy) = DungeonLayout.cellCenter(s.x, s.y)
        drawCuteSlime(sx, sy, cell, phase)
    }

    // 범례
    drawLegend(boardL + 18f, boardT + boardH - 28f)
}

private fun touchesFloor(floor: DungeonFloor, x: Int, y: Int): Boolean {
    val dirs = arrayOf(0 to -1, 0 to 1, -1 to 0, 1 to 0)
    return dirs.any { (dx, dy) -> floor.cell(x + dx, y + dy) != DungeonCell.WALL }
}

private fun DrawScope.drawCartoonWall(left: Float, top: Float, cell: Float, variant: Int) {
    val inset = 3f
    val path = Path().apply {
        when (variant) {
            0 -> {
                moveTo(left + inset, top + cell * 0.35f)
                quadraticBezierTo(left + cell * 0.15f, top + inset, left + cell * 0.45f, top + inset + 2f)
                quadraticBezierTo(left + cell * 0.75f, top + inset, left + cell - inset, top + cell * 0.28f)
                quadraticBezierTo(left + cell - inset - 1f, top + cell * 0.65f, left + cell * 0.70f, top + cell - inset)
                quadraticBezierTo(left + cell * 0.40f, top + cell - inset + 1f, left + inset + 2f, top + cell * 0.70f)
                close()
            }
            1 -> {
                moveTo(left + inset + 2f, top + cell * 0.25f)
                lineTo(left + cell * 0.55f, top + inset)
                lineTo(left + cell - inset, top + cell * 0.40f)
                lineTo(left + cell - inset - 2f, top + cell - inset)
                lineTo(left + inset, top + cell * 0.75f)
                close()
            }
            else -> {
                moveTo(left + inset, top + cell * 0.45f)
                quadraticBezierTo(left + cell * 0.20f, top + inset + 4f, left + cell * 0.55f, top + inset)
                quadraticBezierTo(left + cell - inset, top + cell * 0.20f, left + cell - inset, top + cell * 0.55f)
                quadraticBezierTo(left + cell * 0.60f, top + cell - inset, left + cell * 0.25f, top + cell - inset)
                close()
            }
        }
    }
    drawPath(path, Rock)
    drawPath(path, RockOutline, style = Stroke(width = 4.5f))
    // 하이라이트 돌무늬
    drawCircle(RockLight.copy(alpha = 0.55f), cell * 0.10f, Offset(left + cell * 0.35f, top + cell * 0.38f))
    drawCircle(RockDark.copy(alpha = 0.45f), cell * 0.08f, Offset(left + cell * 0.62f, top + cell * 0.58f))
}

private fun DrawScope.drawWallTorch(cx: Float, cy: Float, cell: Float, phase: Float) {
    val flicker = 1f + 0.08f * sin(phase * 3.2f)
    drawCircle(Color(0x55E8843A), cell * 0.42f * flicker, Offset(cx, cy - cell * 0.08f))
    drawRect(Color(0xFF5A3A22), Offset(cx - 3.5f, cy - cell * 0.02f), Size(7f, cell * 0.28f))
    drawCircle(TorchOrange, cell * 0.14f * flicker, Offset(cx, cy - cell * 0.12f))
    drawCircle(TorchYellow, cell * 0.07f, Offset(cx, cy - cell * 0.14f))
}

private fun DrawScope.drawStairsUp(cx: Float, cy: Float, cell: Float) {
    for (i in 0..3) {
        val t = i / 3f
        drawRoundRect(
            Color(0xFF8A7350).copy(alpha = 0.95f - t * 0.15f),
            Offset(cx - cell * (0.32f - i * 0.04f), cy - cell * 0.28f + i * cell * 0.12f),
            Size(cell * (0.64f - i * 0.08f), cell * 0.10f),
            CornerRadius(3f)
        )
        drawLine(
            RockOutline,
            Offset(cx - cell * (0.32f - i * 0.04f), cy - cell * 0.28f + i * cell * 0.12f),
            Offset(cx + cell * (0.32f - i * 0.04f), cy - cell * 0.28f + i * cell * 0.12f),
            2.5f
        )
    }
    drawLabel("입구", cx - 18f, cy + cell * 0.38f, 14f, Color(0xFF5A4231))
}

private fun DrawScope.drawStairsDown(cx: Float, cy: Float, cell: Float) {
    drawCircle(Color(0xFF2A2218), cell * 0.30f, Offset(cx, cy))
    drawCircle(RockOutline, cell * 0.30f, Offset(cx, cy), style = Stroke(4f))
    // 나선형 느낌
    drawArc(
        Color(0xFFD9A441),
        startAngle = 20f,
        sweepAngle = 240f,
        useCenter = false,
        topLeft = Offset(cx - cell * 0.18f, cy - cell * 0.18f),
        size = Size(cell * 0.36f, cell * 0.36f),
        style = Stroke(4f)
    )
    drawLabel("아래", cx - 16f, cy + cell * 0.40f, 14f, Color(0xFF5A4231))
}

private fun DrawScope.drawTreasureChest(cx: Float, cy: Float, cell: Float) {
    drawRoundRect(Color(0xFF6B4B2E), Offset(cx - cell * 0.22f, cy - cell * 0.08f), Size(cell * 0.44f, cell * 0.26f), CornerRadius(4f))
    drawRoundRect(Color(0xFF8A5A2B), Offset(cx - cell * 0.22f, cy - cell * 0.20f), Size(cell * 0.44f, cell * 0.16f), CornerRadius(4f))
    drawRoundRect(Color(0xFFD9A441), Offset(cx - cell * 0.22f, cy - cell * 0.06f), Size(cell * 0.44f, 4f), CornerRadius(2f))
    drawCircle(Color(0xFFF9DE85), 5f, Offset(cx, cy + 2f))
    drawRoundRect(RockOutline, Offset(cx - cell * 0.22f, cy - cell * 0.20f), Size(cell * 0.44f, cell * 0.38f), CornerRadius(4f), style = Stroke(3f))
}

private fun DrawScope.drawIronGate(cx: Float, cy: Float, cell: Float) {
    drawRoundRect(Color(0xFF3A3733), Offset(cx - cell * 0.20f, cy - cell * 0.28f), Size(cell * 0.40f, cell * 0.50f), CornerRadius(3f))
    for (i in 0..3) {
        val x = cx - cell * 0.14f + i * cell * 0.09f
        drawLine(Color(0xFF7E858C), Offset(x, cy - cell * 0.24f), Offset(x, cy + cell * 0.18f), 4f)
    }
    drawLine(Color(0xFFBFC5CC), Offset(cx - cell * 0.18f, cy - cell * 0.02f), Offset(cx + cell * 0.18f, cy - cell * 0.02f), 5f)
    drawCircle(Color(0xFFD9A441), 4f, Offset(cx + cell * 0.12f, cy))
    drawRoundRect(RockOutline, Offset(cx - cell * 0.20f, cy - cell * 0.28f), Size(cell * 0.40f, cell * 0.50f), CornerRadius(3f), style = Stroke(3f))
}

private fun DrawScope.drawCuteSlime(cx: Float, cy: Float, cell: Float, phase: Float) {
    val bob = sin(phase * 2.4f) * 2.5f
    val bodyTop = cy - cell * 0.18f + bob
    drawOval(Color(0x33000000), Offset(cx - cell * 0.18f, cy + cell * 0.10f), Size(cell * 0.36f, cell * 0.12f))
    val body = Path().apply {
        moveTo(cx - cell * 0.22f, bodyTop + cell * 0.22f)
        quadraticBezierTo(cx - cell * 0.24f, bodyTop, cx, bodyTop - cell * 0.02f)
        quadraticBezierTo(cx + cell * 0.24f, bodyTop, cx + cell * 0.22f, bodyTop + cell * 0.22f)
        close()
    }
    drawPath(body, Color(0xFF6FBF5A))
    drawPath(body, Color(0xFF2F5A28), style = Stroke(3.5f))
    drawCircle(Color(0x99D8FFC8), cell * 0.06f, Offset(cx - cell * 0.06f, bodyTop + cell * 0.04f))
    drawCircle(Color(0xFF1E2A18), 3.2f, Offset(cx - cell * 0.07f, bodyTop + cell * 0.10f))
    drawCircle(Color(0xFF1E2A18), 3.2f, Offset(cx + cell * 0.07f, bodyTop + cell * 0.10f))
    drawCircle(Color.White, 1.4f, Offset(cx - cell * 0.06f, bodyTop + cell * 0.09f))
    drawCircle(Color.White, 1.4f, Offset(cx + cell * 0.08f, bodyTop + cell * 0.09f))
}

private fun DrawScope.drawLegend(x: Float, y: Float) {
    drawCircle(TorchOrange, 5f, Offset(x, y))
    drawLabel("횃불", x + 10f, y + 5f, 12f, Color(0xFF5A4231))
    drawRoundRect(Color(0xFF8A5A2B), Offset(x + 70f, y - 6f), Size(12f, 10f), CornerRadius(2f))
    drawLabel("상자", x + 86f, y + 5f, 12f, Color(0xFF5A4231))
    drawCircle(Color(0xFF6FBF5A), 5f, Offset(x + 150f, y))
    drawLabel("몬스터", x + 160f, y + 5f, 12f, Color(0xFF5A4231))
}

private fun DrawScope.drawLabel(text: String, x: Float, y: Float, size: Float, color: Color) {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = size
            this.color = android.graphics.Color.argb(
                (color.alpha * 255).toInt(),
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.nativeCanvas.drawText(text, x, y, paint)
    }
}
