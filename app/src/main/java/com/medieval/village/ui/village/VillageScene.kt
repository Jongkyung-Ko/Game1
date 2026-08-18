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
    BoxWithConstraints(modifier.background(Color(0xFF1A140E))) {
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
                .pointerInput(s, ox, oy, mapZoom.zoom, mapZoom.pan) {
                    detectTapGestures { tap ->
                        val content = mapZoom.screenToContent(tap, viewSize)
                        val wx = (content.x - ox) / s
                        val wy = (content.y - oy) / s
                        val hit = Village.places.firstOrNull { p ->
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
                        drawCustomVillageMap(art)
                    } else {
                        drawRect(Color(0xFF6F9A54), size = Size(Village.W, Village.H))
                        drawRect(
                            Color(0xFFC2A16B),
                            topLeft = Offset(Village.W * 0.45f, 0f),
                            size = Size(Village.W * 0.1f, Village.H)
                        )
                    }

                    // 건물 핫스팟 + 이름 (같은 Canvas 변환으로 정렬)
                    Village.places.forEach { p ->
                        drawRoundRect(
                            color = Color(0x88FFE29A),
                            topLeft = Offset(p.left, p.top),
                            size = Size(p.w, p.h),
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(width = 3.5f)
                        )
                        drawPlaceLabel(p.name, p.cx, p.top - 6f)
                    }

                    // 마을 주민
                    if (art != null) {
                        Village.townsfolk.forEachIndexed { index, (key, x, y) ->
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
                        mercScale = 0.82f,
                        frontIndex = frontIndex,
                        frontAnimKind = if (walking) HeroAnimKind.WALK else HeroAnimKind.IDLE,
                        slots = partySlots,
                    )
                }
            }
        }

        Text(
            text = if (art != null) "Oakhaven · v0.4.20" else "Oakhaven · v0.4.20 (맵 로딩 실패)",
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

        val near = Village.places.firstOrNull {
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
            8f,
            8f,
            bg
        )
        canvas.nativeCanvas.drawText(text, cx, baselineY, paint)
    }
}
