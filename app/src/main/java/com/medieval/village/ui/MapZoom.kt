package com.medieval.village.ui

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput

/** 핀치 확대/축소 + 확대 시 팬 */
class MapZoomState(
    minZoom: Float = 1f,
    maxZoom: Float = 2.6f,
) {
    var zoom by mutableFloatStateOf(1f)
        private set
    var pan by mutableStateOf(Offset.Zero)
        private set

    private val minZ = minZoom
    private val maxZ = maxZoom

    fun apply(centroid: Offset, panChange: Offset, zoomChange: Float, viewSize: Size) {
        val oldZoom = zoom
        val newZoom = (oldZoom * zoomChange).coerceIn(minZ, maxZ)
        if (newZoom <= 1.01f) {
            zoom = 1f
            pan = Offset.Zero
            return
        }
        // 핀치 중심 기준으로 확대되도록 팬 보정
        if (zoomChange != 1f && oldZoom > 0f) {
            val c = Offset(viewSize.width / 2f, viewSize.height / 2f)
            val focus = centroid - pan
            val world = c + (focus - c) / oldZoom
            val newFocus = c + (world - c) * newZoom
            pan = centroid - newFocus
        }
        zoom = newZoom
        pan += panChange
        clampPan(viewSize)
    }

    private fun clampPan(viewSize: Size) {
        val maxX = viewSize.width * (zoom - 1f) * 0.55f
        val maxY = viewSize.height * (zoom - 1f) * 0.55f
        pan = Offset(
            pan.x.coerceIn(-maxX, maxX),
            pan.y.coerceIn(-maxY, maxY),
        )
    }

    /** 화면 좌표 → 줌/팬 적용 전 캔버스 좌표 */
    fun screenToContent(screen: Offset, viewSize: Size): Offset {
        val c = Offset(viewSize.width / 2f, viewSize.height / 2f)
        val afterPan = screen - pan
        return c + (afterPan - c) / zoom
    }

    fun reset() {
        zoom = 1f
        pan = Offset.Zero
    }
}

@Composable
fun rememberMapZoomState(minZoom: Float = 1f, maxZoom: Float = 2.6f): MapZoomState {
    return remember { MapZoomState(minZoom, maxZoom) }
}

fun Modifier.mapZoomGestures(state: MapZoomState): Modifier =
    this.pointerInput(state) {
        detectTransformGestures { centroid, pan, zoom, _ ->
            state.apply(centroid, pan, zoom, Size(size.width.toFloat(), size.height.toFloat()))
        }
    }

/** 던전 (+) 확대 플레이 배율 — 타일과 캐릭터가 크게 보이고 카메라는 주인공을 따라간다. */
const val DUNGEON_ENLARGE_ZOOM = 2.25f

fun dungeonPlayZoom(enlarged: Boolean): Float = if (enlarged) DUNGEON_ENLARGE_ZOOM else 1f

fun DrawScope.withMapZoom(state: MapZoomState, block: DrawScope.() -> Unit) {
    val pivot = Offset(size.width / 2f, size.height / 2f)
    withTransform({
        translate(left = state.pan.x, top = state.pan.y)
        scale(scaleX = state.zoom, scaleY = state.zoom, pivot = pivot)
    }, block)
}
