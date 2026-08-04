package com.medieval.village.ui.place

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.DungeonFactory
import com.medieval.village.model.DungeonFloor
import com.medieval.village.model.DungeonMonster
import com.medieval.village.model.DungeonTile
import com.medieval.village.ui.Chip
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawMercenary
import com.medieval.village.ui.village.rememberCustomArt
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@Composable
fun DungeonScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val art = rememberCustomArt()
    val floor = vm.dungeonFloor
    Column(modifier = modifier.fillMaxSize().background(Color(0xFF14100C))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "잊혀진 지하 · ${vm.dungeonFloorNumber}층",
                    color = Palette.Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "횃불이 비추는 돌복도 — 화면을 눌러 이동",
                    color = Palette.ParchmentDim,
                    fontSize = 10.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("기록 ${vm.player.dungeonDepth}층", Palette.WoodLight)
                Chip("좀비 ${floor?.monsters?.count { it.alive } ?: 0}", Palette.Blood)
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color(0xFFD9C8A4), RoundedCornerShape(12.dp))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val facing = vm.facing
            val walking = vm.dungeonWalking
            val walkPhase = vm.walkPhase
            val heroX = vm.dungeonHeroX
            val heroY = vm.dungeonHeroY
            val party = vm.activeParty

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(floor?.floor, widthPx, heightPx) {
                        detectTapGestures { tap ->
                            val map = vm.dungeonFloor ?: return@detectTapGestures
                            val cam = cameraOffset(map, vm.dungeonHeroX, vm.dungeonHeroY, widthPx, heightPx)
                            val worldX = tap.x + cam.first
                            val worldY = tap.y + cam.second
                            val zombie = map.monsters
                                .filter { it.alive }
                                .minByOrNull { hypot(worldX - it.x, worldY - it.y) }
                                ?.takeIf { hypot(worldX - it.x, worldY - it.y) < DungeonFactory.TILE * 0.9f }
                            if (zombie != null) {
                                vm.approachDungeonMonster(zombie)
                            } else {
                                vm.walkInDungeon(worldX, worldY)
                            }
                        }
                    }
            ) {
                // 화면 좌표계 배경 — 변환 실패해도 빈 화면이 되지 않게
                drawRect(Color(0xFFEFE0C0), size = size)
                drawRoundRect(
                    Color(0xFF4A3524),
                    Offset(6f, 6f),
                    Size(size.width - 12f, size.height - 12f),
                    CornerRadius(14f, 14f),
                    style = Stroke(5f)
                )

                val map = floor
                if (map == null) {
                    drawLabel("지도를 펼치는 중…", size.width * 0.28f, size.height * 0.5f, 28f, Color(0xFF5A4231))
                    return@Canvas
                }

                val viewW = size.width.coerceAtLeast(1f)
                val viewH = size.height.coerceAtLeast(1f)
                val (camX, camY) = cameraOffset(map, heroX, heroY, viewW, viewH)
                withTransform({
                    translate(-camX, -camY)
                }) {
                    drawDungeonFloor(map)
                    map.monsters.filter { it.alive }.forEach { drawZombie(art, it) }
                    party.forEachIndexed { index, mercenary ->
                        drawMercenary(
                            mercenary = mercenary,
                            x = heroX + if (index == 0) -40f else 40f,
                            y = heroY + 28f + index * 6f,
                            facing = facing,
                            walking = walking,
                            phase = walkPhase + index * 0.7f,
                            scale = 0.72f,
                        )
                    }
                    drawHero(
                        heroX,
                        heroY,
                        facing,
                        walking,
                        walkPhase,
                        scale = 0.78f,
                    )
                }
                drawMinimap(map, heroX, heroY, viewW, viewH)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .background(Color(0xAA1B120A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    when (vm.dungeonHint) {
                        "stairs_up" -> "↑ 지상 출구 — 아래에서 ‘탈출’"
                        "stairs_down" -> "↓ 더 깊은 층 — 아래에서 ‘내려가기’"
                        else -> "화면을 눌러 이동 · 좀비를 누르면 다가가 전투"
                    },
                    color = Palette.Parchment,
                    fontSize = 11.sp
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            MessageLog(vm.log, Modifier.height(78.dp))
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (vm.dungeonHint) {
                    "stairs_up" -> WoodButton("탈출", Modifier.weight(1f), highlight = true) {
                        vm.escapeDungeon()
                    }
                    "stairs_down" -> WoodButton("내려가기", Modifier.weight(1f), highlight = true) {
                        vm.descendDungeon()
                    }
                    else -> WoodButton("탈출", Modifier.weight(1f)) { vm.escapeDungeon() }
                }
                val potion = vm.inventory.toList().firstOrNull { it.item.healHp > 0 }
                WoodButton(
                    text = if (potion != null) "물약" else "물약 없음",
                    modifier = Modifier.weight(1f),
                    enabled = potion != null
                ) {
                    potion?.let { vm.useItem(it.item) }
                }
            }
        }
    }
}

