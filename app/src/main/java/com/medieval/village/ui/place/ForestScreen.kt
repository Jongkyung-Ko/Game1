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
import com.medieval.village.model.DungeonFloor
import com.medieval.village.model.DungeonMonster
import com.medieval.village.model.DungeonTile
import com.medieval.village.model.ForestFactory
import com.medieval.village.model.ItemCatalog
import com.medieval.village.ui.Chip
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawKenneySprite
import com.medieval.village.ui.village.drawKenneySpriteAsset
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawMercenary
import com.medieval.village.ui.village.rememberKenneyAtlasOrNull
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@Composable
fun ForestScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val atlas = rememberKenneyAtlasOrNull()
    LaunchedEffect(Unit) { vm.ensureDungeonLoaded() }
    val floor = vm.dungeonFloor
    val mod = Modifier
    Column(modifier = modifier.fillMaxSize().background(Color(0xFF1A2414))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "동쪽 숲 · ${vm.dungeonFloorNumber}지대",
                    color = Palette.Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "깊을수록 강한 짐승 — 화면을 눌러 이동",
                    color = Palette.ParchmentDim,
                    fontSize = 10.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chip("기록 ${vm.player.forestDepth}지대", Palette.WoodLight)
                Chip("짐승 ${floor?.monsters?.count { it.alive } ?: 0}", Color(0xFF4A7A38))
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color(0xFFC8D9A4), RoundedCornerShape(12.dp))
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
                            val cam = forestCamera(map, vm.dungeonHeroX, vm.dungeonHeroY, widthPx, heightPx)
                            val worldX = tap.x + cam.first
                            val worldY = tap.y + cam.second
                            val beast = map.monsters
                                .filter { it.alive }
                                .minByOrNull { hypot(worldX - it.x, worldY - it.y) }
                                ?.takeIf { hypot(worldX - it.x, worldY - it.y) < ForestFactory.TILE * 0.9f }
                            if (beast != null) {
                                vm.approachDungeonMonster(beast)
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
                drawRect(Color(0xFFDCE8B8), size = size)
                drawRoundRect(
                    Color(0xFF3A5028),
                    Offset(6f, 6f),
                    Size(size.width - 12f, size.height - 12f),
                    CornerRadius(14f, 14f),
                    style = Stroke(5f)
                )

                val map = floor
                if (map == null) {
                    forestLabel("숲길을 찾는 중…", size.width * 0.28f, size.height * 0.5f, 28f, Color(0xFF3A5028))
                    return@Canvas
                }

                val viewW = size.width.coerceAtLeast(1f)
                val viewH = size.height.coerceAtLeast(1f)
                val (camX, camY) = forestCamera(map, heroX, heroY, viewW, viewH)
                withTransform({
                    translate(-camX, -camY)
                }) {
                    if (atlas != null) {
                        drawForestFloor(atlas, map)
                    } else {
                        drawForestFloorFallback(map)
                    }
                    map.monsters.filter { it.alive }.forEach { monster ->
                        drawForestBeast(atlas, monster)
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
                    drawHero(heroX, heroY, facing, walking, walkPhase, scale = 0.78f)
                }
                drawForestMinimap(map, heroX, heroY, viewW, viewH)
                forestLabel("v0.4.3 Eastern forest", 14f, 28f, 18f, Color(0xFF3A5028))
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
                        "stairs_up" -> "↑ 마을 출구 — 아래에서 ‘탈출’"
                        "stairs_down" -> "↓ 더 깊은 숲 — 아래에서 ‘들어가기’"
                        "portal" -> "◎ 집 포털 — 아래에서 ‘집으로’"
                        "chest" -> "◆ 숲속 상자 — 아래에서 ‘열기’ 또는 상자를 탭"
                        "chest_open" -> "이미 열어 본 상자"
                        else -> "화면을 눌러 이동 · 상자를 탭해 열기 · 짐승은 전투"
                    },
                    color = Palette.Parchment,
                    fontSize = 11.sp
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            MessageLog(vm.log, mod.height(78.dp))
            Spacer(mod.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (vm.dungeonHint) {
                    "stairs_up" -> WoodButton("탈출", mod.weight(1f), highlight = true) {
                        vm.escapeDungeon()
                    }
                    "stairs_down" -> WoodButton("들어가기", mod.weight(1f), highlight = true) {
                        vm.descendDungeon()
                    }
                    "portal" -> WoodButton("집으로", mod.weight(1f), highlight = true) {
                        vm.enterHomePortal()
                    }
                    "chest" -> WoodButton("열기", mod.weight(1f), highlight = true) {
                        vm.openDungeonChest()
                    }
                    else -> WoodButton("탈출", mod.weight(1f)) { vm.escapeDungeon() }
                }
                val portalStone = vm.inventory.toList().firstOrNull {
                    it.item.id == ItemCatalog.portalStone.id && it.count > 0
                }
                val potion = vm.inventory.toList().firstOrNull { it.item.healHp > 0 }
                if (portalStone != null) {
                    WoodButton("포털스톤", mod.weight(1f), highlight = true) {
                        vm.useItem(ItemCatalog.portalStone)
                    }
                }
                WoodButton(
                    text = if (potion != null) "물약" else "물약 없음",
                    modifier = mod.weight(1f),
                    enabled = potion != null
                ) {
                    potion?.let { vm.useItem(it.item) }
                }
            }
        }
    }
}

