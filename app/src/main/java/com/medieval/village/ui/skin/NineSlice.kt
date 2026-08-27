package com.medieval.village.ui.skin

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/** 원본 텍스처에서 늘이지 않을 테두리 두께(px). */
data class NineSliceInsets(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

/**
 * 9분할로 텍스처를 그린다. 네 모서리는 원본 크기를 유지하고 변과 중앙만 늘어난다.
 * [drawCenter] 를 끄면 액자처럼 가운데가 비어 아래 내용이 비쳐 보인다.
 */
fun DrawScope.drawNineSlice(
    image: ImageBitmap,
    insets: NineSliceInsets,
    width: Float = size.width,
    height: Float = size.height,
    drawCenter: Boolean = true,
) {
    val iw = image.width
    val ih = image.height
    // 대상이 테두리보다 좁으면 테두리를 비례 축소해 겹치지 않게 한다
    val shrinkX = minOf(1f, width / (insets.left + insets.right).coerceAtLeast(1).toFloat())
    val shrinkY = minOf(1f, height / (insets.top + insets.bottom).coerceAtLeast(1).toFloat())
    val dl = (insets.left * shrinkX).roundToInt()
    val dr = (insets.right * shrinkX).roundToInt()
    val dt = (insets.top * shrinkY).roundToInt()
    val db = (insets.bottom * shrinkY).roundToInt()

    val dw = width.roundToInt()
    val dh = height.roundToInt()
    val midSrcW = (iw - insets.left - insets.right).coerceAtLeast(1)
    val midSrcH = (ih - insets.top - insets.bottom).coerceAtLeast(1)
    val midDstW = (dw - dl - dr).coerceAtLeast(0)
    val midDstH = (dh - dt - db).coerceAtLeast(0)

    fun piece(sx: Int, sy: Int, sw: Int, sh: Int, dx: Int, dy: Int, dwp: Int, dhp: Int) {
        if (sw <= 0 || sh <= 0 || dwp <= 0 || dhp <= 0) return
        drawImage(
            image = image,
            srcOffset = IntOffset(sx, sy),
            srcSize = IntSize(sw, sh),
            dstOffset = IntOffset(dx, dy),
            dstSize = IntSize(dwp, dhp),
            filterQuality = FilterQuality.Medium,
        )
    }

    // 모서리
    piece(0, 0, insets.left, insets.top, 0, 0, dl, dt)
    piece(iw - insets.right, 0, insets.right, insets.top, dw - dr, 0, dr, dt)
    piece(0, ih - insets.bottom, insets.left, insets.bottom, 0, dh - db, dl, db)
    piece(iw - insets.right, ih - insets.bottom, insets.right, insets.bottom, dw - dr, dh - db, dr, db)
    // 변
    piece(insets.left, 0, midSrcW, insets.top, dl, 0, midDstW, dt)
    piece(insets.left, ih - insets.bottom, midSrcW, insets.bottom, dl, dh - db, midDstW, db)
    piece(0, insets.top, insets.left, midSrcH, 0, dt, dl, midDstH)
    piece(iw - insets.right, insets.top, insets.right, midSrcH, dw - dr, dt, dr, midDstH)
    // 중앙
    if (drawCenter) {
        piece(insets.left, insets.top, midSrcW, midSrcH, dl, dt, midDstW, midDstH)
    }
}

/** 위젯 배경으로 9분할 텍스처를 깐다. */
fun Modifier.nineSliceBackground(
    image: ImageBitmap?,
    insets: NineSliceInsets,
): Modifier = if (image == null) this else drawBehind {
    drawNineSlice(image, insets)
}
