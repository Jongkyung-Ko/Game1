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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import com.medieval.village.ui.theme.Palette
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun VillageScene(vm: GameViewModel, modifier: Modifier = Modifier) {
    val atlas = rememberKenneyAtlas()
    BoxWithConstraints(modifier.background(Color(0xFF3D6B2E))) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val s = min(wPx / Village.W, hPx / Village.H)
        val ox = (wPx - Village.W * s) / 2f
        val oy = (hPx - Village.H * s) / 2f
        val animTime = vm.animTime
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
                            wx >= p.left - 12f && wx <= p.right + 12f &&
                                wy >= p.top - 20f && wy <= p.doorY + 16f
                        }
                        if (hit != null) vm.goToPlace(hit) else vm.walkTo(wx, wy)
                    }
                }
        ) {
            // Letterbox
            drawRect(Color(0xFF2A4A22), size = size)
            withTransform({
                translate(ox, oy)
                scale(s, s, Offset.Zero)
            }) {
                // Real Kenney Tiny Town tilemap (Option A)
                drawVillageTilemap(atlas)
                drawKenneyScenery(atlas)
                Village.places.sortedBy { it.bottom }.forEach { drawKenneyPlace(atlas, it) }
                drawVillageLife(atlas, animTime)
                party.forEachIndexed { index, mercenary ->
                    val side = if (index == 0) -1f else 1f
                    drawMercenary(
                        atlas = atlas,
                        mercenary = mercenary,
                        x = heroX + side * 44f,
                        y = heroY + 36f + index * 10f,
                        facing = facing,
                        walking = walking,
                        animTime = animTime + index * 0.4f
                    )
                }
                drawHero(atlas, heroX, heroY, facing, walking, animTime)
            }
        }

        Village.places.forEach { p ->
            val hTiles = BuildingRecipes.heightTiles(p.style, p.id)
            val labelWorldY = p.bottom - WORLD_TILE * (hTiles + 0.35f)
            val labelW = BuildingRecipes.widthTiles(p.style, p.id) * WORLD_TILE
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (ox + (p.cx - labelW / 2f) * s).roundToInt(),
                            (oy + labelWorldY * s).roundToInt()
                        )
                    }
                    .width(with(density) { (labelW * s).toDp() }),
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

        // 설치 확인용 — 구 APK와 구분
        Text(
            text = "Style A · Kenney v5",
            color = Color(0xFFE8F5C8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0x99000000), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )

        val near = Village.places.firstOrNull {
            hypot(vm.heroX - it.doorX, vm.heroY - it.doorY) < 42f
        }
        if (near != null && !vm.walking) {
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
