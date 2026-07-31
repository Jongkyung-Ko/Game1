package com.medieval.village.ui.village

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.medieval.village.game.Facing
import com.medieval.village.model.Village
import kotlin.math.roundToInt

/** 직접 그린 마을 맵 + 주인공 정지 스프라이트 (걷기 프레임 제외). */
class CustomArt(
    val villageMap: ImageBitmap,
    val heroFront: ImageBitmap,
    val heroBack: ImageBitmap,
    val heroSide: ImageBitmap,
    val heroPortrait: ImageBitmap,
) {
    fun heroFor(facing: Facing): ImageBitmap = when (facing) {
        Facing.DOWN -> heroFront
        Facing.UP -> heroBack
        Facing.LEFT, Facing.RIGHT -> heroSide
    }

    companion object {
        fun load(context: Context): CustomArt {
            fun loadAsset(path: String): ImageBitmap {
                context.assets.open(path).use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                        ?: error("Failed to decode $path")
                    return bmp.asImageBitmap()
                }
            }
            return CustomArt(
                villageMap = loadAsset("custom/village_map.png"),
                heroFront = loadAsset("custom/hero_front.png"),
                heroBack = loadAsset("custom/hero_back.png"),
                heroSide = loadAsset("custom/hero_side.png"),
                heroPortrait = loadAsset("custom/hero_portrait.png"),
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

/** 걷기 프레임 없이 방향별 정지 스프라이트만 사용. */
fun DrawScope.drawCustomHero(
    art: CustomArt,
    x: Float,
    y: Float,
    facing: Facing,
    worldHeight: Float = 72f,
) {
    val image = art.heroFor(facing)
    val aspect = image.width.toFloat() / image.height.toFloat()
    val h = worldHeight
    val w = h * aspect
    val left = x - w / 2f
    val top = y - h
    val dw = w.roundToInt().coerceAtLeast(1)
    val dh = h.roundToInt().coerceAtLeast(1)
    val mirror = facing == Facing.LEFT
    if (mirror) {
        translate(left + w, top) {
            scale(-1f, 1f, pivot = Offset.Zero) {
                drawImage(
                    image = image,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(image.width, image.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(dw, dh),
                    filterQuality = FilterQuality.Medium,
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
            filterQuality = FilterQuality.Medium,
        )
    }
}
