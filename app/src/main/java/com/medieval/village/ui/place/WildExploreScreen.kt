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
import com.medieval.village.model.ItemCatalog
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.PartySwitchBar
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.mapZoomGestures
import com.medieval.village.ui.rememberMapZoomState
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.withMapZoom
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.PARTY_REAR_SCALE_FACTOR
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawLevelUpBurst
import com.medieval.village.ui.village.drawPartySlots
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawKenneySprite
import com.medieval.village.ui.village.drawKenneySpriteAsset
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.rememberCustomArtOrNull
import com.medieval.village.ui.village.rememberKenneyAtlasOrNull
import kotlin.math.max
import kotlin.math.min

enum class WildTheme {
    FOREST, DESERT, GLACIER
}

private data class WildThemeUi(
    val title: String,
    val subtitle: String,
    val recordLabel: (Int) -> String,
    val foeLabel: String,
    val foeChip: Color,
    val chromeBg: Color,
    val mapFrameBg: Color,
    val border: Color,
    val canvasBg: Color,
    val watermark: String,
    val exitHint: String,
    val deepHint: String,
    val moveHint: String,
    val deepButton: String,
)

private fun themeUi(theme: WildTheme, zone: Int, record: Int, foeCount: Int): WildThemeUi = when (theme) {
    WildTheme.FOREST -> WildThemeUi(
        title = "동쪽 숲 · ${zone}지대",
        subtitle = "",
        recordLabel = { "기록 ${it}지대" },
        foeLabel = "짐승 $foeCount",
        foeChip = Color(0xFF4A7A38),
        chromeBg = Color(0xFF1A2414),
        mapFrameBg = Color(0xFFC8D9A4),
        border = Color(0xFF3A5028),
        canvasBg = Color(0xFFDCE8B8),
        watermark = "v0.4.32 Eastern forest",
        exitHint = "↑ 탈출",
        deepHint = "↓ 들어가기",
        moveHint = "왼쪽 패드 이동 · 오른쪽 공격 · 상자는 탭",
        deepButton = "들어가기",
    )
    WildTheme.DESERT -> WildThemeUi(
        title = "남쪽 사막 · ${zone}지대",
        subtitle = "",
        recordLabel = { "기록 ${it}지대" },
        foeLabel = "괴물 $foeCount",
        foeChip = Color(0xFFC07828),
        chromeBg = Color(0xFF2A1E12),
        mapFrameBg = Color(0xFFE8D4A0),
        border = Color(0xFF8A5A28),
        canvasBg = Color(0xFFF0E0B0),
        watermark = "v0.4.32 Southern desert",
        exitHint = "↑ 탈출",
        deepHint = "↓ 들어가기",
        moveHint = "왼쪽 패드 이동 · 오른쪽 공격 · 상자는 탭",
        deepButton = "들어가기",
    )
    WildTheme.GLACIER -> WildThemeUi(
        title = "북쪽 빙하 · ${zone}지대",
        subtitle = "",
        recordLabel = { "기록 ${it}지대" },
        foeLabel = "극지 $foeCount",
        foeChip = Color(0xFF4A7A9A),
        chromeBg = Color(0xFF121820),
        mapFrameBg = Color(0xFFD0E0F0),
        border = Color(0xFF3A5A78),
        canvasBg = Color(0xFFE8F0F8),
        watermark = "v0.4.32 Northern glacier",
        exitHint = "↑ 탈출",
        deepHint = "↓ 들어가기",
        moveHint = "왼쪽 패드 이동 · 오른쪽 공격 · 상자는 탭",
        deepButton = "들어가기",
    )
}

