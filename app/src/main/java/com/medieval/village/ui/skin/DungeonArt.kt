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
 * 도안에서 잘라낸 던전 지형·소품 텍스처.
 * 하나라도 없으면 null 을 돌려주고 화면은 기존 페인티드 렌더러로 되돌아간다.
 */
class DungeonArt(
    private val floors: List<ImageBitmap>,
    val wall: ImageBitmap,
    val stairsUp: ImageBitmap?,
    val stairsDown: ImageBitmap?,
    val chestClosed: ImageBitmap?,
    val chestOpen: ImageBitmap?,
    val portal: ImageBitmap?,
    val sewerGrate: ImageBitmap?,
) {
    /** 같은 칸은 늘 같은 무늬가 나오도록 좌표 해시로 고른다. */
    fun floorFor(col: Int, row: Int): ImageBitmap {
        if (floors.size == 1) return floors[0]
        val h = (col * 73856093) xor (row * 19349663)
        val pick = ((h ushr 3) and 0xFF) % 100
        val index = when {
            pick < 62 -> 0
            pick < 86 -> 1
            else -> 2
        }
        return floors[index.coerceAtMost(floors.lastIndex)]
    }

    companion object {
        private const val TAG = "DungeonArt"
        private const val DIR = "dungeon"

        fun loadOrNull(context: Context): DungeonArt? {
            val plain = decode(context, "floor_plain.png") ?: return null
            val wall = decode(context, "wall_stone.png") ?: return null
            val floors = listOfNotNull(
                plain,
                decode(context, "floor_cracked.png"),
                decode(context, "floor_mossy.png"),
            )
            return DungeonArt(
                floors = floors,
                wall = wall,
                stairsUp = decode(context, "stairs_up.png"),
                stairsDown = decode(context, "stairs_down.png"),
                chestClosed = decode(context, "chest_closed.png"),
                chestOpen = decode(context, "chest_open.png"),
                portal = decode(context, "portal.png"),
                sewerGrate = decode(context, "sewer_grate.png"),
            )
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
fun rememberDungeonArt(): DungeonArt? {
    val context = LocalContext.current
    return remember(context) { DungeonArt.loadOrNull(context) }
}
