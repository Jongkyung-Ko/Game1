package com.medieval.village.ui.map

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
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
import com.medieval.village.game.GameViewModel
import com.medieval.village.game.MenuTab
import com.medieval.village.model.SettlementId
import com.medieval.village.model.Settlements
import com.medieval.village.model.Village
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.rememberCustomArtOrNull
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 대륙 세계지도. 핀을 누르면 해당 마을/장소로 이동한다.
 */
@Composable
fun WorldMapOverlay(vm: GameViewModel, modifier: Modifier = Modifier) {
    if (vm.menuTab != MenuTab.WORLD_MAP) return

    val art = rememberCustomArtOrNull()
    val continent = art?.continentMap

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEE0E0A06))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* 배경 탭은 닫지 않음 — 핀만 이동 */ }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val wPx = with(density) { maxWidth.toPx() }
            val hPx = with(density) { maxHeight.toPx() }
            val s = min(wPx / Village.W, hPx / Village.H).coerceAtLeast(0.01f)
            val ox = (wPx - Village.W * s) / 2f
            val oy = (hPx - Village.H * s) / 2f
            val currentId = vm.currentSettlement

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(s, ox, oy) {
                        detectTapGestures { tap ->
                            val wx = (tap.x - ox) / s
                            val wy = (tap.y - oy) / s
            val hit = Settlements.all(vm.player.worldFlags).minByOrNull { st ->
                                hypot(wx - st.mapX, wy - st.mapY)
                            }
                            if (hit != null && hypot(wx - hit.mapX, wy - hit.mapY) < 70f) {
                                vm.travelToSettlement(hit.id)
                            }
                        }
                    }
            ) {
                withTransform({
                    translate(ox, oy)
                    scale(s, s, Offset.Zero)
                }) {
                    if (continent != null) {
                        drawImage(
                            image = continent,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(continent.width, continent.height),
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(Village.W.roundToInt(), Village.H.roundToInt()),
                            filterQuality = FilterQuality.Medium,
                        )
                    } else {
                        drawRect(Color(0xFF2A4A5A), size = Size(Village.W, Village.H))
                        drawRect(
                            Color(0xFF6F9A54),
                            topLeft = Offset(220f, 180f),
                            size = Size(1100f, 700f)
                        )
                    }

                    Settlements.all(vm.player.worldFlags).forEach { st ->
                        val here = st.id == currentId
                        val pinColor = when {
                            st.id == SettlementId.GRAY_CASTLE && vm.player.castleCleared ->
                                Color(0xFFE8F0FF)
                            st.id == SettlementId.IGLOO && vm.player.iglooCleared ->
                                Color(0xFFB8E0FF)
                            st.id == SettlementId.SEASIDE && vm.player.seasideCleared ->
                                Color(0xFF7EC8C8)
                            st.id == SettlementId.WINTER_CASTLE && vm.player.winterCleared ->
                                Color(0xFFF4E4C0)
                            here -> Color(0xFFFFD76A)
                            else -> Color(0xFFE85A3C)
                        }
                        val r = if (here) 22f else 18f
                        drawCircle(pinColor, radius = r, center = Offset(st.mapX, st.mapY))
                        drawCircle(
                            Color(0xFF1A120C),
                            radius = r,
                            center = Offset(st.mapX, st.mapY),
                            style = Stroke(width = 3.5f)
                        )
                        drawCircle(
                            Color.White.copy(alpha = 0.85f),
                            radius = r * 0.35f,
                            center = Offset(st.mapX, st.mapY - r * 0.15f)
                        )
                        drawIntoCanvas { canvas ->
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = android.graphics.Color.parseColor("#FFF3D6")
                                textSize = 28f
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                textAlign = Paint.Align.CENTER
                            }
                            val label = st.nameKo
                            val tw = paint.measureText(label)
                            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = android.graphics.Color.argb(0xDD, 0x1A, 0x12, 0x0C)
                            }
                            val ly = st.mapY + r + 36f
                            canvas.nativeCanvas.drawRoundRect(
                                st.mapX - tw / 2f - 12f,
                                ly - 28f,
                                st.mapX + tw / 2f + 12f,
                                ly + 8f,
                                10f,
                                10f,
                                bg
                            )
                            canvas.nativeCanvas.drawText(label, st.mapX, ly, paint)
                        }
                    }
                }
            }
        }

        Text(
            text = "세계지도 · 에메랄드 해안",
            color = Palette.Gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
                .background(Color(0xCC1A120C), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )

        Text(
            text = "마을을 눌러 이동 · 현재: ${vm.settlement.nameKo}",
            color = Palette.ParchmentDim,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .background(Color(0xCC1A120C), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Text(
            text = "닫기",
            color = Palette.Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .background(Palette.Gold, RoundedCornerShape(10.dp))
                .clickable { vm.menuTab = MenuTab.NONE }
                .padding(horizontal = 28.dp, vertical = 10.dp)
        )
    }
}
