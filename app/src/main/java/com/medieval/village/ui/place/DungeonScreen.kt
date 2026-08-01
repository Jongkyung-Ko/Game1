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
                    "좀비 둥지 · 지하 ${vm.dungeonFloorNumber}층",
                    color = Palette.Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "오염된 하수도·지하 보관소",
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
                .background(Color(0xFF0B0907), RoundedCornerShape(12.dp))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val facing = vm.facing
            val walking = vm.dungeonWalking
            val walkPhase = vm.walkPhase

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
                val map = floor ?: return@Canvas
                val (camX, camY) = cameraOffset(map, vm.dungeonHeroX, vm.dungeonHeroY, size.width, size.height)
                withTransform({
                    translate(-camX, -camY)
                }) {
                    drawDungeonFloor(map)
                    map.monsters.filter { it.alive }.forEach { drawZombie(art, it) }
                    vm.activeParty.forEachIndexed { index, mercenary ->
                        drawMercenary(
                            mercenary = mercenary,
                            x = vm.dungeonHeroX + if (index == 0) -40f else 40f,
                            y = vm.dungeonHeroY + 28f + index * 6f,
                            facing = facing,
                            walking = walking,
                            phase = walkPhase + index * 0.7f,
                            scale = 0.72f,
                        )
                    }
                    drawHero(
                        vm.dungeonHeroX,
                        vm.dungeonHeroY,
                        facing,
                        walking,
                        walkPhase,
                        scale = 0.78f,
                    )
                }
                drawMinimap(map, vm.dungeonHeroX, vm.dungeonHeroY, size.width, size.height)
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
    drawRect(Color(0xFF0A0806), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val tile = map.tileAt(c, r)
            val x = c * ts
            val y = r * ts
            when (tile) {
                DungeonTile.WALL -> {
                    drawRect(Color(0xFF2A2420), Offset(x, y), Size(ts, ts))
                    drawRect(Color(0xFF3A322C), Offset(x + 3f, y + 3f), Size(ts - 6f, ts - 6f))
                }
                DungeonTile.FLOOR -> {
                    drawRect(if ((c + r) % 2 == 0) Color(0xFF3E342C) else Color(0xFF372E27), Offset(x, y), Size(ts, ts))
                }
                DungeonTile.SEWER -> {
                    drawRect(Color(0xFF2F3A30), Offset(x, y), Size(ts, ts))
                    drawRect(Color(0xFF1C2820), Offset(x + 18f, y), Size(ts - 36f, ts))
                    drawCircle(Color(0x4426A86A), 10f, Offset(x + ts / 2f, y + ts * 0.7f))
                }
                DungeonTile.VAULT -> {
                    drawRect(Color(0xFF4A3A28), Offset(x, y), Size(ts, ts))
                    drawRect(Color(0xFF6B4E2E), Offset(x + 10f, y + 10f), Size(ts - 20f, ts - 20f))
                    drawCircle(Color(0x88C0392B), 8f, Offset(x + ts / 2f, y + ts / 2f))
                }
                DungeonTile.STAIRS_UP -> {
                    drawRect(Color(0xFF3E342C), Offset(x, y), Size(ts, ts))
                    for (i in 0..3) {
                        drawRect(
                            Color(0xFF8B7355),
                            Offset(x + 10f + i * 4f, y + 12f + i * 10f),
                            Size(ts - 20f - i * 8f, 8f)
                        )
                    }
                    drawLabel("↑출구", x + 8f, y + 14f, 16f, Color(0xFFE8D9B8))
                }
                DungeonTile.STAIRS_DOWN -> {
                    drawRect(Color(0xFF2A221C), Offset(x, y), Size(ts, ts))
                    for (i in 0..3) {
                        drawRect(
                            Color(0xFF5C4030),
                            Offset(x + 8f + i * 3f, y + 10f + i * 11f),
                            Size(ts - 16f - i * 6f, 8f)
                        )
                    }
                    drawLabel("↓심층", x + 8f, y + 14f, 16f, Color(0xFFC0392B))
                }
            }
            // 횃불 느낌의 포인트
            if (tile != DungeonTile.WALL && (c * 17 + r * 31) % 23 == 0) {
                drawCircle(Color(0x33E8843A), 18f, Offset(x + ts / 2f, y + 10f))
                drawCircle(Color(0xFFE8843A), 5f, Offset(x + ts / 2f, y + 10f))
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
                DungeonTile.SEWER -> Color(0xFF3D6B4F)
                DungeonTile.VAULT -> Color(0xFFB8860B)
                else -> Color(0xFF5A4A3A)
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
