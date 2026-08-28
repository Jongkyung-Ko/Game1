package com.medieval.village.ui.place

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.DungeonFloor
import com.medieval.village.model.DungeonMonster
import com.medieval.village.model.DungeonTile
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.mapZoomGestures
import com.medieval.village.ui.rememberMapZoomState
import com.medieval.village.ui.skin.DungeonArt
import com.medieval.village.ui.skin.rememberDungeonArt
import com.medieval.village.ui.withMapZoom
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.PARTY_REAR_SCALE_FACTOR
import com.medieval.village.ui.village.drawLevelUpBurst
import com.medieval.village.ui.village.drawPartySlots
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawKenneySprite
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.rememberCustomArtOrNull
import com.medieval.village.ui.village.rememberKenneyAtlasOrNull
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun DungeonScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val atlas = rememberKenneyAtlasOrNull()
    val art = rememberCustomArtOrNull()
    val dungeonArt = rememberDungeonArt()
    LaunchedEffect(Unit) { vm.ensureDungeonLoaded() }
    val floor = vm.dungeonFloor
    // 던전 이름·층·잔여 몬스터는 TopMenuBar 고정 영역에 표시 (맵과 분리)
    Column(modifier = modifier.fillMaxSize().background(Color(0xFF14100C))) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD9C8A4), RoundedCornerShape(12.dp))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val walking = vm.dungeonWalking
            val walkPhase = vm.walkPhase
            val heroX = vm.dungeonHeroX
            val heroY = vm.dungeonHeroY
            val slashFx = vm.meleeSlashFx
            val specialFx = vm.specialSkillFx.toList()
            val projectiles = vm.dungeonProjectiles.toList()
            val combatFrame = vm.dungeonCombatFrame
            val heroAnimKind = vm.heroAnimKind
            val heroAnimFrame = vm.heroAnimFrame
            val specialAnimSet = vm.specialAnimSet
            val partySlots = vm.partyDrawSlots(heroX, heroY)
            val mapZoom = rememberMapZoomState()
            val viewSize = Size(widthPx, heightPx)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(floor?.floor, widthPx, heightPx, mapZoom.zoom, mapZoom.pan) {
                        detectTapGestures { tap ->
                            val map = vm.dungeonFloor ?: return@detectTapGestures
                            val content = mapZoom.screenToContent(tap, viewSize)
                            val cam = cameraOffset(map, vm.dungeonHeroX, vm.dungeonHeroY, widthPx, heightPx)
                            val worldX = content.x + cam.first
                            val worldY = content.y + cam.second
                            val col = (worldX / map.tileSize).toInt()
                            val row = (worldY / map.tileSize).toInt()
                            if (map.tileAt(col, row) == DungeonTile.VAULT) {
                                vm.openDungeonChest(col, row)
                            }
                        }
                    }
                    .mapZoomGestures(mapZoom)
            ) {
                @Suppress("UNUSED_EXPRESSION")
                combatFrame

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
                withMapZoom(mapZoom) {
                    withTransform({
                        translate(-camX, -camY)
                    }) {
                        if (dungeonArt != null) {
                            drawArtDungeonFloor(dungeonArt, map, camX, camY, viewW, viewH)
                        } else if (atlas != null) {
                            drawHybridDungeonFloor(atlas, map)
                        } else {
                            drawDungeonFloorFallback(map)
                        }
                        map.monsters.filter { it.alive }.forEach { monster ->
                            drawDungeonMonster(atlas, art, monster)
                        }
                        projectiles.forEach { drawDungeonProjectile(it, art) }
                        drawPartySlots(
                            slots = partySlots,
                            walking = walking,
                            walkPhase = walkPhase,
                            frontAnimKind = heroAnimKind,
                            frontAnimFrame = heroAnimFrame,
                            art = art,
                            scale = 0.88f,
                            rearScaleFactor = PARTY_REAR_SCALE_FACTOR,
                            specialAnimSet = specialAnimSet,
                        )
                        val fxKey = vm.levelUpFxActorKey
                        if (fxKey != null) {
                            val slot = partySlots.firstOrNull { it.actorKey == fxKey }
                                ?: partySlots.firstOrNull()
                            if (slot != null) {
                                val rem = (vm.levelUpFxUntil - vm.animTime).coerceAtLeast(0f)
                                val progress = (1f - rem / 2f).coerceIn(0f, 1f)
                                drawLevelUpBurst(slot.x, slot.y, progress, vm.animTime)
                            }
                        }
                        slashFx?.let { drawMeleeSlashFx(it) }
                        specialFx.forEach { drawSpecialSkillFx(it, art) }
                    }
                }
                drawMinimap(map, heroX, heroY, viewW, viewH)
                drawLabel(
                    if (vm.currentPlace == PlaceId.GRAY_CASTLE) {
                        "v0.4.41 Gray Castle"
                    } else {
                        "v0.4.41 Undead nest"
                    },
                    14f,
                    28f,
                    18f,
                    Color(0xFF5A4231),
                )
            }
        }

        DungeonBottomChrome(
            vm = vm,
            logContent = {
                MessageLog(vm.log, Modifier.fillMaxWidth().height(72.dp))
            },
        )
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
 * 도안에서 잘라낸 석조 타일로 그리는 던전.
 * 화면 밖 칸은 건너뛰고, 횃불 불빛과 비네트는 기존 연출을 유지한다.
 */
