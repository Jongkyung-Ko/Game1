package com.medieval.village.ui.village

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.medieval.village.game.Facing
import com.medieval.village.model.Village
import kotlin.math.roundToInt

/** 커스텀 마을 맵 + 캐릭터 스프라이트. 로딩 실패 시 null을 허용한다. */
class CustomArt(
    val villageMap: ImageBitmap,
    private val chars: Map<String, ImageBitmap>,
) {
    fun charOrNull(name: String): ImageBitmap? = chars[name]

    fun npcSpriteOrNull(key: String): ImageBitmap? = charOrNull(key)

    fun zombieSpriteOrNull(kind: String): ImageBitmap? = when (kind) {
        "shambler" -> charOrNull("zombie_shambler")
        "runner" -> charOrNull("zombie_runner")
        "bloater" -> charOrNull("zombie_bloater")
        "armored" -> charOrNull("zombie_armored")
        "blacksmith" -> charOrNull("zombie_blacksmith")
        "farmer" -> charOrNull("zombie_farmer")
        "golem" -> charOrNull("golem_teacher")
        else -> charOrNull("zombie_shambler")
    }

    companion object {
        private const val TAG = "CustomArt"

        @Volatile
        private var cached: CustomArt? = null

        private val CHAR_NAMES = listOf(
            "warrior", "rogue", "mage", "paladin",
            "merchant", "shopkeeper", "blacksmith", "doctor",
            "farmer", "teacher", "chef",
            "zombie_shambler", "zombie_runner", "zombie_bloater", "zombie_armored",
            "zombie_blacksmith", "zombie_farmer", "golem_teacher",
        )

        fun loadOrNull(context: Context): CustomArt? {
            cached?.let { return it }
            return try {
                synchronized(this) {
                    cached?.let { return it }
                    val app = context.applicationContext
                    val village = loadVillageMap(app) ?: return null
                    val chars = LinkedHashMap<String, ImageBitmap>()
                    CHAR_NAMES.forEach { name ->
                        loadAsset(app, "custom/chars/$name.png", cleanEdges = true)?.let {
                            chars[name] = it
                        }
                    }
                    val art = CustomArt(villageMap = village, chars = chars)
                    cached = art
                    art
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load custom art", t)
                null
            }
        }

        private fun loadVillageMap(context: Context): ImageBitmap? {
            // oakhaven_base 우선, 실패 시 village_map
            return loadAsset(context, "custom/oakhaven_base.png", cleanEdges = false)
                ?: loadAsset(context, "custom/village_map.png", cleanEdges = false)
        }

        private fun loadAsset(context: Context, path: String, cleanEdges: Boolean): ImageBitmap? {
            return try {
                val bytes = context.assets.open(path).use { it.readBytes() }
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inMutable = cleanEdges
                    inPremultiplied = true
                }
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                    ?: return null
                val bmp = if (cleanEdges) {
                    val mutable = if (decoded.isMutable) {
                        decoded
                    } else {
                        decoded.copy(Bitmap.Config.ARGB_8888, true).also { decoded.recycle() }
                    }
                    hardenSpriteAlpha(mutable)
                    mutable
                } else {
                    decoded
                }
                bmp.asImageBitmap()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to decode $path", t)
                null
            }
        }

        private fun hardenSpriteAlpha(bmp: Bitmap) {
            if (!bmp.isMutable) return
            val w = bmp.width
            val h = bmp.height
            val px = IntArray(w * h)
            bmp.getPixels(px, 0, w, 0, 0, w, h)
            for (i in px.indices) {
                val c = px[i]
                val a = c ushr 24
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val nearWhite = r >= 236 && g >= 236 && b >= 236
                px[i] = when {
                    a < 16 || nearWhite -> c and 0x00FFFFFF
                    else -> c or 0xFF000000.toInt()
                }
            }
            bmp.setPixels(px, 0, w, 0, 0, w, h)
        }
    }
}

@Composable
fun rememberCustomArtOrNull(): CustomArt? {
    val context = LocalContext.current
    return remember(context) { CustomArt.loadOrNull(context) }
}

/** 하위 호환: 실패하면 예외 대신 null 경로를 쓰도록 화면 쪽에서 처리한다. */
@Composable
fun rememberCustomArt(): CustomArt {
    return rememberCustomArtOrNull()
        ?: error("Custom art assets missing — rebuild APK with assets/custom")
}

fun DrawScope.drawCustomVillageMap(art: CustomArt) {
    drawImage(
        image = art.villageMap,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(art.villageMap.width, art.villageMap.height),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(Village.W.roundToInt(), Village.H.roundToInt()),
        filterQuality = FilterQuality.Medium,
    )
}

fun DrawScope.drawCustomSprite(
    image: ImageBitmap,
    cx: Float,
    footY: Float,
    worldHeight: Float,
    mirrorX: Boolean = false,
) {
    val aspect = image.width.toFloat() / image.height.toFloat().coerceAtLeast(1f)
    val h = worldHeight
    val w = h * aspect
    val left = cx - w / 2f
    val top = footY - h
    val dw = w.roundToInt().coerceAtLeast(1)
    val dh = h.roundToInt().coerceAtLeast(1)
    if (mirrorX) {
        translate(left + w, top) {
            scale(-1f, 1f, pivot = Offset.Zero) {
                drawImage(
                    image = image,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(image.width, image.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(dw, dh),
                    alpha = 1f,
                    filterQuality = FilterQuality.None,
                )
            }
        }
    } else {
        drawImage(
            image = image,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(image.width, image.height),
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(dw, dh),
            alpha = 1f,
            filterQuality = FilterQuality.None,
        )
    }
}

fun DrawScope.drawCustomHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    worldHeight: Float = 72f,
) {
    val sprite = art.charOrNull("warrior") ?: return
    drawCustomSprite(
        image = sprite,
        cx = x,
        footY = y,
        worldHeight = worldHeight,
        mirrorX = facing == Facing.LEFT,
    )
}