@Composable
fun WildExploreScreen(vm: GameViewModel, theme: WildTheme, modifier: Modifier = Modifier) {
    val atlas = rememberKenneyAtlasOrNull()
    val art = rememberCustomArtOrNull()
    LaunchedEffect(Unit) { vm.ensureDungeonLoaded() }
    val floor = vm.dungeonFloor
    val record = when (theme) {
        WildTheme.FOREST -> vm.player.forestDepth
        WildTheme.DESERT -> vm.player.desertDepth
        WildTheme.GLACIER -> vm.player.glacierDepth
    }
    val foeCount = floor?.monsters?.count { it.alive } ?: 0
    val ui = themeUi(theme, vm.dungeonFloorNumber, record, foeCount)
    val mod = Modifier
    val contextHint = when (vm.dungeonHint) {
        "stairs_up" -> ui.exitHint
        "stairs_down" -> ui.deepHint
        "portal" -> "◎ 집으로"
        "chest" -> "◆ 상자 열기"
        else -> null
    }

    Column(modifier = modifier.fillMaxSize().background(ui.chromeBg)) {
        // 맨 윗줄: 이름·지대만
        Text(
            text = ui.title,
            color = Palette.Gold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )

        if (contextHint != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .background(Color(0xAA1B120A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(contextHint, color = Palette.Parchment, fontSize = 11.sp)
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(ui.mapFrameBg, RoundedCornerShape(12.dp))
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
                    .pointerInput(floor?.floor, widthPx, heightPx, theme, mapZoom.zoom, mapZoom.pan) {
                        detectTapGestures { tap ->
                            val map = vm.dungeonFloor ?: return@detectTapGestures
                            val content = mapZoom.screenToContent(tap, viewSize)
                            val cam = wildCamera(map, vm.dungeonHeroX, vm.dungeonHeroY, widthPx, heightPx)
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

                drawRect(ui.canvasBg, size = size)
                drawRoundRect(
                    ui.border,
                    Offset(6f, 6f),
                    Size(size.width - 12f, size.height - 12f),
                    CornerRadius(14f, 14f),
                    style = Stroke(5f)
                )
                val map = floor
                if (map == null) {
                    wildLabel("지도를 펼치는 중…", size.width * 0.28f, size.height * 0.5f, 28f, ui.border)
                    return@Canvas
                }
                val viewW = size.width.coerceAtLeast(1f)
                val viewH = size.height.coerceAtLeast(1f)
                val (camX, camY) = wildCamera(map, heroX, heroY, viewW, viewH)
                withMapZoom(mapZoom) {
                    withTransform({ translate(-camX, -camY) }) {
                        when (theme) {
                            WildTheme.FOREST -> drawForestFloor(atlas, map)
                            WildTheme.DESERT -> drawDesertFloor(atlas, map)
                            WildTheme.GLACIER -> drawGlacierFloor(atlas, map)
                        }
                        map.monsters.filter { it.alive }.forEach { monster ->
                            drawWildBeast(atlas, art, theme, monster)
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
                drawWildMinimap(map, theme, heroX, heroY, viewW, viewH)
                wildLabel(ui.watermark, 14f, 28f, 18f, ui.border)
            }

            DungeonMapStatusOverlay(vm)
        }

        PartySwitchBar(
            vm = vm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            DungeonCombatControls(
                attackLabel = vm.attackLabel(),
                attackEnabled = vm.attackReady && vm.dungeonFloor != null && vm.levelUpSkillOffer == null,
                onPad = { dx, dy -> vm.setDungeonPad(dx, dy) },
                onPadRelease = { vm.clearDungeonPad() },
                onAttack = { vm.dungeonAttack() },
                logContent = {
                    MessageLog(vm.log, mod.fillMaxWidth().height(72.dp))
                },
                skillSlots = vm.frontSkillSlotsUi(),
                onSpecial = { slot -> vm.dungeonSpecialAttack(slot) },
            )
            Spacer(mod.height(6.dp))
            Row(
                modifier = mod.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (vm.dungeonHint) {
                    "stairs_up" -> WoodButton("탈출", mod.weight(1f), highlight = true) { vm.escapeDungeon() }
                    "stairs_down" -> WoodButton(ui.deepButton, mod.weight(1f), highlight = true) { vm.descendDungeon() }
                    "portal" -> WoodButton("집으로", mod.weight(1f), highlight = true) { vm.enterHomePortal() }
                    "chest" -> WoodButton("열기", mod.weight(1f), highlight = true) { vm.openDungeonChest() }
                    else -> WoodButton("탈출", mod.weight(1f), enabled = false) {}
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
                ) { potion?.let { vm.useItem(it.item) } }
            }
        }
    }
}


@Composable
fun ForestScreen(vm: GameViewModel, modifier: Modifier = Modifier) =
    WildExploreScreen(vm, WildTheme.FOREST, modifier)

@Composable
fun DesertScreen(vm: GameViewModel, modifier: Modifier = Modifier) =
    WildExploreScreen(vm, WildTheme.DESERT, modifier)

@Composable
fun GlacierScreen(vm: GameViewModel, modifier: Modifier = Modifier) =
    WildExploreScreen(vm, WildTheme.GLACIER, modifier)

private fun wildCamera(
    map: DungeonFloor,
    heroX: Float,
    heroY: Float,
    viewW: Float,
    viewH: Float
): Pair<Float, Float> {
    val maxX = max(0f, map.worldW - viewW)
    val maxY = max(0f, map.worldH - viewH)
    return (heroX - viewW / 2f).coerceIn(0f, maxX) to (heroY - viewH / 2f).coerceIn(0f, maxY)
}

// ----- Forest floor (from previous ForestScreen) -----

private fun DrawScope.drawForestFloor(atlas: KenneyAtlas?, map: DungeonFloor) {
    drawRect(Color(0xFF1E2E18), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val cr = CornerRadius(ts * 0.14f, ts * 0.14f)
    fun isWalk(c: Int, r: Int) = map.tileAt(c, r) != DungeonTile.WALL
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            val x = c * ts
            val y = r * ts
            val grass = if ((c + r) % 2 == 0) Color(0xFF6FA84E) else Color(0xFF5E943F)
            drawRoundRect(grass, Offset(x + 1f, y + 1f), Size(ts - 2f, ts - 2f), cr)
            if (atlas != null) {
                when {
                    (c * 7 + r * 3) % 5 == 0 -> drawKenneyTile(atlas.town, TownTiles.GRASS_TUFT, x, y, ts)
                    (c + r) % 7 == 0 -> drawKenneyTile(atlas.town, TownTiles.GRASS_FLOWER, x, y, ts)
                    map.tileAt(c, r) == DungeonTile.FLOOR && (c * 5 + r) % 4 == 0 ->
                        drawKenneyTile(atlas.town, TownTiles.PATH, x, y, ts * 0.92f)
                }
            }
        }
    }
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) != DungeonTile.WALL) continue
            if (!(isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r))) continue
            val x = c * ts
            val y = r * ts
            drawCircle(Color(0xFF2F5A28), ts * 0.42f, Offset(x + ts * 0.5f, y + ts * 0.42f))
            drawRoundRect(Color(0xFF5A3A22), Offset(x + ts * 0.42f, y + ts * 0.55f), Size(ts * 0.16f, ts * 0.35f), CornerRadius(3f))
            if (atlas != null && (c * 11 + r * 5) % 3 == 0) {
                atlas.spriteOrNull(if ((c + r) % 2 == 0) "tree_g" else "tree_o")?.let {
                    drawKenneySpriteAsset(it, x + ts * 0.5f, y + ts * 0.95f, ts * 1.15f)
                }
            }
        }
    }
    drawWildSpecialTiles(atlas, map, exitLabel = "마을", deepLabel = "깊은숲", deepColor = Color(0xFF8A5A32))
}

