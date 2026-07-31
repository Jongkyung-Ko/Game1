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
import com.medieval.village.model.BuildingStyle
import com.medieval.village.model.PlaceId
import kotlin.math.roundToInt

/** Kenney Tiny Town / Tiny Dungeon — packed sheets + precomposed building sprites. */
class KenneyAtlas(
    val town: ImageBitmap,
    val dungeon: ImageBitmap,
    private val sprites: Map<String, ImageBitmap>,
) {
    fun sprite(name: String): ImageBitmap =
        sprites[name] ?: error("Missing kenney sprite: $name")

    companion object {
        const val TILE = 16
        const val STRIDE = 17
        const val COLS = 12
        /** Precomposed sprites were exported at 8× native tile size. */
        const val SPRITE_SCALE = 8

        private val SPRITE_NAMES = listOf(
            "house_red", "house_red5", "inn", "shop", "pub",
            "house_blue", "clinic", "forge", "tower", "church", "armory",
            "cave", "camp", "arena",
            "tree_g", "tree_o", "bush", "well", "mushroom", "sign", "crate", "basket", "target",
            "hero", "knight_b", "knight_g", "knight_r", "mage", "villager", "woman", "elder", "youth",
            "critter_a", "critter_b", "critter_c",
        )

        fun load(context: Context): KenneyAtlas {
            fun loadAsset(path: String): ImageBitmap {
                context.assets.open(path).use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                        ?: error("Failed to decode $path")
                    return bmp.asImageBitmap()
                }
            }
            val sprites = SPRITE_NAMES.associateWith { name ->
                loadAsset("kenney/sprites/$name.png")
            }
            return KenneyAtlas(
                town = loadAsset("kenney/tiny_town.png"),
                dungeon = loadAsset("kenney/tiny_dungeon.png"),
                sprites = sprites,
            )
        }

        fun buildingSprite(style: BuildingStyle, id: PlaceId): String = when (style) {
            BuildingStyle.CHURCH -> "church"
            BuildingStyle.TOWER -> "tower"
            BuildingStyle.CAVE -> "cave"
            BuildingStyle.ARENA -> "arena"
            BuildingStyle.CAMP -> "camp"
            BuildingStyle.FORGE -> "forge"
            BuildingStyle.STORE -> "shop"
            BuildingStyle.INN -> "inn"
            BuildingStyle.PUB -> "pub"
            BuildingStyle.CLINIC -> "clinic"
            BuildingStyle.ARMORY -> "armory"
            BuildingStyle.HOUSE -> "house_red"
            else -> when (id) {
                PlaceId.HOME -> "house_red5"
                else -> "house_red"
            }
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

/** Draw a precomposed PNG sprite with bottom-center at (cx, footY). */
fun DrawScope.drawKenneySpriteAsset(
    image: ImageBitmap,
    cx: Float,
    footY: Float,
    worldHeight: Float,
    bob: Float = 0f,
    mirrorX: Boolean = false,
) {
    val aspect = image.width.toFloat() / image.height.toFloat()
    val h = worldHeight
    val w = h * aspect
    val left = cx - w / 2f
    val top = footY - h + bob
    val dw = w.roundToInt().coerceAtLeast(1)
    val dh = h.roundToInt().coerceAtLeast(1)
    if (mirrorX) {
        translate(left + w, top) {
            scale(-1f, 1f, pivot = Offset.Zero) {
                drawImage(
                    image = image,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(image.width, image.height),
                    dstOffset = IntOffset(0, 0),
                    dstSize = IntSize(dw, dh),
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
            filterQuality = FilterQuality.None,
        )
    }
}

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

object TownTiles {
    const val GRASS = 0
    const val GRASS_TUFT = 1
    const val GRASS_FLOWER = 2
    const val PATH_TL = 12
    const val PATH_T = 13
    const val PATH_TR = 14
    const val PATH_L = 24
    const val PATH = 25
    const val PATH_R = 26
    const val PATH_BL = 36
    const val PATH_B = 37
    const val PATH_BR = 38
}

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
}
