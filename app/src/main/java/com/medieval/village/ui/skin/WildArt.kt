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
 * 숲·사막·빙하 지대의 지형 타일. 던전과 같은 방식으로 도안에서 잘라낸 그림이다.
 * 상자·포털은 던전 것과 같은 물건이라 [DungeonArt] 쪽을 함께 쓴다.
 */
class WildArt(
    private val grounds: List<ImageBitmap>,
    val scenery: ImageBitmap?,
    val obstacleA: ImageBitmap?,
    val obstacleB: ImageBitmap?,
    val exit: ImageBitmap?,
    val deeper: ImageBitmap?,
) {
    fun groundFor(col: Int, row: Int): ImageBitmap {
        if (grounds.size == 1) return grounds[0]
        val h = (col * 73856093) xor (row * 19349663)
        val pick = ((h ushr 3) and 0xFF) % 100
        val index = when {
            pick < 54 -> 0
            pick < 82 -> 1
            else -> 2
        }
        return grounds[index.coerceAtMost(grounds.lastIndex)]
    }

    /** 장애물은 두 종류를 섞어 배치한다. */
    fun obstacleFor(col: Int, row: Int): ImageBitmap? {
        val a = obstacleA
        val b = obstacleB ?: return a
        if (a == null) return b
        val h = (col * 40503) xor (row * 12289)
        return if (((h ushr 2) and 0x3) == 0) b else a
    }

    companion object {
        private const val TAG = "WildArt"

        fun loadOrNull(context: Context, dir: String): WildArt? {
            val first = decode(context, dir, "ground_a.png") ?: return null
            val grounds = listOfNotNull(
                first,
                decode(context, dir, "ground_b.png"),
                decode(context, dir, "ground_c.png"),
            )
            return WildArt(
                grounds = grounds,
                scenery = decode(context, dir, "scenery.png"),
                obstacleA = decode(context, dir, "obstacle_a.png"),
                obstacleB = decode(context, dir, "obstacle_b.png"),
                exit = decode(context, dir, "exit.png"),
                deeper = decode(context, dir, "deeper.png"),
            )
        }

        private fun decode(context: Context, dir: String, name: String): ImageBitmap? = try {
            val bytes = context.assets.open("$dir/$name").use { it.readBytes() }
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.asImageBitmap()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to decode $dir/$name", t)
            null
        }
    }
}

@Composable
fun rememberWildArt(dir: String): WildArt? {
    val context = LocalContext.current
    return remember(context, dir) { WildArt.loadOrNull(context, dir) }
}