// ----- Desert floor -----

private fun DrawScope.drawDesertFloor(atlas: KenneyAtlas?, map: DungeonFloor) {
    drawRect(Color(0xFFC9A66A), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val cr = CornerRadius(ts * 0.12f, ts * 0.12f)
    fun isWalk(c: Int, r: Int) = map.tileAt(c, r) != DungeonTile.WALL
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            val x = c * ts
            val y = r * ts
            val sand = if ((c + r) % 2 == 0) Color(0xFFE8C878) else Color(0xFFD9B86A)
            drawRoundRect(sand, Offset(x + 1f, y + 1f), Size(ts - 2f, ts - 2f), cr)
            // 모래 결
            if ((c * 3 + r) % 4 == 0) {
                drawLine(Color(0x33A07030), Offset(x + 8f, y + ts * 0.4f), Offset(x + ts - 8f, y + ts * 0.55f), 2f)
            }
        }
    }
    // 모래언덕 / 바위 (WALL)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) != DungeonTile.WALL) continue
            if (!(isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r))) continue
            val x = c * ts
            val y = r * ts
            val dune = Path().apply {
                moveTo(x + 4f, y + ts - 4f)
                quadraticBezierTo(x + ts * 0.5f, y + 6f, x + ts - 4f, y + ts - 4f)
                close()
            }
            drawPath(dune, Color(0xFFC09048))
            drawPath(dune, Color(0xFF8A6030), style = Stroke(2f))
            // 선인장
            if ((c + r * 2) % 3 == 0) {
                drawRoundRect(Color(0xFF5A8A3A), Offset(x + ts * 0.42f, y + ts * 0.25f), Size(ts * 0.14f, ts * 0.5f), CornerRadius(3f))
                drawRoundRect(Color(0xFF5A8A3A), Offset(x + ts * 0.28f, y + ts * 0.38f), Size(ts * 0.18f, ts * 0.12f), CornerRadius(3f))
                drawRoundRect(Color(0xFF5A8A3A), Offset(x + ts * 0.52f, y + ts * 0.32f), Size(ts * 0.18f, ts * 0.12f), CornerRadius(3f))
            }
        }
    }
    drawWildSpecialTiles(atlas, map, exitLabel = "마을", deepLabel = "모래길", deepColor = Color(0xFFA05020))
}

