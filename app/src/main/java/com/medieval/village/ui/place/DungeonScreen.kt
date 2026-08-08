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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
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
import com.medieval.village.model.ItemCatalog
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
                    "하이브리드 던전 — 화면을 눌러 이동",
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
                                return@detectTapGestures
                            }
                            val col = (worldX / map.tileSize).toInt()
                            val row = (worldY / map.tileSize).toInt()
                            if (map.tileAt(col, row) == DungeonTile.VAULT) {
                                vm.openDungeonChest(col, row)
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
                        drawHybridDungeonFloor(atlas, map)
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
                drawLabel("v0.4.0 Hybrid dungeon", 14f, 28f, 18f, Color(0xFF5A4231))
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
                        "portal" -> "◎ 집 포털 — 아래에서 ‘집으로’"
                        "chest" -> "◆ 보물상자 — 아래에서 ‘열기’ 또는 상자를 탭"
                        "chest_open" -> "이미 열어 본 보물상자"
                        else -> "화면을 눌러 이동 · 상자를 탭해 열기 · 좀비는 전투"
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
                    "portal" -> WoodButton("집으로", Modifier.weight(1f), highlight = true) {
                        vm.enterHomePortal()
                    }
                    "chest" -> WoodButton("열기", Modifier.weight(1f), highlight = true) {
                        vm.openDungeonChest()
                    }
                    else -> WoodButton("탈출", Modifier.weight(1f)) { vm.escapeDungeon() }
                }
                val portalStone = vm.inventory.toList().firstOrNull {
                    it.item.id == ItemCatalog.portalStone.id && it.count > 0
                }
                val potion = vm.inventory.toList().firstOrNull { it.item.healHp > 0 }
                if (portalStone != null) {
                    WoodButton("포털스톤", Modifier.weight(1f), highlight = true) {
                        vm.useItem(ItemCatalog.portalStone)
                    }
                }
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

/**
 * Style C — 하이브리드 던전.
 * 페인티드 바닥/벽 + Kenney 소품·계단 + 횃불 글로우.
 */
