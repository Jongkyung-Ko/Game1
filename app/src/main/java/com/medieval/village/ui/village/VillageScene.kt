package com.medieval.village.ui.village

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.Village
import com.medieval.village.ui.theme.Palette
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun VillageScene(vm: GameViewModel, modifier: Modifier = Modifier) {
    val art = rememberCustomArtOrNull()
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

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(s, ox, oy) {
                    detectTapGestures { tap ->
                        val wx = (tap.x - ox) / s
                        val wy = (tap.y - oy) / s
                        val hit = Village.places.firstOrNull { p ->
                            wx >= p.left - 16f && wx <= p.right + 16f &&
                                wy >= p.top - 24f && wy <= p.doorY + 20f
                        }
                        if (hit != null) vm.goToPlace(hit) else vm.walkTo(wx, wy)
                    }
                }
        ) {
            // 화면 좌표계 배경 (변환 실패해도 빈 화면 방지)
            drawRect(Color(0xFF2A1C12), size = size)
            withTransform({
                translate(ox, oy)
                scale(s, s, Offset.Zero)
            }) {
                if (art != null) {
                    drawCustomVillageMap(art)
                } else {
                    // 에셋 로드 실패 시에도 오크헤이븐 좌표에 맞춘 임시 바닥
                    drawRect(Color(0xFF6F9A54), size = androidx.compose.ui.geometry.Size(Village.W, Village.H))
                    drawRect(Color(0xFFC2A16B), topLeft = Offset(Village.W * 0.45f, 0f), size = androidx.compose.ui.geometry.Size(Village.W * 0.1f, Village.H))
                }

                // 건물 핫스팟 힌트
                Village.places.forEach { p ->
                    drawRoundRect(
                        color = Color(0x66FFE29A),
                        topLeft = Offset(p.left, p.top),
                        size = androidx.compose.ui.geometry.Size(p.w, p.h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 3f)
                    )
                }

                party.forEachIndexed { index, mercenary ->
                    val side = if (index == 0) -1f else 1f
                    drawMercenary(
                        mercenary = mercenary,
                        x = heroX + side * 28f,
                        y = heroY + 18f + index * 8f,
                        facing = facing,
                        walking = walking,
                        phase = walkPhase + index * 0.7f,
                        scale = 0.95f,
                    )
                }
                drawHero(heroX, heroY, facing, walking, walkPhase, scale = 1.05f)
            }
        }

        Village.places.forEach { p ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (ox + p.left * s).roundToInt(),
                            (oy + (p.top - 18f) * s).roundToInt()
                        )
                    }
                    .width(with(density) { (p.w * s).toDp() }),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xCC1A120C), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = p.name,
                        color = Palette.Parchment,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        Text(
            text = if (art != null) "Oakhaven · v0.4.2" else "Oakhaven · v0.4.2 (맵 로딩 실패)",
            color = Color(0xFFFFE29A),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xCC000000), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
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