// ----- Glacier floor -----

private fun DrawScope.drawGlacierFloor(atlas: KenneyAtlas?, map: DungeonFloor) {
    drawRect(Color(0xFFB8D0E8), size = Size(map.worldW, map.worldH))
    val ts = map.tileSize
    val cr = CornerRadius(ts * 0.12f, ts * 0.12f)
    fun isWalk(c: Int, r: Int) = map.tileAt(c, r) != DungeonTile.WALL
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) == DungeonTile.WALL) continue
            val x = c * ts
            val y = r * ts
            val ice = if ((c + r) % 2 == 0) Color(0xFFE8F4FF) else Color(0xFFD0E8F8)
            drawRoundRect(ice, Offset(x + 1f, y + 1f), Size(ts - 2f, ts - 2f), cr)
            drawRoundRect(Color(0x66A0C8E8), Offset(x + 6f, y + 6f), Size(ts * 0.35f, ts * 0.2f), CornerRadius(3f))
        }
    }
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            if (map.tileAt(c, r) != DungeonTile.WALL) continue
            if (!(isWalk(c, r - 1) || isWalk(c, r + 1) || isWalk(c - 1, r) || isWalk(c + 1, r))) continue
            val x = c * ts
            val y = r * ts
            // 빙벽 / 빙산
            val peak = Path().apply {
                moveTo(x + 6f, y + ts - 4f)
                lineTo(x + ts * 0.5f, y + 8f)
                lineTo(x + ts - 6f, y + ts - 4f)
                close()
            }
            drawPath(peak, Color(0xFFD8ECF8))
            drawPath(peak, Color(0xFF6A90B0), style = Stroke(2.2f))
            drawCircle(Color(0xAAFFFFFF), ts * 0.12f, Offset(x + ts * 0.45f, y + ts * 0.45f))
        }
    }
    drawWildSpecialTiles(atlas, map, exitLabel = "마을", deepLabel = "심빙", deepColor = Color(0xFF3A6A9A))
}

