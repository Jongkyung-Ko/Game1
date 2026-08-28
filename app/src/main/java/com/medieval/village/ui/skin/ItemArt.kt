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
 * 도안에서 잘라낸 장비·아이템 그림. 배경이 제거된 PNG 라 어떤 슬롯 위에도 얹을 수 있다.
 * 없는 아이템은 기존 Canvas 글리프로 그린다.
 */
class ItemArt(
    private val icons: Map<String, ImageBitmap>,
    val emptySlot: ImageBitmap?,
) {
    fun iconOrNull(itemId: String?): ImageBitmap? = itemId?.let { icons[it] }

    val isEmpty: Boolean get() = icons.isEmpty()

    companion object {
        private const val TAG = "ItemArt"
        private const val DIR = "items"

        fun loadOrNull(context: Context): ItemArt? = try {
            val names = context.assets.list(DIR)?.filter { it.endsWith(".png") } ?: emptyList()
            val icons = names
                .filter { it != "slot_empty.png" }
                .mapNotNull { file ->
                    decode(context, file)?.let { file.removeSuffix(".png") to it }
                }
                .toMap()
            if (icons.isEmpty()) null else ItemArt(icons, decode(context, "slot_empty.png"))
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load item art", t)
            null
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
fun rememberItemArt(): ItemArt? {
    val context = LocalContext.current
    return remember(context) { ItemArt.loadOrNull(context) }
}
