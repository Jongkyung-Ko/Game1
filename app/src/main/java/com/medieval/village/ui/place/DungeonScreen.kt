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
import androidx.compose.runtime.LaunchedEffect
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
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawKenneySprite
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawMercenary
import com.medieval.village.ui.village.rememberCustomArtOrNull
import com.medieval.village.ui.village.rememberKenneyAtlasOrNull
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@Composable
fun DungeonScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val atlas = rememberKenneyAtlasOrNull()
    val art = rememberCustomArtOrNull()
    LaunchedEffect(Unit) { vm.ensureDungeonLoaded() }
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
                    "Kenney Tiny Dungeon — 화면을 눌러 이동",
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
                    if (atlas != null) {
                        drawKenneyDungeonFloor(atlas, map)
                    } else {
                        drawDungeonFloorFallback(map)
                    }
                    map.monsters.filter { it.alive }.forEach { monster ->
                        drawDungeonMonster(atlas, art, monster)
                    }
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
                drawLabel("v0.3.3 Kenney dungeon", 14f, 28f, 18f, Color(0xFF5A4231))
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

/** Kenney Tiny Dungeon 타일시트로 층 맵을 그린다. */
private fun DrawScope.drawKenneyDungeonFloor(atlas: KenneyAtlas, map: DungeonFloor) {
    drawRect(Color(0xFF1A1410), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val sheet = atlas.dungeon

    fun isWalk(c: Int, r: Int): Boolean = map.tileAt(c, r) != DungeonTile.WALL

    fun wallTile(c: Int, r: Int): Int {
        val below = isWalk(c, r + 1)
        val above = isWalk(c, r - 1)
        val side = isWalk(c - 1, r) || isWalk(c + 1, r)
        return when {
            below && !above -> DungeonTiles.WALL_TOP
            side && !below -> DungeonTiles.WALL_MID
            (c + r) % 5 == 0 -> DungeonTiles.WALL_WINDOW
            (c * 3 + r) % 7 == 0 -> DungeonTiles.WALL_BRICK
            else -> DungeonTiles.WALL_FILL
        }
    }

    // 1) 바닥 / 벽
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            val cell = map.tileAt(c, r)
            if (cell == DungeonTile.WALL) {
                // 보이는 벽만 타일 (깊은 벽은 배경색)
                val visible = isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r)
                if (visible) {
                    drawKenneyTile(sheet, wallTile(c, r), x, y, ts)
                }
                continue
            }
            val floorId = when {
                (c * 5 + r * 3) % 11 == 0 -> DungeonTiles.FLOOR_STONE
                (c + r) % 2 == 0 -> DungeonTiles.FLOOR
                else -> DungeonTiles.FLOOR_ALT
            }
            drawKenneyTile(sheet, floorId, x, y, ts)
        }
    }

    // 2) 소품 / 계단
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.STAIRS_UP -> drawKenneyTile(sheet, DungeonTiles.LADDER_UP, x, y, ts)
                DungeonTile.STAIRS_DOWN -> drawKenneyTile(sheet, DungeonTiles.LADDER_DOWN, x, y, ts)
                DungeonTile.VAULT -> drawKenneyTile(
                    sheet,
                    if ((c + r) % 2 == 0) DungeonTiles.CHEST else DungeonTiles.CHEST_OPEN,
                    x, y, ts
                )
                DungeonTile.SEWER -> drawKenneyTile(sheet, DungeonTiles.BARREL, x, y, ts)
                DungeonTile.FLOOR -> {
                    when {
                        (c * 13 + r * 7) % 29 == 0 -> drawKenneyTile(sheet, DungeonTiles.PILLAR, x, y, ts)
                        (c + r * 2) % 31 == 0 -> drawKenneyTile(sheet, DungeonTiles.TOMB, x, y, ts)
                        (c * 11 + r * 5) % 37 == 0 -> drawKenneyTile(
                            sheet,
                            if ((c + r) % 2 == 0) DungeonTiles.POTION_R else DungeonTiles.POTION_B,
                            x, y, ts
                        )
                        (c * 3 + r * 17) % 41 == 0 -> drawKenneyTile(sheet, DungeonTiles.DOOR_OPEN, x, y, ts)
                    }
                }
                else -> Unit
            }
        }
    }
}

/** 타일시트 로드 실패 시 최소한의 폴백 */
private fun DrawScope.drawDungeonFloorFallback(map: DungeonFloor) {
    drawRect(Color(0xFFCDB892), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.WALL -> drawRect(Color(0xFF5A544C), Offset(x, y), Size(ts, ts))
                DungeonTile.STAIRS_UP -> drawRect(Color(0xFF8FCF7A), Offset(x, y), Size(ts, ts))
                DungeonTile.STAIRS_DOWN -> drawRect(Color(0xFFC0392B), Offset(x, y), Size(ts, ts))
                DungeonTile.VAULT -> drawRect(Color(0xFFD9A441), Offset(x, y), Size(ts, ts))
                else -> drawRect(
                    if ((c + r) % 2 == 0) Color(0xFFC9A876) else Color(0xFFB8955F),
                    Offset(x, y),
                    Size(ts, ts)
                )
            }
        }
    }
}

private fun DrawScope.drawDungeonMonster(atlas: KenneyAtlas?, art: CustomArt?, monster: DungeonMonster) {
    val x = monster.x
    val y = monster.y
    drawOval(Color(0x55000000), Offset(x - 22f, y - 4f), Size(44f, 14f))

    val kenneyId = when (monster.kind) {
        "shambler", "bloater" -> DungeonTiles.SLIME
        "runner" -> DungeonTiles.BAT
        "armored", "blacksmith" -> DungeonTiles.ORC
        "farmer" -> DungeonTiles.SPIDER
        "golem" -> DungeonTiles.SKELETON
        else -> DungeonTiles.SLIME
    }

    if (atlas != null) {
        drawKenneySprite(atlas.dungeon, kenneyId, x, y, size = 56f)
    } else {
        val sprite = art?.zombieSpriteOrNull(monster.kind)
        if (sprite != null) {
            drawCustomSprite(sprite, x, y, worldHeight = 64f)
        } else {
            val body = Path().apply {
                moveTo(x - 18f, y)
                quadraticBezierTo(x - 20f, y - 34f, x, y - 38f)
                quadraticBezierTo(x + 20f, y - 34f, x + 18f, y)
                close()
            }
            drawPath(body, Color(0xFF6FBF5A))
            drawPath(body, Color(0xFF2F5A28), style = Stroke(3f))
        }
    }
    drawLabel(monster.name, x - 48f, y + 14f, 13f, Color(0xFFE8D9B8))
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