private fun DrawScope.drawWildSpecialTiles(
    atlas: KenneyAtlas?,
    map: DungeonFloor,
    exitLabel: String,
    deepLabel: String,
    deepColor: Color,
) {
    val ts = map.tileSize
    val cr = CornerRadius(ts * 0.12f, ts * 0.12f)
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val x = c * ts
            val y = r * ts
            when (map.tileAt(c, r)) {
                DungeonTile.STAIRS_UP -> {
                    drawRoundRect(Color(0xFF8FCF7A), Offset(x + 6f, y + 6f), Size(ts - 12f, ts - 12f), cr)
                    wildLabel(exitLabel, x + 10f, y + ts * 0.55f, 14f, Color(0xFF1E2E18))
                }
                DungeonTile.STAIRS_DOWN -> {
                    drawRoundRect(deepColor, Offset(x + 6f, y + 6f), Size(ts - 12f, ts - 12f), cr)
                    wildLabel(deepLabel, x + 6f, y + ts * 0.55f, 13f, Color(0xFFF3E4C5))
                }
                DungeonTile.PORTAL -> {
                    drawCircle(Color(0xAA3AB0E0), ts * 0.35f, Offset(x + ts * 0.5f, y + ts * 0.5f))
                    drawCircle(Color(0xCC8FE8FF), ts * 0.18f, Offset(x + ts * 0.5f, y + ts * 0.5f))
                }
                DungeonTile.VAULT -> if (atlas != null) {
                    drawKenneyTile(atlas.dungeon, DungeonTiles.CHEST, x, y, ts)
                } else {
                    drawRoundRect(Color(0xFFD9A441), Offset(x + 8f, y + 12f), Size(ts - 16f, ts - 20f), cr)
                }
                DungeonTile.CHEST_OPEN -> if (atlas != null) {
                    drawKenneyTile(atlas.dungeon, DungeonTiles.CHEST_OPEN, x, y, ts)
                } else {
                    drawRoundRect(Color(0xFF8A7040), Offset(x + 8f, y + 12f), Size(ts - 16f, ts - 20f), cr)
                }
                DungeonTile.SEWER -> {
                    // theme-neutral scrub / snowdrift / dune scrub
                    drawCircle(Color(0x668A7040), ts * 0.22f, Offset(x + ts * 0.4f, y + ts * 0.55f))
                    drawCircle(Color(0x668A7040), ts * 0.18f, Offset(x + ts * 0.62f, y + ts * 0.5f))
                    if (atlas != null && (c + r) % 2 == 0) {
                        atlas.spriteOrNull("bush")?.let {
                            drawKenneySpriteAsset(it, x + ts * 0.5f, y + ts * 0.85f, ts * 0.65f)
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

private fun DrawScope.drawWildBeast(
    atlas: KenneyAtlas?,
    art: CustomArt?,
    theme: WildTheme,
    monster: DungeonMonster,
) {
    val x = monster.x
    val y = monster.y
    drawOval(Color(0x55000000), Offset(x - 20f, y - 4f), Size(40f, 12f))

    val anim = art?.monsterAnimFrameOrNull(
        kind = monster.kind,
        attacking = monster.attacking,
        walking = monster.moving,
        frame = monster.animFrame,
    )
    if (anim != null && art.hasMonsterAnim(monster.kind)) {
        drawCustomSprite(
            image = anim,
            cx = x,
            footY = y,
            worldHeight = 66f,
            mirrorX = monster.facingLeft,
        )
        wildLabel(monster.name, x - 36f, y + 14f, 13f, Color(0xFFF3E4C5))
        return
    }

    when (theme) {
        WildTheme.FOREST -> drawForestBeast(atlas, monster, x, y)
        WildTheme.DESERT -> drawDesertBeast(atlas, monster, x, y)
        WildTheme.GLACIER -> drawGlacierBeast(atlas, monster, x, y)
    }
    wildLabel(monster.name, x - 36f, y + 14f, 13f, Color(0xFFF3E4C5))
}

private fun DrawScope.drawForestBeast(atlas: KenneyAtlas?, monster: DungeonMonster, x: Float, y: Float) {
    val critter = when (monster.kind) {
        "rabbit" -> "critter_a"
        "fox", "owl" -> "critter_b"
        "deer", "stag" -> "critter_c"
        else -> null
    }
    val kenney = when (monster.kind) {
        "wolf", "dire_wolf" -> DungeonTiles.BAT
        "boar", "giant_boar", "bear" -> DungeonTiles.ORC
        "snake" -> DungeonTiles.SLIME
        "forest_spider" -> DungeonTiles.SPIDER
        else -> null
    }
    if (drawCritterOrKenney(atlas, critter, kenney, x, y)) return
    drawBeastBlob(Color(0xFF8A6A3A), x, y)
}

private fun DrawScope.drawDesertBeast(atlas: KenneyAtlas?, monster: DungeonMonster, x: Float, y: Float) {
    val critter = when (monster.kind) {
        "desert_fox" -> "critter_b"
        "dung_beetle" -> "critter_a"
        "vulture" -> "critter_c"
        else -> null
    }
    val kenney = when (monster.kind) {
        "scorpion", "giant_scorpion", "deathstalker", "camel_spider" -> DungeonTiles.SPIDER
        "sand_snake", "sidewinder", "dune_worm" -> DungeonTiles.SLIME
        "sand_golem", "desert_drake" -> DungeonTiles.ORC
        else -> null
    }
    if (drawCritterOrKenney(atlas, critter, kenney, x, y)) {
        // 전갈 집게 강조
        if (monster.kind.contains("scorpion") || monster.kind == "deathstalker") {
            drawLine(Color(0xFF8A5030), Offset(x - 22f, y - 18f), Offset(x - 10f, y - 8f), 3f)
            drawLine(Color(0xFF8A5030), Offset(x + 22f, y - 18f), Offset(x + 10f, y - 8f), 3f)
            drawCircle(Color(0xFFC0392B), 4f, Offset(x, y - 34f))
        }
        return
    }
    val color = when (monster.kind) {
        "scorpion", "giant_scorpion", "deathstalker" -> Color(0xFF8A5030)
        "desert_fox" -> Color(0xFFD09040)
        "camel_spider" -> Color(0xFFB89050)
        else -> Color(0xFFC9A050)
    }
    drawBeastBlob(color, x, y)
    if (monster.kind.contains("scorpion") || monster.kind == "camel_spider") {
        // 꼬리
        drawLine(Color(0xFF5A3020), Offset(x + 8f, y - 10f), Offset(x + 18f, y - 28f), 3.5f)
        drawCircle(Color(0xFFC0392B), 4f, Offset(x + 18f, y - 30f))
    }
}

private fun DrawScope.drawGlacierBeast(atlas: KenneyAtlas?, monster: DungeonMonster, x: Float, y: Float) {
    val critter = when (monster.kind) {
        "penguin", "ice_penguin", "frost_penguin" -> "critter_a"
        "ice_fox", "snow_hare" -> "critter_b"
        "seal", "frost_owl" -> "critter_c"
        else -> null
    }
    val kenney = when (monster.kind) {
        "ice_wolf" -> DungeonTiles.BAT
        "polar_bear", "yeti", "ice_elemental" -> DungeonTiles.ORC
        "ice_spider" -> DungeonTiles.SPIDER
        else -> null
    }
    if (drawCritterOrKenney(atlas, critter, kenney, x, y)) {
        // 북극곰/설인 흰 틴트
        if (monster.kind == "polar_bear" || monster.kind == "yeti") {
            drawCircle(Color(0x66FFFFFF), 22f, Offset(x, y - 18f))
        }
        return
    }
    val color = when (monster.kind) {
        "polar_bear", "yeti" -> Color(0xFFF0F4F8)
        "penguin", "ice_penguin", "frost_penguin" -> Color(0xFF2A2A32)
        "ice_wolf", "ice_fox" -> Color(0xFFD0D8E0)
        "ice_elemental" -> Color(0xFF8AC8F0)
        else -> Color(0xFFE0E8F0)
    }
    drawBeastBlob(color, x, y)
    if (monster.kind.contains("penguin")) {
        drawOval(Color(0xFFF4F4F8), Offset(x - 8f, y - 22f), Size(16f, 20f))
        drawCircle(Color(0xFFE8A040), 3f, Offset(x, y - 10f))
    }
}

private fun DrawScope.drawCritterOrKenney(
    atlas: KenneyAtlas?,
    critter: String?,
    kenney: Int?,
    x: Float,
    y: Float
): Boolean {
    if (atlas != null && critter != null) {
        val sprite = atlas.spriteOrNull(critter)
        if (sprite != null) {
            drawKenneySpriteAsset(sprite, x, y, 52f)
            return true
        }
    }
    if (atlas != null && kenney != null) {
        drawKenneySprite(atlas.dungeon, kenney, x, y, size = 54f)
        return true
    }
    return false
}

private fun DrawScope.drawBeastBlob(color: Color, x: Float, y: Float) {
    val path = Path().apply {
        moveTo(x - 16f, y)
        quadraticBezierTo(x - 18f, y - 28f, x, y - 32f)
        quadraticBezierTo(x + 18f, y - 28f, x + 16f, y)
        close()
    }
    drawPath(path, color)
    drawPath(path, Color(0xFF2A1A10), style = Stroke(2.5f))
    drawCircle(Color(0xFF1A1210), 2.5f, Offset(x - 5f, y - 22f))
    drawCircle(Color(0xFF1A1210), 2.5f, Offset(x + 5f, y - 22f))
}

private fun DrawScope.drawWildMinimap(
    map: DungeonFloor,
    theme: WildTheme,
    heroX: Float,
    heroY: Float,
    viewW: Float,
    viewH: Float
) {
    val mw = min(140f, viewW * 0.28f)
    val mh = mw * (map.rows.toFloat() / map.cols)
    val left = viewW - mw - 12f
    val top = 12f
    val floorColor = when (theme) {
        WildTheme.FOREST -> Color(0xFF6FA84E)
        WildTheme.DESERT -> Color(0xFFE8C878)
        WildTheme.GLACIER -> Color(0xFFD0E8F8)
    }
    drawRoundRect(Color(0xAA0D1409), Offset(left - 4f, top - 4f), Size(mw + 8f, mh + 8f), CornerRadius(8f, 8f))
    val sx = mw / map.worldW
    val sy = mh / map.worldH
    for (r in 0 until map.rows) {
        for (c in 0 until map.cols) {
            val tile = map.tileAt(c, r)
            if (tile == DungeonTile.WALL) continue
            val color = when (tile) {
                DungeonTile.STAIRS_UP -> Color(0xFF8FCF7A)
                DungeonTile.STAIRS_DOWN -> Color(0xFFC0392B)
                DungeonTile.PORTAL -> Color(0xFF55C8E8)
                DungeonTile.SEWER -> Color(0xFF8A7040)
                DungeonTile.VAULT -> Color(0xFFD9A441)
                DungeonTile.CHEST_OPEN -> Color(0xFF8A7040)
                else -> floorColor
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

private fun DrawScope.wildLabel(text: String, x: Float, y: Float, size: Float, color: Color) {
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
