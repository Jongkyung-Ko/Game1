package com.medieval.village.ui.skin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

/**
 * 도안(고전 UI 시안)에서 잘라낸 목재·양피지·금테 텍스처.
 * 로딩에 실패하면 각 위젯이 코드로 그린 폴백으로 되돌아간다.
 */
class UiSkin(
    val buttonWood: ImageBitmap?,
    val buttonArcane: ImageBitmap?,
    val plaque: ImageBitmap?,
    val scroll: ImageBitmap?,
    val logScroll: ImageBitmap?,
    val infoStrip: ImageBitmap?,
    val barFrame: ImageBitmap?,
    val dpad: ImageBitmap?,
    val roundButton: ImageBitmap?,
) {
    companion object {
        private const val TAG = "UiSkin"
        private const val DIR = "ui"

        fun loadOrNull(context: Context): UiSkin? {
            val skin = UiSkin(
                buttonWood = decode(context, "btn_wood.png"),
                buttonArcane = decode(context, "btn_arcane.png"),
                plaque = decode(context, "plaque.png"),
                scroll = decode(context, "scroll.png"),
                logScroll = decode(context, "log_scroll.png"),
                infoStrip = decode(context, "info_strip.png"),
                barFrame = decode(context, "bar_frame.png"),
                dpad = decode(context, "dpad.png"),
                roundButton = decode(context, "btn_round.png"),
            )
            return if (skin.buttonWood == null) null else skin
        }

        private fun decode(context: Context, name: String): ImageBitmap? = try {
            val bytes = context.assets.open("$DIR/$name").use { it.readBytes() }
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.asImageBitmap()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to decode $name", t)
            null
        }
    }
}

@Composable
fun rememberUiSkin(): UiSkin? {
    val context = LocalContext.current
    return remember(context) { UiSkin.loadOrNull(context) }
}

/**
 * 각 텍스처의 고정 테두리 두께(px, 원본 기준).
 * 가운데만 늘어나고 모서리 장식은 그대로 유지된다.
 */
object SkinInsets {
    val Button = NineSliceInsets(30, 26, 30, 26)
    val Arcane = NineSliceInsets(40, 44, 40, 44)
    val Plaque = NineSliceInsets(72, 15, 22, 15)
    val Scroll = NineSliceInsets(22, 54, 22, 58)
    val LogScroll = NineSliceInsets(24, 32, 24, 42)
    val InfoStrip = NineSliceInsets(60, 10, 60, 10)
    val BarFrame = NineSliceInsets(24, 7, 28, 7)
}
