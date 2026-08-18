package com.medieval.village.ui.place

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.Facing
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.NpcKind
import com.medieval.village.model.PubNpc
import com.medieval.village.model.PubNpcCatalog
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.PartySwitchBar
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawVillageFollowParty
import com.medieval.village.ui.village.rememberCustomArtOrNull
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
@Composable
fun PubScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val art = rememberCustomArtOrNull()
    Column(modifier = modifier.fillMaxSize().background(Palette.WoodDark)) {
        Text(
            text = "PUB · 신성한 잔 선술집",
            color = Palette.Gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color(0xFF21150E), RoundedCornerShape(12.dp))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val scale = min(widthPx / PubNpcCatalog.WORLD_W, heightPx / PubNpcCatalog.WORLD_H)
            val offsetX = (widthPx - PubNpcCatalog.WORLD_W * scale) / 2f
            val offsetY = (heightPx - PubNpcCatalog.WORLD_H * scale) / 2f
            val facing = vm.facing
            val walking = vm.pubWalking
            val walkPhase = vm.walkPhase

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(scale, offsetX, offsetY) {
                        detectTapGestures { tap ->
                            val x = (tap.x - offsetX) / scale
                            val y = (tap.y - offsetY) / scale
                            val npc = PubNpcCatalog.all.minByOrNull {
                                hypot(x - it.x, y - it.y)
                            }?.takeIf { hypot(x - it.x, y - it.y) < 95f }
                            if (npc != null) vm.approachPubNpc(npc) else vm.walkInPub(x, y)
                        }
                    }
            ) {
                withTransform({
                    translate(offsetX, offsetY)
                    scale(scale, scale, Offset.Zero)
                }) {
                    drawPubBackground(art)
                    PubNpcCatalog.all.forEach { npc ->
                        drawPubNpc(npc, vm.pubSpeakerId == npc.id, vm.pubDialogue, art)
                    }
                    drawVillageFollowParty(
                        heroX = vm.pubHeroX,
                        heroY = vm.pubHeroY,
                        facing = facing,
                        walking = walking,
                        walkPhase = walkPhase,
                        mercs = vm.activeParty,
                        art = art,
                        heroScale = 1.08f,
                        mercScale = 0.76f,
                        frontIndex = vm.frontIndex,
                        slots = vm.partyDrawSlots(vm.pubHeroX, vm.pubHeroY),
                    )
                }
            }

            PartySwitchBar(
                vm = vm,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                "NPC를 누르면 가까이 이동한 뒤 대화합니다.",
                color = Palette.ParchmentDim,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(5.dp))
            MessageLog(vm.log, Modifier.height(82.dp))
            Spacer(Modifier.height(7.dp))
            WoodButton("마을로 나가기", Modifier.fillMaxWidth(), highlight = true) {
                vm.leavePlace()
            }
        }
    }
}