private fun cameraOffset(
    map: DungeonFloor,
    heroX: Float,
    heroY: Float,
    viewW: Float,
    viewH: Float
): Pair<Float, Float> {
    val maxX = max(0f, map.worldW - viewW)
    val maxY = max(0f, map.worldH - viewH)
    val camX = (heroX - viewW / 2f).coerceIn(0f, maxX)
    val camY = (heroY - viewH / 2f).coerceIn(0f, maxY)
    return camX to camY
}

private fun DrawScope.drawDungeonFloor(map: DungeonFloor) {
    // 만화풍: 밝은 양피지 바탕 + 두꺼운 돌벽 외곽선
    drawRect(Color(0xFFCDB892), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val rock = Color(0xFF5A544C)
    val rockLight = Color(0xFF726B61)
    val rockDark = Color(0xFF3B3630)
    val outline = Color(0xFF231E19)
    val floorA = Color(0xFFC9A876)
    val floorB = Color(0xFFB8955F)

    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val tile = map.tileAt(c, r)
            val x = c * ts
            val y = r * ts
            when (tile) {
                DungeonTile.WALL -> {
                    drawRoundRect(rock, Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), CornerRadius(8f, 8f))
                    drawRoundRect(outline, Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), CornerRadius(8f, 8f), style = Stroke(4f))
                    drawCircle(rockLight.copy(alpha = 0.55f), ts * 0.12f, Offset(x + ts * 0.35f, y + ts * 0.35f))
                    drawCircle(rockDark.copy(alpha = 0.4f), ts * 0.10f, Offset(x + ts * 0.65f, y + ts * 0.62f))
                }
                DungeonTile.FLOOR -> {
                    val tone = if ((c + r) % 2 == 0) floorA else floorB
                    drawRoundRect(tone, Offset(x + 3f, y + 3f), Size(ts - 6f, ts - 6f), CornerRadius(6f, 6f))
                }
                DungeonTile.SEWER -> {
                    drawRoundRect(Color(0xFF8FAE7A), Offset(x + 3f, y + 3f), Size(ts - 6f, ts - 6f), CornerRadius(6f, 6f))
                    drawRoundRect(Color(0xFF4E6B3A), Offset(x + ts * 0.35f, y + 6f), Size(ts * 0.30f, ts - 12f), CornerRadius(4f, 4f))
                    drawCircle(Color(0x6626A86A), 12f, Offset(x + ts / 2f, y + ts * 0.72f))
                }
                DungeonTile.VAULT -> {
                    drawRoundRect(floorA, Offset(x + 3f, y + 3f), Size(ts - 6f, ts - 6f), CornerRadius(6f, 6f))
                    // 보물상자
                    drawRoundRect(Color(0xFF6B4B2E), Offset(x + ts * 0.22f, y + ts * 0.42f), Size(ts * 0.56f, ts * 0.32f), CornerRadius(4f, 4f))
                    drawRoundRect(Color(0xFF8A5A2B), Offset(x + ts * 0.22f, y + ts * 0.28f), Size(ts * 0.56f, ts * 0.20f), CornerRadius(4f, 4f))
                    drawRoundRect(Color(0xFFD9A441), Offset(x + ts * 0.22f, y + ts * 0.44f), Size(ts * 0.56f, 4f), CornerRadius(2f, 2f))
                    drawCircle(Color(0xFFF9DE85), 5f, Offset(x + ts / 2f, y + ts * 0.55f))
                    drawRoundRect(outline, Offset(x + ts * 0.22f, y + ts * 0.28f), Size(ts * 0.56f, ts * 0.46f), CornerRadius(4f, 4f), style = Stroke(3f))
                }
                DungeonTile.STAIRS_UP -> {
                    drawRoundRect(floorA, Offset(x + 3f, y + 3f), Size(ts - 6f, ts - 6f), CornerRadius(6f, 6f))
                    for (i in 0..3) {
                        drawRoundRect(
                            Color(0xFF8A7350),
                            Offset(x + 10f + i * 4f, y + 12f + i * 10f),
                            Size(ts - 20f - i * 8f, 8f),
                            CornerRadius(3f, 3f)
                        )
                        drawLine(
                            outline,
                            Offset(x + 10f + i * 4f, y + 12f + i * 10f),
                            Offset(x + ts - 10f - i * 4f, y + 12f + i * 10f),
                            2.5f
                        )
                    }
                    drawLabel("입구", x + 14f, y + ts - 8f, 16f, Color(0xFF5A4231))
                }
                DungeonTile.STAIRS_DOWN -> {
                    drawRoundRect(floorB, Offset(x + 3f, y + 3f), Size(ts - 6f, ts - 6f), CornerRadius(6f, 6f))
                    drawCircle(Color(0xFF2A2218), ts * 0.28f, Offset(x + ts / 2f, y + ts / 2f))
                    drawCircle(outline, ts * 0.28f, Offset(x + ts / 2f, y + ts / 2f), style = Stroke(4f))
                    drawArc(
                        Color(0xFFD9A441),
                        startAngle = 20f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = Offset(x + ts * 0.28f, y + ts * 0.28f),
                        size = Size(ts * 0.44f, ts * 0.44f),
                        style = Stroke(4f)
                    )
                    drawLabel("아래", x + 14f, y + ts - 8f, 16f, Color(0xFF5A4231))
                }
            }
            // 횃불
            if (tile != DungeonTile.WALL && (c * 17 + r * 31) % 23 == 0) {
                drawCircle(Color(0x55E8843A), 22f, Offset(x + ts / 2f, y + 14f))
                drawRect(Color(0xFF5A3A22), Offset(x + ts / 2f - 3.5f, y + 16f), Size(7f, ts * 0.28f))
                drawCircle(Color(0xFFE8843A), 8f, Offset(x + ts / 2f, y + 12f))
                drawCircle(Color(0xFFF9DE85), 4f, Offset(x + ts / 2f, y + 10f))
            }
        }
    }

    // 통로 가장자리 두꺼운 외곽선
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            val x = c * ts
            val y = r * ts
            if (map.tileAt(c, r - 1) == DungeonTile.WALL) {
                drawLine(outline, Offset(x + 4f, y + 3f), Offset(x + ts - 4f, y + 3f), 5f)
            }
            if (map.tileAt(c, r + 1) == DungeonTile.WALL) {
                drawLine(outline, Offset(x + 4f, y + ts - 3f), Offset(x + ts - 4f, y + ts - 3f), 5f)
            }
            if (map.tileAt(c - 1, r) == DungeonTile.WALL) {
                drawLine(outline, Offset(x + 3f, y + 4f), Offset(x + 3f, y + ts - 4f), 5f)
            }
            if (map.tileAt(c + 1, r) == DungeonTile.WALL) {
                drawLine(outline, Offset(x + ts - 3f, y + 4f), Offset(x + ts - 3f, y + ts - 4f), 5f)
            }
        }
    }
}