private fun DrawScope.drawHybridDungeonFloor(atlas: KenneyAtlas, map: DungeonFloor) {
    drawRect(Color(0xFF0E0A08), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val sheet = atlas.dungeon
    val floorCr = CornerRadius(ts * 0.12f, ts * 0.12f)
    val wallCr = CornerRadius(ts * 0.20f, ts * 0.20f)

    fun isWalk(c: Int, r: Int): Boolean = map.tileAt(c, r) != DungeonTile.WALL

    fun rgb(r: Int, g: Int, b: Int, a: Int = 255): Color =
        Color(
            red = r.coerceIn(0, 255) / 255f,
            green = g.coerceIn(0, 255) / 255f,
            blue = b.coerceIn(0, 255) / 255f,
            alpha = a.coerceIn(0, 255) / 255f,
        )

    // 1) 페인티드 바닥 (베벨 돌판)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            val x = c * ts
            val y = r * ts
            val even = (c + r) % 2 == 0
            val n = ((c * 17 + r * 31) % 17) - 8
            val baseR = (if (even) 205 else 186) + n
            val baseG = (if (even) 170 else 150) + n
            val baseB = (if (even) 118 else 96) + n / 2
            drawRoundRect(
                rgb(baseR, baseG, baseB),
                Offset(x + 1f, y + 1f),
                Size(ts - 2f, ts - 2f),
                floorCr,
            )
            // 하단 살짝 그림자
            drawRoundRect(
                rgb(40, 14, 8, 45),
                Offset(x + 4f, y + ts * 0.72f),
                Size(ts - 8f, ts * 0.18f),
                CornerRadius(4f, 4f),
            )
            // 윗면 하이라이트
            drawRoundRect(
                rgb(255, 230, 180, 28),
                Offset(x + 5f, y + 4f),
                Size(ts - 10f, ts * 0.18f),
                CornerRadius(3f, 3f),
            )
        }
    }

    // 2) 페인티드 벽 (돌 블록)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) != DungeonTile.WALL) continue
            val visible = isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r)
            if (!visible) continue
            val x = c * ts
            val y = r * ts
            val tone = when {
                (c + r) % 5 == 0 -> 98
                (c * 3 + r) % 7 == 0 -> 82
                else -> 88
            }
            drawRoundRect(
                rgb(tone, tone - 6, tone - 14),
                Offset(x + 1f, y + 1f),
                Size(ts - 2f, ts - 2f),
                wallCr,
            )
            drawRoundRect(
                rgb(25, 20, 16),
                Offset(x + 1f, y + 1f),
                Size(ts - 2f, ts - 2f),
                wallCr,
                style = Stroke(2.2f),
            )
            // 돌 질감 점
            drawOval(
                rgb(130, 122, 112, 150),
                Offset(x + ts * 0.18f, y + ts * 0.16f),
                Size(ts * 0.22f, ts * 0.14f),
            )
            drawOval(
                rgb(50, 46, 42, 140),
                Offset(x + ts * 0.52f, y + ts * 0.48f),
                Size(ts * 0.24f, ts * 0.16f),
            )
        }
    }

    // 3) 바닥–벽 경계 아웃라인
    val edge = Color(0xE61C1814)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (!isWalk(c, r)) continue
            val x = c * ts
            val y = r * ts
            if (!isWalk(c, r - 1)) {
                drawLine(edge, Offset(x + 2f, y + 2f), Offset(x + ts - 2f, y + 2f), strokeWidth = 3f)
            }
            if (!isWalk(c, r + 1)) {
                drawLine(edge, Offset(x + 2f, y + ts - 2f), Offset(x + ts - 2f, y + ts - 2f), strokeWidth = 3f)
            }
            if (!isWalk(c - 1, r)) {
                drawLine(edge, Offset(x + 2f, y + 2f), Offset(x + 2f, y + ts - 2f), strokeWidth = 3f)
            }
            if (!isWalk(c + 1, r)) {
                drawLine(edge, Offset(x + ts - 2f, y + 2f), Offset(x + ts - 2f, y + ts - 2f), strokeWidth = 3f)
            }
        }
    }

    // 4) Kenney 소품 / 계단
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.STAIRS_UP -> drawKenneyTile(sheet, DungeonTiles.LADDER_UP, x, y, ts)
                DungeonTile.STAIRS_DOWN -> drawKenneyTile(sheet, DungeonTiles.LADDER_DOWN, x, y, ts)
                DungeonTile.PORTAL -> drawHomePortalTile(x, y, ts)
                DungeonTile.VAULT -> drawKenneyTile(sheet, DungeonTiles.CHEST, x, y, ts)
                DungeonTile.CHEST_OPEN -> drawKenneyTile(sheet, DungeonTiles.CHEST_OPEN, x, y, ts)
                DungeonTile.SEWER -> drawKenneyTile(sheet, DungeonTiles.BARREL, x, y, ts)
                DungeonTile.FLOOR -> {
                    when {
                        (c * 13 + r * 7) % 29 == 0 -> drawKenneyTile(sheet, DungeonTiles.PILLAR, x, y, ts)
                        (c + r * 2) % 31 == 0 -> drawKenneyTile(sheet, DungeonTiles.TOMB, x, y, ts)
                        (c * 11 + r * 5) % 37 == 0 -> drawKenneyTile(
                            sheet,
                            if ((c + r) % 2 == 0) DungeonTiles.POTION_R else DungeonTiles.POTION_B,
                            x, y, ts,
                        )
                        (c * 3 + r * 17) % 41 == 0 -> drawKenneyTile(sheet, DungeonTiles.DOOR_OPEN, x, y, ts)
                    }
                }
                else -> Unit
            }
        }
    }

    // 5) 횃불 + 분위기 조명
    val torchSpots = ArrayList<Offset>(24)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (!isWalk(c, r)) continue
            val nearWall = !isWalk(c, r - 1) || !isWalk(c, r + 1) || !isWalk(c - 1, r) || !isWalk(c + 1, r)
            if (!nearWall || (c * 17 + r * 9) % 16 != 0) continue
            val cx = c * ts + ts * 0.5f
            val cy = r * ts + ts * 0.32f
            torchSpots += Offset(cx, cy)
            // 횃불 본체
            drawRect(rgb(90, 58, 34), Offset(cx - 2.5f, cy), Size(5f, ts * 0.28f))
            drawOval(rgb(232, 132, 58), Offset(cx - 7f, cy - 12f), Size(14f, 14f))
            drawOval(rgb(249, 222, 133), Offset(cx - 4f, cy - 14f), Size(8f, 10f))
            if (torchSpots.size >= 18) break
        }
        if (torchSpots.size >= 18) break
    }

    // 전체 살짝 어둡게
    drawRect(rgb(8, 4, 2, 72), size = Size(map.worldW, map.worldH))

    // 횃불 방사광
    val glowR = ts * 3.2f
    for (spot in torchSpots) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x66E8843A),
                    Color(0x33E8843A),
                    Color(0x00E8843A),
                ),
                center = spot,
                radius = glowR,
            ),
            radius = glowR,
            center = spot,
            blendMode = BlendMode.Screen,
        )
    }

    // 집 포털 추가 글로우
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) != DungeonTile.PORTAL) continue
            val center = Offset(c * ts + ts * 0.5f, r * ts + ts * 0.5f)
            val pr = ts * 2.4f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x8855C8E8),
                        Color(0x4455A0E8),
                        Color(0x0055A0E8),
                    ),
                    center = center,
                    radius = pr,
                ),
                radius = pr,
                center = center,
                blendMode = BlendMode.Screen,
            )
        }
    }
}

