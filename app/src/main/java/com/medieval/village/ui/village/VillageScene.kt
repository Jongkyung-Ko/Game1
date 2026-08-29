package com.medieval.village.ui.village

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.model.Village
import com.medieval.village.ui.PartySwitchBar
import com.medieval.village.ui.mapZoomGestures
import com.medieval.village.ui.rememberMapZoomState
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.withMapZoom
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun VillageScene(vm: GameViewModel, modifier: Modifier = Modifier) {
    val art = rememberCustomArtOrNull()
    val mapZoom = rememberMapZoomState()
    val settlement = vm.settlement
    val places = settlement.places
    BoxWithConstraints(modifier.clipToBounds().background(Color(0xFF1A140E))) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val s = min(wPx / Village.W, hPx / Village.H).coerceAtLeast(0.01f)
        val ox = (wPx - Village.W * s) / 2f
        val oy = (hPx - Village.H * s) / 2f
        val heroX = vm.heroX
        val heroY = vm.heroY
        val facing = vm.facing
        val walking = vm.walking
        val walkPhase = vm.walkPhase
        val party = vm.activeParty
        val frontIndex = vm.frontIndex
        val partySlots = vm.partyDrawSlots(heroX, heroY)
        val viewSize = Size(wPx, hPx)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(s, ox, oy, mapZoom.zoom, mapZoom.pan, settlement.id) {
                    detectTapGestures { tap ->
                        val content = mapZoom.screenToContent(tap, viewSize)
                        val wx = (content.x - ox) / s
                        val wy = (content.y - oy) / s
                        val hit = places.firstOrNull { p ->
                            wx >= p.left - 16f && wx <= p.right + 16f &&
                                wy >= p.top - 24f && wy <= p.doorY + 20f
                        }
                        if (hit != null) vm.goToPlace(hit) else vm.walkTo(wx, wy)
                    }
                }
                .mapZoomGestures(mapZoom)
        ) {
            drawRect(Color(0xFF2A1C12), size = size)
            withMapZoom(mapZoom) {
                withTransform({
                    translate(ox, oy)
                    scale(s, s, Offset.Zero)
                }) {
                    if (art != null) {
                        drawCustomVillageMap(
                            art = art,
                            settlementId = settlement.id,
                            mapAsset = settlement.mapAsset,
                        )
                    } else {
                        drawRect(Color(0xFF6F9A54), size = Size(Village.W, Village.H))
                        drawRect(
                            Color(0xFFC2A16B),
                            topLeft = Offset(Village.W * 0.45f, 0f),
                            size = Size(Village.W * 0.1f, Village.H)
                        )
                    }

                    // 건물 이름만 표시 (핫스팟 사각형은 숨김)
                    places.forEach { p ->
                        drawPlaceLabel(p.name, p.cx, p.top - 6f)
                    }

                    // 마을 주민
                    if (art != null) {
                        settlement.townsfolk.forEachIndexed { index, (key, x, y) ->
                            val sprite = art.npcSpriteOrNull(key)
                            if (sprite != null) {
                                val bob = sin(walkPhase * 0.6f + index) * 1.5f
                                drawCustomSprite(
                                    image = sprite,
                                    cx = x,
                                    footY = y + bob,
                                    worldHeight = 78f,
                                )
                            }
                        }
                    }

                    drawVillageFollowParty(
                        heroX = heroX,
                        heroY = heroY,
                        facing = facing,
                        walking = walking,
                        walkPhase = walkPhase,
                        mercs = party,
                        art = art,
                        heroScale = 1.08f,
                        mercScale = 0.76f,
                        frontIndex = frontIndex,
                        frontAnimKind = if (walking) HeroAnimKind.WALK else HeroAnimKind.IDLE,
                        slots = partySlots,
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
                }
            }
        }

        Text(
            text = if (art != null) {
                "${settlement.nameEn} · v0.4.43"
            } else {
                "${settlement.nameEn} · v0.4.43 (맵 로딩 실패)"
            },
            color = Color(0xFFFFE29A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xCC000000), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )

        PartySwitchBar(
            vm = vm,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        )

        val near = places.firstOrNull {
            hypot(vm.heroX - it.doorX, vm.heroY - it.doorY) < 48f
        }
        if (near != null && !walking) {
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

private fun DrawScope.drawPlaceLabel(text: String, cx: Float, baselineY: Float) {
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#FFE29A")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val width = paint.measureText(text)
        val padX = 10f
        val padY = 6f
        val top = baselineY - paint.textSize
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(0xCC, 0x1A, 0x12, 0x0C)
        }
        canvas.nativeCanvas.drawRoundRect(
            cx - width / 2f - padX,
            top - padY,
            cx + width / 2f + padX,
            baselineY + padY,
            10f,
            10f,
            bg
        )
        canvas.nativeCanvas.drawText(text, cx, baselineY, paint)
    }
}