private fun DrawScope.drawZombie(art: CustomArt, monster: DungeonMonster) {
    val x = monster.x
    val y = monster.y
    drawOval(Color(0x55000000), Offset(x - 22f, y - 4f), Size(44f, 14f))
    drawCustomSprite(
        image = art.zombieSprite(monster.kind),
        cx = x,
        footY = y,
        worldHeight = 64f,
    )
    drawLabel(monster.name, x - 48f, y + 14f, 13f, Color(0xFFE8B4B4))
}

private fun DrawScope.drawMinimap(
    map: DungeonFloor,
    heroX: Float,
    heroY: Float,
    viewW: Float,
    viewH: Float
) {
    val mw = min(140f, viewW * 0.28f)
    val mh = mw * (map.rows.toFloat() / map.cols)
    val left = viewW - mw - 12f
    val top = 12f
    drawRoundRect(Color(0xAA0D0B09), Offset(left - 4f, top - 4f), Size(mw + 8f, mh + 8f), CornerRadius(8f, 8f))
    val sx = mw / map.worldW
    val sy = mh / map.worldH
    for (r in 0 until map.rows step 1) {
        for (c in 0 until map.cols step 1) {
            val tile = map.tileAt(c, r)
            if (tile == DungeonTile.WALL) continue
            val color = when (tile) {
                DungeonTile.STAIRS_UP -> Color(0xFF8FCF7A)
                DungeonTile.STAIRS_DOWN -> Color(0xFFC0392B)
                DungeonTile.SEWER -> Color(0xFF6F9A54)
                DungeonTile.VAULT -> Color(0xFFD9A441)
                else -> Color(0xFFC9A876)
            }
            drawRect(
                color,
                Offset(left + c * map.tileSize * sx, top + r * map.tileSize * sy),
                Size(max(1.5f, map.tileSize * sx), max(1.5f, map.tileSize * sy))
            )
        }
    }
    map.monsters.filter { it.alive }.forEach {
        drawCircle(Color(0xFFC0392B), 2.2f, Offset(left + it.x * sx, top + it.y * sy))
    }
    drawCircle(Color(0xFFF4D35E), 3.2f, Offset(left + heroX * sx, top + heroY * sy))
    drawRoundRect(
        Color(0x66E8D9B8),
        Offset(left - 4f, top - 4f),
        Size(mw + 8f, mh + 8f),
        CornerRadius(8f, 8f),
        style = Stroke(1.5f)
    )
}

private fun DrawScope.drawLabel(text: String, x: Float, y: Float, size: Float, color: Color) {
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.argb(
                (color.alpha * 255).toInt(),
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            textSize = size
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.nativeCanvas.drawText(text, x, y, paint)
    }
}