private fun forestCamera(
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

private fun DrawScope.drawForestFloor(atlas: KenneyAtlas, map: DungeonFloor) {
    drawRect(Color(0xFF1E2E18), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val town = atlas.town
    val dungeon = atlas.dungeon
    val cr = CornerRadius(ts * 0.14f, ts * 0.14f)

    fun isWalk(c: Int, r: Int) = map.tileAt(c, r) != DungeonTile.WALL

    // 풀밭 / 오솔길
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            val x = c * ts
            val y = r * ts
            val even = (c + r) % 2 == 0
            val grass = if (even) Color(0xFF6FA84E) else Color(0xFF5E943F)
            drawRoundRect(grass, Offset(x + 1f, y + 1f), Size(ts - 2f, ts - 2f), cr)
            if ((c * 7 + r * 3) % 5 == 0) {
                drawKenneyTile(town, TownTiles.GRASS_TUFT, x, y, ts)
            } else if ((c + r) % 7 == 0) {
                drawKenneyTile(town, TownTiles.GRASS_FLOWER, x, y, ts)
            } else if (map.tileAt(c, r) == DungeonTile.FLOOR && (c * 5 + r) % 4 == 0) {
                drawKenneyTile(town, TownTiles.PATH, x, y, ts * 0.92f)
            }
        }
    }

    // 수풀(WALL) — 나무 덩어리
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) != DungeonTile.WALL) continue
            val visible = isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r)
            if (!visible) continue
            val x = c * ts
            val y = r * ts
            val canopy = if ((c + r) % 2 == 0) Color(0xFF2F5A28) else Color(0xFF3A6A30)
            drawCircle(canopy, ts * 0.42f, Offset(x + ts * 0.5f, y + ts * 0.42f))
            drawCircle(Color(0xFF244820), ts * 0.28f, Offset(x + ts * 0.35f, y + ts * 0.55f))
            drawRoundRect(Color(0xFF5A3A22), Offset(x + ts * 0.42f, y + ts * 0.55f), Size(ts * 0.16f, ts * 0.35f), CornerRadius(3f))
            // Kenney 나무 스프라이트 가끔
            if ((c * 11 + r * 5) % 3 == 0) {
                val tree = atlas.spriteOrNull(if ((c + r) % 2 == 0) "tree_g" else "tree_o")
                if (tree != null) {
                    drawKenneySpriteAsset(tree, x + ts * 0.5f, y + ts * 0.95f, ts * 1.15f)
                }
            }
        }
    }

    // 특수 타일
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.STAIRS_UP -> {
                    drawRoundRect(Color(0xFF8FCF7A), Offset(x + 6f, y + 6f), Size(ts - 12f, ts - 12f), cr)
                    forestLabel("마을", x + 10f, y + ts * 0.55f, 14f, Color(0xFF1E2E18))
                }
                DungeonTile.STAIRS_DOWN -> {
                    drawRoundRect(Color(0xFF8A5A32), Offset(x + 6f, y + 6f), Size(ts - 12f, ts - 12f), cr)
                    forestLabel("깊은숲", x + 4f, y + ts * 0.55f, 13f, Color(0xFFF3E4C5))
                }
                DungeonTile.PORTAL -> {
                    drawCircle(Color(0xAA3AB0E0), ts * 0.35f, Offset(x + ts * 0.5f, y + ts * 0.5f))
                    drawCircle(Color(0xCC8FE8FF), ts * 0.18f, Offset(x + ts * 0.5f, y + ts * 0.5f))
                }
                DungeonTile.VAULT -> drawKenneyTile(dungeon, DungeonTiles.CHEST, x, y, ts)
                DungeonTile.CHEST_OPEN -> drawKenneyTile(dungeon, DungeonTiles.CHEST_OPEN, x, y, ts)
                DungeonTile.SEWER -> {
                    // 덤불
                    drawCircle(Color(0xFF4A7A38), ts * 0.22f, Offset(x + ts * 0.35f, y + ts * 0.55f))
                    drawCircle(Color(0xFF3A6A30), ts * 0.18f, Offset(x + ts * 0.62f, y + ts * 0.50f))
                    val bush = atlas.spriteOrNull("bush")
                    if (bush != null && (c + r) % 2 == 0) {
                        drawKenneySpriteAsset(bush, x + ts * 0.5f, y + ts * 0.85f, ts * 0.7f)
                    }
                }
                else -> Unit
            }
        }
    }
}