private fun DrawScope.drawPubBackground(art: CustomArt?) {
    val w = PubNpcCatalog.WORLD_W
    val h = PubNpcCatalog.WORLD_H
    val roomArt = art?.interiorOrNull("pub")
    if (roomArt != null) {
        drawImage(
            image = roomArt,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(roomArt.width, roomArt.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(w.roundToInt(), h.roundToInt()),
            filterQuality = FilterQuality.Medium,
        )
        drawRect(Color(0x22000000), Offset.Zero, Size(w, h * 0.08f))
        drawRect(Color(0x33000000), Offset(0f, h * 0.92f), Size(w, h * 0.08f))
        return
    }

    // 따뜻한 목조 벽과 석재 하단 (폴백)
    drawRect(Color(0xFF5A2F21), size = Size(w, h))
    for (x in 0..10) {
        drawRect(
            if (x % 2 == 0) Color(0xFF74402B) else Color(0xFF663523),
            Offset(x * 100f, 0f),
            Size(100f, 250f)
        )
    }
    drawRect(Color(0xFF826348), Offset(0f, 250f), Size(w, 450f))

    // 사선 판자 바닥으로 등각 시점 분위기
    for (y in 250..700 step 42) {
        drawLine(Color(0xFF4B3325), Offset(0f, y.toFloat()), Offset(w, y.toFloat()), 3f)
    }
    for (x in -300..1200 step 95) {
        drawLine(Color(0x55341F16), Offset(x.toFloat(), 250f), Offset(x + 250f, h), 3f)
    }

    // 천장 들보
    drawRect(Color(0xFF352018), Offset(0f, 25f), Size(w, 28f))
    drawRect(Color(0xFF352018), Offset(0f, 210f), Size(w, 24f))
    drawRect(Color(0xFF352018), Offset(80f, 0f), Size(28f, 260f))
    drawRect(Color(0xFF352018), Offset(905f, 0f), Size(28f, 300f))

    drawFireplace()
    drawQuestBoard()
    drawBarCounter()
    drawTables()
    drawStairs()
}

private fun DrawScope.drawFireplace() {
    drawRect(Color(0xFF786B60), Offset(25f, 82f), Size(210f, 205f))
    drawRect(Color(0xFF2B1A13), Offset(67f, 136f), Size(126f, 151f))
    drawRect(Color(0xFF998878), Offset(10f, 68f), Size(240f, 28f))
    drawOval(Color(0xFF3D2419), Offset(58f, 226f), Size(145f, 48f))
    val flame = Path().apply {
        moveTo(91f, 250f)
        quadraticBezierTo(107f, 180f, 126f, 242f)
        quadraticBezierTo(148f, 164f, 166f, 252f)
        close()
    }
    drawPath(flame, Color(0xFFE8582C))
    drawCircle(Color(0xFFFFC857), 26f, Offset(130f, 243f))
    drawCircle(Color(0x33FFB23E), 110f, Offset(130f, 212f))
}

private fun DrawScope.drawQuestBoard() {
    drawRect(Color(0xFF382219), Offset(335f, 58f), Size(375f, 214f))
    drawRect(Color(0xFF8A5A32), Offset(348f, 74f), Size(349f, 182f))
    drawRect(Color(0xFF3E2519), Offset(440f, 34f), Size(164f, 44f))
    drawLabel("QUESTS", 475f, 65f, 27f, Color(0xFFE2B866))
    val notes = listOf(
        Triple(374f, 102f, Color(0xFFE8D8B6)),
        Triple(450f, 91f, Color(0xFFD9C08D)),
        Triple(528f, 114f, Color(0xFFE9DEC6)),
        Triple(608f, 93f, Color(0xFFCBB890)),
        Triple(394f, 178f, Color(0xFFD6C39C)),
        Triple(486f, 170f, Color(0xFFEAD9B5)),
        Triple(577f, 185f, Color(0xFFD7BE86))
    )
    notes.forEachIndexed { i, (x, y, color) ->
        drawRect(color, Offset(x, y), Size(55f, 61f))
        drawLine(Color(0xFF8A765A), Offset(x + 8f, y + 18f), Offset(x + 45f, y + 18f), 3f)
        drawLine(Color(0xFF8A765A), Offset(x + 8f, y + 31f), Offset(x + 39f, y + 31f), 2f)
        drawCircle(if (i % 2 == 0) Color(0xFF9B3B2E) else Color(0xFF355D7A), 5f, Offset(x + 28f, y + 5f))
    }
}

private fun DrawScope.drawBarCounter() {
    drawRect(Color(0xFF4C2C1D), Offset(705f, 282f), Size(295f, 176f))
    drawRect(Color(0xFF9A5E34), Offset(688f, 278f), Size(312f, 28f))
    for (x in 725..975 step 48) {
        drawRect(Color(0x55311A12), Offset(x.toFloat(), 306f), Size(4f, 150f))
    }
    // 병과 잔
    val colors = listOf(Color(0xFF56806B), Color(0xFF9B6A43), Color(0xFF5B6F92))
    colors.forEachIndexed { i, color ->
        drawRect(color, Offset(750f + i * 65f, 241f), Size(18f, 37f))
        drawRect(color, Offset(755f + i * 65f, 231f), Size(8f, 12f))
    }
}

private fun DrawScope.drawTables() {
    listOf(240f to 355f, 460f to 520f, 790f to 570f).forEachIndexed { index, (x, y) ->
        drawOval(Color(0xFF3C261A), Offset(x - 92f, y - 16f), Size(184f, 68f))
        drawOval(Color(0xFF89542F), Offset(x - 92f, y - 28f), Size(184f, 62f))
        drawRect(Color(0xFF4A2D1D), Offset(x - 12f, y + 22f), Size(24f, 62f))
        drawCircle(if (index == 1) Color(0xFFB94134) else Color(0xFFD9B15D), 10f, Offset(x - 28f, y - 2f))
        drawRect(Color(0xFFD3B887), Offset(x + 20f, y - 12f), Size(30f, 22f))
    }
}

private fun DrawScope.drawStairs() {
    for (i in 0..5) {
        drawRect(
            Color(0xFF4A2A1C).copy(alpha = 1f - i * 0.04f),
            Offset(0f, 480f + i * 36f),
            Size(125f + i * 18f, 28f)
        )
        drawLine(Color(0xFF9B643D), Offset(0f, 480f + i * 36f), Offset(125f + i * 18f, 480f + i * 36f), 4f)
    }
}

private fun DrawScope.drawPubNpc(
    npc: PubNpc,
    speaking: Boolean,
    dialogue: String?,
    art: CustomArt?,
) {
    val sprite = art?.npcSpriteOrNull(npc.spriteKey)
    if (sprite != null) {
        drawCustomSprite(
            image = sprite,
            cx = npc.x,
            footY = npc.y,
            worldHeight = 118f,
        )
    } else {
        val outfit = when (npc.kind) {
            NpcKind.OWNER -> Color(0xFF9A3F35)
            NpcKind.TRAVELER -> Color(0xFF456B8E)
            NpcKind.GUILD_MEMBER -> Color(0xFF4E7843)
            NpcKind.DRUNK -> Color(0xFF775489)
        }
        drawOval(Color(0x33000000), Offset(npc.x - 27f, npc.y - 7f), Size(54f, 18f))
        drawRect(Color(0xFF453326), Offset(npc.x - 17f, npc.y - 34f), Size(12f, 34f))
        drawRect(Color(0xFF453326), Offset(npc.x + 5f, npc.y - 34f), Size(12f, 34f))
        val body = Path().apply {
            moveTo(npc.x - 25f, npc.y - 92f)
            lineTo(npc.x + 25f, npc.y - 92f)
            lineTo(npc.x + 31f, npc.y - 31f)
            lineTo(npc.x - 31f, npc.y - 31f)
            close()
        }
        drawPath(body, outfit)
        drawCircle(Color(0xFFE2B087), 21f, Offset(npc.x, npc.y - 113f))
        drawArc(
            if (npc.kind == NpcKind.DRUNK) Color(0xFF8B6A42) else Color(0xFF4D3325),
            180f,
            180f,
            true,
            Offset(npc.x - 22f, npc.y - 136f),
            Size(44f, 35f)
        )
        if (npc.kind == NpcKind.OWNER) {
            drawRect(Color(0xFFE7D9C2), Offset(npc.x - 19f, npc.y - 82f), Size(38f, 44f))
        }
    }
    drawLabel("${npc.name} · ${npc.role}", npc.x - 58f, npc.y + 22f, 18f, Color(0xFFF3E4C5))

    if (speaking && dialogue != null) {
        drawSpeechBubble(npc.x, npc.y - 150f, dialogue)
    } else {
        drawCircle(Color(0xFFE8D9B8), 17f, Offset(npc.x + 28f, npc.y - 130f))
        drawLabel("…", npc.x + 20f, npc.y - 124f, 20f, Color(0xFF342017))
    }
}

private fun DrawScope.drawSpeechBubble(cx: Float, bottomY: Float, text: String) {
    val lines = wrapDialogue(text, 19)
    val width = 330f
    val height = 48f + lines.size * 25f
    val left = (cx - width / 2f).coerceIn(10f, PubNpcCatalog.WORLD_W - width - 10f)
    val top = (bottomY - height).coerceAtLeast(10f)
    drawRoundRect(
        Color(0xFFF2E5C8),
        Offset(left, top),
        Size(width, height),
        CornerRadius(18f, 18f)
    )
    drawRoundRect(
        Color(0xFF4A2B1B),
        Offset(left, top),
        Size(width, height),
        CornerRadius(18f, 18f),
        style = Stroke(4f)
    )
    val tail = Path().apply {
        moveTo(cx - 14f, top + height)
        lineTo(cx, top + height + 20f)
        lineTo(cx + 16f, top + height)
        close()
    }
    drawPath(tail, Color(0xFFF2E5C8))
    lines.forEachIndexed { index, line ->
        drawLabel(line, left + 18f, top + 34f + index * 25f, 20f, Color(0xFF2F1C13))
    }
}

private fun wrapDialogue(text: String, length: Int): List<String> {
    if (text.length <= length) return listOf(text)
    val result = mutableListOf<String>()
    var start = 0
    while (start < text.length && result.size < 3) {
        val end = (start + length).coerceAtMost(text.length)
        result += text.substring(start, end)
        start = end
    }
    return result
}

private fun DrawScope.drawLabel(text: String, x: Float, y: Float, size: Float, color: Color) {
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toArgb()
            textSize = size
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.nativeCanvas.drawText(text, x, y, paint)
    }
}

private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
