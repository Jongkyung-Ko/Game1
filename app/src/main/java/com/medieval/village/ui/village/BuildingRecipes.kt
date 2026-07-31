package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.BuildingStyle
import com.medieval.village.model.Place
import com.medieval.village.model.PlaceId

/**
 * Tiny Town Sample 스타일: 지붕 1줄 + 벽/문 1줄 (필요 시 첨탑 1줄).
 * 타일을 장소 크기에 늘리지 않고 WORLD_TILE 그리드에 맞춰 그린다.
 */
object BuildingRecipes {
    /** row-major tile ids; null = skip */
    fun tiles(style: BuildingStyle, id: PlaceId): Array<Array<Int?>> = when (style) {
        BuildingStyle.CHURCH -> arrayOf(
            arrayOf(48, 49, 50, 51),
            arrayOf(60, 61, 62, 61),
            arrayOf(76, 84, 85, 76),
        )
        BuildingStyle.TOWER -> arrayOf(
            arrayOf(49, 51, 49),
            arrayOf(76, 78, 76),
            arrayOf(76, 77, 76),
        )
        BuildingStyle.CAVE -> arrayOf(
            arrayOf(77, 77, 77),
            arrayOf(76, 78, 76),
        )
        BuildingStyle.ARENA -> arrayOf(
            arrayOf(40, 41, 40),
            arrayOf(null, 95, null),
        )
        BuildingStyle.CAMP -> arrayOf(
            arrayOf(63, 65, 67),
            arrayOf(44, 85, 46),
        )
        BuildingStyle.FORGE -> arrayOf(
            arrayOf(60, 61, 62),
            arrayOf(76, 85, 76),
        )
        BuildingStyle.STORE -> arrayOf(
            arrayOf(52, 53, 54),
            arrayOf(72, 86, 87),
        )
        BuildingStyle.INN -> arrayOf(
            arrayOf(52, 53, 54, 55),
            arrayOf(72, 84, 85, 72),
        )
        BuildingStyle.PUB -> arrayOf(
            arrayOf(64, 65, 66),
            arrayOf(72, 85, 72),
        )
        BuildingStyle.CLINIC -> arrayOf(
            arrayOf(48, 49, 51),
            arrayOf(76, 84, 85),
        )
        BuildingStyle.ARMORY -> arrayOf(
            arrayOf(52, 53, 54),
            arrayOf(72, 85, 84),
        )
        BuildingStyle.HOUSE -> if (id == PlaceId.HOME) {
            arrayOf(
                arrayOf(52, 53, 55),
                arrayOf(72, 84, 85),
            )
        } else {
            arrayOf(
                arrayOf(52, 53, 54),
                arrayOf(72, 85, 84),
            )
        }
    }

    fun widthTiles(style: BuildingStyle, id: PlaceId): Int =
        tiles(style, id).maxOf { row -> row.size }

    fun heightTiles(style: BuildingStyle, id: PlaceId): Int =
        tiles(style, id).size
}

fun DrawScope.drawBuildingRecipe(atlas: KenneyAtlas, p: Place) {
    val grid = BuildingRecipes.tiles(p.style, p.id)
    val rows = grid.size
    val cols = grid.maxOf { it.size }
    val totalW = cols * WORLD_TILE
    val totalH = rows * WORLD_TILE
    val originX = p.cx - totalW / 2f
    val originY = p.bottom - totalH
    for (r in 0 until rows) {
        val row = grid[r]
        for (c in row.indices) {
            val tid = row[c] ?: continue
            drawKenneyTile(
                atlas.town,
                tid,
                originX + c * WORLD_TILE,
                originY + r * WORLD_TILE,
                WORLD_TILE,
            )
        }
    }
    if (p.style == BuildingStyle.STORE) {
        drawKenneySpriteAsset(atlas.sprite("crate"), p.cx - WORLD_TILE, p.bottom, WORLD_TILE)
        drawKenneySpriteAsset(atlas.sprite("sign"), p.cx + WORLD_TILE * 1.2f, p.bottom - WORLD_TILE, WORLD_TILE)
    }
    if (p.style == BuildingStyle.ARENA) {
        drawKenneySpriteAsset(atlas.sprite("target"), p.cx + WORLD_TILE, p.bottom, WORLD_TILE * 1.1f)
    }
}
