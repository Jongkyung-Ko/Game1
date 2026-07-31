package com.medieval.village.ui.village

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.BitmapFactory
import kotlin.math.roundToInt

/** Kenney Tiny Town / Tiny Dungeon packed sheets (16px tiles, 1px spacing, 12 columns). */
class KenneyAtlas(
    val town: ImageBitmap,
    val dungeon: ImageBitmap,
) {
    companion object {
        const val TILE = 16
        const val STRIDE = 17 // 16 + 1px gap
        const val COLS = 12

        fun load(context: Context): KenneyAtlas {
            fun loadAsset(path: String): ImageBitmap {
                context.assets.open(path).use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                        ?: error("Failed to decode $path")
                    return bmp.asImageBitmap()
                }
            }
            return KenneyAtlas(
                town = loadAsset("kenney/tiny_town.png"),
                dungeon = loadAsset("kenney/tiny_dungeon.png"),
            )
        }
    }
}

@Composable
fun rememberKenneyAtlas(): KenneyAtlas {
    val context = LocalContext.current
    return remember(context) { KenneyAtlas.load(context) }
}

fun DrawScope.drawKenneyTile(
    sheet: ImageBitmap,
    tileId: Int,
    x: Float,
    y: Float,
    size: Float,
    mirrorX: Boolean = false,
) {
    if (tileId < 0) return
    val col = tileId % KenneyAtlas.COLS
    val row = tileId / KenneyAtlas.COLS
    val srcX = col * KenneyAtlas.STRIDE
    val srcY = row * KenneyAtlas.STRIDE
    val dst = size.roundToInt().coerceAtLeast(1)
    if (mirrorX) {
        translate(x + size, y) {
            scale(-1f, 1f, pivot = Offset.Zero) {
                drawImage(
                    image = sheet,
                    srcOffset = IntOffset(srcX, srcY),
                    srcSize = IntSize(KenneyAtlas.TILE, KenneyAtlas.TILE),
                    dstOffset = IntOffset(0, 0),
                    dstSize = IntSize(dst, dst),
                    filterQuality = FilterQuality.None,
                )
            }
        }
    } else {
        drawImage(
            image = sheet,
            srcOffset = IntOffset(srcX, srcY),
            srcSize = IntSize(KenneyAtlas.TILE, KenneyAtlas.TILE),
            dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
            dstSize = IntSize(dst, dst),
            filterQuality = FilterQuality.None,
        )
    }
}

/** Draw a character/creature tile centered on (cx, footY), with optional bob. */
fun DrawScope.drawKenneySprite(
    sheet: ImageBitmap,
    tileId: Int,
    cx: Float,
    footY: Float,
    size: Float,
    bob: Float = 0f,
    mirrorX: Boolean = false,
) {
    drawKenneyTile(sheet, tileId, cx - size / 2f, footY - size + bob, size, mirrorX)
}

/** Tiny Town tile IDs (from Kenney packed sheet, 12 cols) */
object TownTiles {
    const val GRASS = 0
    const val GRASS_TUFT = 1
    const val GRASS_FLOWER = 2
    const val TREE_ORANGE = 3
    const val TREE_GREEN = 4
    const val BUSH = 5
    const val PATH_TL = 12
    const val PATH_T = 13
    const val PATH_TR = 14
    const val PATH_L = 24
    const val PATH = 25
    const val PATH_R = 26
    const val PATH_BL = 36
    const val PATH_B = 37
    const val PATH_BR = 38
    const val MUSHROOM = 29
    const val ROOF_BLUE_L = 48
    const val ROOF_BLUE_M = 49
    const val ROOF_BLUE_R = 50
    const val ROOF_RED_L = 52
    const val ROOF_RED_M = 53
    const val ROOF_RED_R = 54
    const val FENCE_H = 56
    const val FENCE_V = 57
    const val WALL_WOOD_WIN = 84
    const val WALL_WOOD_DOOR = 85
    const val WALL_WOOD_M = 86
    const val WALL_WOOD_R = 87
    const val WALL_STONE_WIN = 88
    const val WALL_STONE_DOOR = 89
    const val WALL_STONE_M = 90
    const val WALL_STONE_R = 91
    const val ALCOVE = 92
    const val SACK = 93
    const val BASKET = 94
    const val TARGET = 95
    const val CASTLE_TL = 96
    const val CASTLE_TM = 97
    const val CASTLE_TR = 98
    const val CASTLE_BL = 108
    const val CASTLE_BM = 109
    const val CASTLE_BR = 110
    const val WELL = 92
    const val BARREL = 93
    const val CRATE = 94
    // aliases used by house builder
    const val WALL_TAN_L = 84
    const val WALL_TAN_M = 86
    const val WALL_TAN_WIN = 84
    const val WALL_TAN_R = 87
    const val WALL_GREY_L = 88
    const val WALL_GREY_M = 90
    const val WALL_GREY_WIN = 88
    const val WALL_GREY_R = 91
    const val STONE_A = 48
    const val STONE_B = 49
    const val STONE_C = 50
    const val SIGN = 82
}

/** Tiny Dungeon character / creature tile IDs */
object DungeonTiles {
    const val FLOOR = 0
    const val WALL = 1
    const val TABLE = 72
    const val CHEST = 74
    const val MAGE = 84
    const val VILLAGER = 85
    const val HOODED = 86
    const val ELDER = 87
    const val YOUTH = 88
    const val KNIGHT_BLUE = 96
    const val KNIGHT_GOLD = 97
    const val KNIGHT_RED = 98
    const val WOMAN = 99
    const val HERO = 100
    const val BEAST = 108
    const val SKULL = 109
    const val CRAB = 110
    const val FURRY = 111
    const val PUP = 112
    // aliases for village life
    const val SLIME = BEAST
    const val BAT = CRAB
    const val SPIDER = FURRY
    const val BLOB = PUP
}
