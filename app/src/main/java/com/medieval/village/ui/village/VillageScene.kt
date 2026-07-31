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
    val art = rememberCustomArt()
    BoxWithConstraints(modifier.background(Color(0xFF1A140E))) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val s = min(wPx / Village.W, hPx / Village.H)
        val ox = (wPx - Village.W * s) / 2f
        val oy = (hPx - Village.H * s) / 2f
        val heroX = vm.heroX
        val heroY = vm.heroY
        val facing = vm.facing
        val walking = vm.walking
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
            drawRect(Color(0xFF120E0A), size = size)
            withTransform({
                translate(ox, oy)
                scale(s, s, Offset.Zero)
            }) {
                // 직접 그린 마을 일러스트 (Style B)
                drawCustomVillageMap(art)

                // 건물 핫스팟 힌트 (얇은 테두리)
                Village.places.forEach { p ->
                    drawRoundRect(
                        color = Color(0x55FFF3C4),
                        topLeft = Offset(p.left, p.top),
                        size = androidx.compose.ui.geometry.Size(p.w, p.h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = 2f)
                    )
                }

                party.forEachIndexed { index, mercenary ->
                    val side = if (index == 0) -1f else 1f
                    drawMercenary(
                        art = art,
                        mercenary = mercenary,
                        x = heroX + side * 28f,
                        y = heroY + 18f + index * 8f,
                        facing = facing,
                    )
                }
                drawCustomHero(art, heroX, heroY, facing, worldHeight = 78f)
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
            text = "Style B · Roster v7",
            color = Color(0xFFFFE29A),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0x99000000), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
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