/** 집으로 이어지는 일시 포털 비주얼 */
private fun DrawScope.drawHomePortalTile(x: Float, y: Float, ts: Float) {
    val cx = x + ts * 0.5f
    val cy = y + ts * 0.5f
    drawOval(
        Color(0x55203860),
        Offset(x + ts * 0.12f, y + ts * 0.18f),
        Size(ts * 0.76f, ts * 0.64f),
    )
    drawOval(
        Color(0xAA3AB0E0),
        Offset(x + ts * 0.22f, y + ts * 0.26f),
        Size(ts * 0.56f, ts * 0.48f),
        style = Stroke(3.5f),
    )
    drawOval(
        Color(0xCC8FE8FF),
        Offset(x + ts * 0.32f, y + ts * 0.34f),
        Size(ts * 0.36f, ts * 0.32f),
    )
    drawCircle(Color(0xEEF4FFFF), radius = ts * 0.08f, center = Offset(cx, cy))
}

/** 타일시트 로드 실패 시 — 페인티드 바닥만이라도 보이게 */
private fun DrawScope.drawDungeonFloorFallback(map: DungeonFloor) {
    drawRect(Color(0xFF0E0A08), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val cr = CornerRadius(ts * 0.12f, ts * 0.12f)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.WALL -> {
                    val visible = (c > 0 && map.tileAt(c - 1, r) != DungeonTile.WALL) ||
                        (c < map.cols - 1 && map.tileAt(c + 1, r) != DungeonTile.WALL) ||
                        (r > 0 && map.tileAt(c, r - 1) != DungeonTile.WALL) ||
                        (r < map.rows - 1 && map.tileAt(c, r + 1) != DungeonTile.WALL)
                    if (visible) {
                        drawRoundRect(Color(0xFF58524A), Offset(x + 1f, y + 1f), Size(ts - 2f, ts - 2f), cr)
                    }
                }
                DungeonTile.STAIRS_UP -> drawRoundRect(Color(0xFF8FCF7A), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.STAIRS_DOWN -> drawRoundRect(Color(0xFFC0392B), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.PORTAL -> drawHomePortalTile(x, y, ts)
                DungeonTile.VAULT -> drawRoundRect(Color(0xFFD9A441), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.CHEST_OPEN -> drawRoundRect(Color(0xFF8A7040), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                else -> drawRoundRect(
                    if ((c + r) % 2 == 0) Color(0xFFCDAA76) else Color(0xFFBA965F),
                    Offset(x + 1f, y + 1f),
                    Size(ts - 2f, ts - 2f),
                    cr,
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
                DungeonTile.PORTAL -> Color(0xFF55C8E8)
                DungeonTile.SEWER -> Color(0xFF6F9A54)
                DungeonTile.VAULT -> Color(0xFFD9A441)
                DungeonTile.CHEST_OPEN -> Color(0xFF8A7040)
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