private fun DrawScope.drawArtDungeonFloor(
    art: DungeonArt,
    map: DungeonFloor,
    camX: Float,
    camY: Float,
    viewW: Float,
    viewH: Float,
) {
    val ts = map.tileSize
    drawRect(Color(0xFF0E0A08), size = Size(map.worldW, map.worldH))

    fun isWalk(c: Int, r: Int): Boolean = map.tileAt(c, r) != DungeonTile.WALL

    // 카메라에 걸치는 칸만 그린다 (여유 2칸)
    val c0 = ((camX / ts).toInt() - 2).coerceAtLeast(0)
    val r0 = ((camY / ts).toInt() - 2).coerceAtLeast(0)
    val c1 = (((camX + viewW) / ts).toInt() + 2).coerceAtMost(map.cols - 1)
    val r1 = (((camY + viewH) / ts).toInt() + 2).coerceAtMost(map.rows - 1)

    fun tile(image: ImageBitmap, x: Float, y: Float) {
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
            // 칸 사이 이음새가 보이지 않도록 1px 겹쳐 깐다
            dstSize = IntSize(ts.roundToInt() + 1, ts.roundToInt() + 1),
            filterQuality = FilterQuality.Medium,
        )
    }

    fun prop(image: ImageBitmap, x: Float, y: Float, fill: Float) {
        val budget = ts * fill
        val scale = min(budget / image.width, budget / image.height)
        val w = (image.width * scale).roundToInt().coerceAtLeast(1)
        val h = (image.height * scale).roundToInt().coerceAtLeast(1)
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(
                (x + (ts - w) / 2f).roundToInt(),
                (y + (ts - h) / 2f).roundToInt(),
            ),
            dstSize = IntSize(w, h),
            filterQuality = FilterQuality.Medium,
        )
    }

    // 1) 바닥
    for (r in r0..r1) {
        for (c in c0..c1) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            tile(art.floorFor(c, r), c * ts, r * ts)
        }
    }

    // 2) 벽 — 바닥과 맞닿은 면만
    for (r in r0..r1) {
        for (c in c0..c1) {
            if (map.tileAt(c, r) != DungeonTile.WALL) continue
            if (!(isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r))) continue
            tile(art.wall, c * ts, r * ts)
        }
    }

    // 3) 벽 밑동 그림자 — 바닥에 닿는 곳을 어둡게
    for (r in r0..r1) {
        for (c in c0..c1) {
            if (!isWalk(c, r)) continue
            val x = c * ts
            val y = r * ts
            if (!isWalk(c, r - 1)) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x73000000), Color(0x00000000)),
                        startY = y,
                        endY = y + ts * 0.5f,
                    ),
                    topLeft = Offset(x, y),
                    size = Size(ts, ts * 0.5f),
                )
            }
            if (!isWalk(c - 1, r)) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0x59000000), Color(0x00000000)),
                        startX = x,
                        endX = x + ts * 0.3f,
                    ),
                    topLeft = Offset(x, y),
                    size = Size(ts * 0.3f, ts),
                )
            }
            if (!isWalk(c + 1, r)) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0x00000000), Color(0x59000000)),
                        startX = x + ts * 0.7f,
                        endX = x + ts,
                    ),
                    topLeft = Offset(x + ts * 0.7f, y),
                    size = Size(ts * 0.3f, ts),
                )
            }
        }
    }

    // 4) 소품
    for (r in r0..r1) {
        for (c in c0..c1) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.STAIRS_UP -> art.stairsUp?.let { prop(it, x, y, 1.02f) }
                DungeonTile.STAIRS_DOWN -> art.stairsDown?.let { prop(it, x, y, 1.02f) }
                DungeonTile.VAULT -> art.chestClosed?.let { prop(it, x, y, 0.86f) }
                DungeonTile.CHEST_OPEN -> art.chestOpen?.let { prop(it, x, y, 0.86f) }
                DungeonTile.PORTAL -> art.portal?.let { prop(it, x, y, 1.15f) }
                DungeonTile.SEWER -> art.sewerGrate?.let { prop(it, x, y, 0.78f) }
                else -> Unit
            }
        }
    }

    // 5) 벽에 걸린 횃불
    val torchSpots = ArrayList<Offset>(24)
    for (r in r0..r1) {
        for (c in c0..c1) {
            if (!isWalk(c, r)) continue
            val nearWall = !isWalk(c, r - 1) || !isWalk(c, r + 1) || !isWalk(c - 1, r) || !isWalk(c + 1, r)
            if (!nearWall || (c * 17 + r * 9) % 16 != 0) continue
            val cx = c * ts + ts * 0.5f
            val cy = r * ts + ts * 0.32f
            torchSpots += Offset(cx, cy)
            drawRect(Color(0xFF5A3A22), Offset(cx - 2.5f, cy), Size(5f, ts * 0.28f))
            drawOval(Color(0xFFE8843A), Offset(cx - 7f, cy - 12f), Size(14f, 14f))
            drawOval(Color(0xFFF9DE85), Offset(cx - 4f, cy - 14f), Size(8f, 10f))
            if (torchSpots.size >= 18) break
        }
        if (torchSpots.size >= 18) break
    }

    // 6) 전체 감광 + 횃불 방사광
    drawRect(Color(0x30080402), size = Size(map.worldW, map.worldH))
    val glowR = ts * 3.2f
    for (spot in torchSpots) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66E8843A), Color(0x33E8843A), Color(0x00E8843A)),
                center = spot,
                radius = glowR,
            ),
            radius = glowR,
            center = spot,
            blendMode = BlendMode.Screen,
        )
    }
    for (r in r0..r1) {
        for (c in c0..c1) {
            if (map.tileAt(c, r) != DungeonTile.PORTAL) continue
            val center = Offset(c * ts + ts * 0.5f, r * ts + ts * 0.5f)
            val pr = ts * 2.4f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x8855C8E8), Color(0x4455A0E8), Color(0x0055A0E8)),
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
    val boss = monster.isBoss
    val bodyH = if (boss) 118f else 68f
    val shadowW = if (boss) 72f else 44f
    val shadowH = if (boss) 22f else 14f
    drawOval(Color(0x55000000), Offset(x - shadowW / 2f, y - 4f), Size(shadowW, shadowH))

    val animSprite = art?.zombieAnimFrameOrNull(
        kind = monster.kind,
        attacking = monster.attacking,
        walking = monster.moving,
        frame = monster.animFrame,
    )
    if (animSprite != null && art.hasZombieAnim(monster.kind)) {
        drawCustomSprite(
            image = animSprite,
            cx = x,
            footY = y,
            worldHeight = bodyH,
            mirrorX = monster.facingLeft,
        )
    } else {
        val kenneyId = when (monster.kind) {
            "shambler", "bloater", "boss_abomination", "spitter" -> DungeonTiles.SLIME
            "runner" -> DungeonTiles.BAT
            "armored", "blacksmith", "boss_warden" -> DungeonTiles.ORC
            "farmer" -> DungeonTiles.SPIDER
            "golem", "boss_lich", "plague_archer",
            "skel_soldier", "skel_archer", "ghost_cavalry", "boss_skel_king" -> DungeonTiles.SKELETON
            else -> DungeonTiles.SLIME
        }
        val static = art?.zombieSpriteOrNull(monster.kind)
        when {
            static != null -> drawCustomSprite(
                static,
                x,
                y,
                worldHeight = if (boss) 112f else 64f,
                mirrorX = monster.facingLeft,
            )
            atlas != null -> drawKenneySprite(
                atlas.dungeon,
                kenneyId,
                x,
                y,
                size = if (boss) 92f else 56f,
            )
            else -> {
                val body = Path().apply {
                    val w = if (boss) 32f else 18f
                    val h = if (boss) 64f else 38f
                    moveTo(x - w, y)
                    quadraticBezierTo(x - w - 2f, y - h + 4f, x, y - h)
                    quadraticBezierTo(x + w + 2f, y - h + 4f, x + w, y)
                    close()
                }
                drawPath(body, if (boss) Color(0xFF8B2E2E) else Color(0xFF6FBF5A))
                drawPath(body, Color(0xFF2F5A28), style = Stroke(3f))
            }
        }
    }
    val labelColor = if (boss) Color(0xFFFFD27A) else Color(0xFFE8D9B8)
    val label = if (boss) "★ ${monster.name}" else monster.name
    drawLabel(label, x - if (boss) 64f else 48f, y + 14f, if (boss) 15f else 13f, labelColor)
    if (boss) {
        // 간단한 체력 막대
        val barW = 70f
        val ratio = (monster.hp.toFloat() / monster.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
        drawRoundRect(Color(0xAA1A0C0C), Offset(x - barW / 2f, y - bodyH - 10f), Size(barW, 7f), CornerRadius(3f, 3f))
        drawRoundRect(Color(0xFFC0392B), Offset(x - barW / 2f, y - bodyH - 10f), Size(barW * ratio, 7f), CornerRadius(3f, 3f))
    }
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
        val r = if (it.isBoss) 4.2f else 2.2f
        val color = if (it.isBoss) Color(0xFFFF6B3D) else Color(0xFFC0392B)
        drawCircle(color, r, Offset(left + it.x * sx, top + it.y * sy))
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
