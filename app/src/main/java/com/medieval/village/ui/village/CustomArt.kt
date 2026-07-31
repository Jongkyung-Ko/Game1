package com.medieval.village.ui.village

import android.content.Context
import android.graphics.BitmapFactory
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

/** 커스텀 마을 맵 + 캐릭터 시트 스프라이트. */
class CustomArt(
    val villageMap: ImageBitmap,
    private val chars: Map<String, ImageBitmap>,
) {
    fun char(name: String): ImageBitmap =
        chars[name] ?: error("Missing custom char: $name")

    fun heroSprite(): ImageBitmap = char("warrior")

    fun mercSprite(role: String): ImageBitmap = when (role) {
        "전사" -> char("warrior")
        "도적" -> char("rogue")
        "성기사" -> char("paladin")
        "마법사" -> char("mage")
        else -> char("warrior")
    }

    fun npcSprite(key: String): ImageBitmap = char(key)

    fun zombieSprite(kind: String): ImageBitmap = when (kind) {
        "shambler" -> char("zombie_shambler")
        "runner" -> char("zombie_runner")
        "bloater" -> char("zombie_bloater")
        "armored" -> char("zombie_armored")
        "blacksmith" -> char("zombie_blacksmith")
        "farmer" -> char("zombie_farmer")
        "golem" -> char("golem_teacher")
        else -> char("zombie_shambler")
    }

    companion object {
        private val CHAR_NAMES = listOf(
            "warrior", "rogue", "mage", "paladin",
            "merchant", "shopkeeper", "blacksmith", "doctor",
            "farmer", "teacher", "chef",
            "zombie_shambler", "zombie_runner", "zombie_bloater", "zombie_armored",
            "zombie_blacksmith", "zombie_farmer", "golem_teacher",
        )

        fun load(context: Context): CustomArt {
            fun loadAsset(path: String): ImageBitmap {
                context.assets.open(path).use { stream ->
                    val opts = BitmapFactory.Options().apply {
                        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                        inPremultiplied = false
                    }
                    val bmp = BitmapFactory.decodeStream(stream, null, opts)
                        ?: error("Failed to decode $path")
                    bmp.setHasAlpha(true)
                    // 반투명/잔광 제거: 알파를 0 또는 255로 고정하고 밝은 가장자리 픽셀 제거
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
                    return bmp.asImageBitmap()
                }
            }
            val chars = CHAR_NAMES.associateWith { name ->
                loadAsset("custom/chars/$name.png")
            }
            return CustomArt(
                villageMap = loadAsset("custom/village_map.png"),
                chars = chars,
            )
        }
    }
}

@Composable
fun rememberCustomArt(): CustomArt {
    val context = LocalContext.current
    return remember(context) { CustomArt.load(context) }
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

/** 주인공: 전사 정지 스프라이트 (걷기 프레임 없음, 좌측만 반전). */
fun DrawScope.drawCustomHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    worldHeight: Float = 72f,
) {
    drawCustomSprite(
        image = art.heroSprite(),
        cx = x,
        footY = y,
        worldHeight = worldHeight,
        mirrorX = facing == Facing.LEFT,
    )
}