private fun DrawScope.drawForestFloorFallback(map: DungeonFloor) {
    drawRect(Color(0xFF1E2E18), size = Size(map.worldW, map.worldH))
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
                        drawCircle(Color(0xFF2F5A28), ts * 0.4f, Offset(x + ts * 0.5f, y + ts * 0.45f))
                    }
                }
                DungeonTile.STAIRS_UP -> drawRoundRect(Color(0xFF8FCF7A), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.STAIRS_DOWN -> drawRoundRect(Color(0xFF8A5A32), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.PORTAL -> drawCircle(Color(0xAA3AB0E0), ts * 0.3f, Offset(x + ts * 0.5f, y + ts * 0.5f))
                DungeonTile.VAULT -> drawRoundRect(Color(0xFFD9A441), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.CHEST_OPEN -> drawRoundRect(Color(0xFF8A7040), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                DungeonTile.SEWER -> drawRoundRect(Color(0xFF4A7A38), Offset(x + 2f, y + 2f), Size(ts - 4f, ts - 4f), cr)
                else -> drawRoundRect(
                    if ((c + r) % 2 == 0) Color(0xFF6FA84E) else Color(0xFF5E943F),
                    Offset(x + 1f, y + 1f),
                    Size(ts - 2f, ts - 2f),
                    cr,
                )
            }
        }
    }
}

private fun DrawScope.drawForestBeast(atlas: KenneyAtlas?, monster: DungeonMonster) {
    val x = monster.x
    val y = monster.y
    drawOval(Color(0x55000000), Offset(x - 20f, y - 4f), Size(40f, 12f))

    val critter = when (monster.kind) {
        "rabbit" -> "critter_a"
        "fox", "owl" -> "critter_b"
        "deer", "stag" -> "critter_c"
        else -> null
    }
    val kenneyEnemy = when (monster.kind) {
        "wolf", "dire_wolf" -> DungeonTiles.BAT
        "boar", "giant_boar", "bear" -> DungeonTiles.ORC
        "snake" -> DungeonTiles.SLIME
        "forest_spider" -> DungeonTiles.SPIDER
        else -> null
    }

    var drawn = false
    if (atlas != null && critter != null) {
        val sprite = atlas.spriteOrNull(critter)
        if (sprite != null) {
            drawKenneySpriteAsset(sprite, x, y, 52f)
            drawn = true
        }
    }
    if (!drawn && atlas != null && kenneyEnemy != null) {
        drawKenneySprite(atlas.dungeon, kenneyEnemy, x, y, size = 54f)
        drawn = true
    }
    if (!drawn) {
        drawAnimalFallback(monster.kind, x, y)
    }
    forestLabel(monster.name, x - 36f, y + 14f, 13f, Color(0xFFF3E4C5))
}

private fun DrawScope.drawAnimalFallback(kind: String, x: Float, y: Float) {
    val body = when (kind) {
        "bear", "giant_boar" -> Color(0xFF5A3A22)
        "wolf", "dire_wolf" -> Color(0xFF6A6A72)
        "fox" -> Color(0xFFC06A2A)
        "deer", "stag" -> Color(0xFFB08A5A)
        "rabbit" -> Color(0xFFE8D9B8)
        "snake" -> Color(0xFF6FBF5A)
        else -> Color(0xFF8A6A3A)
    }
    val path = Path().apply {
        moveTo(x - 16f, y)
        quadraticBezierTo(x - 18f, y - 28f, x, y - 32f)
        quadraticBezierTo(x + 18f, y - 28f, x + 16f, y)
        close()
    }
    drawPath(path, body)
    drawPath(path, Color(0xFF2A1A10), style = Stroke(2.5f))
    drawCircle(Color(0xFF1A1210), 2.5f, Offset(x - 5f, y - 22f))
    drawCircle(Color(0xFF1A1210), 2.5f, Offset(x + 5f, y - 22f))
}

private fun DrawScope.drawForestMinimap(
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
    drawRoundRect(Color(0xAA0D1409), Offset(left - 4f, top - 4f), Size(mw + 8f, mh + 8f), CornerRadius(8f, 8f))
    val sx = mw / map.worldW
    val sy = mh / map.worldH
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val tile = map.tileAt(c, r)
            if (tile == DungeonTile.WALL) continue
            val color = when (tile) {
                DungeonTile.STAIRS_UP -> Color(0xFF8FCF7A)
                DungeonTile.STAIRS_DOWN -> Color(0xFF8A5A32)
                DungeonTile.PORTAL -> Color(0xFF55C8E8)
                DungeonTile.SEWER -> Color(0xFF4A7A38)
                DungeonTile.VAULT -> Color(0xFFD9A441)
                DungeonTile.CHEST_OPEN -> Color(0xFF8A7040)
                else -> Color(0xFF6FA84E)
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
}

private fun DrawScope.forestLabel(text: String, x: Float, y: Float, size: Float, color: Color) {
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
