package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.Place
import com.medieval.village.model.Village
import kotlin.math.floor
import kotlin.random.Random

/** World units per ground tile. */
const val WORLD_TILE = 40f

private val COLS = (Village.W / WORLD_TILE).toInt()
private val ROWS = (Village.H / WORLD_TILE).toInt()

object VillageGround {
    val tiles: Array<IntArray> = Array(ROWS) { IntArray(COLS) { TownTiles.GRASS } }

    init {
        val rng = Random(42)
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                tiles[r][c] = when (rng.nextInt(20)) {
                    0 -> TownTiles.GRASS_TUFT
                    1 -> TownTiles.GRASS_FLOWER
                    else -> TownTiles.GRASS
                }
            }
        }
        paintRoadRect(
            Village.ROAD_X - Village.ROAD_W / 2f,
            Village.ROAD_TOP - Village.ROAD_W / 2f,
            Village.ROAD_W,
            Village.BOTTOM_ROAD_Y - Village.ROAD_TOP + Village.ROAD_W
        )
        Village.rowRoads.forEach { y ->
            paintRoadRect(
                Village.ROW_ROAD_LEFT - Village.ROAD_W / 2f,
                y - Village.ROAD_W / 2f,
                (Village.ROW_ROAD_RIGHT - Village.ROW_ROAD_LEFT) + Village.ROAD_W,
                Village.ROAD_W
            )
        }
        paintRoadRect(
            Village.BOTTOM_ROAD_LEFT - Village.ROAD_W / 2f,
            Village.BOTTOM_ROAD_Y - Village.ROAD_W / 2f,
            (Village.BOTTOM_ROAD_RIGHT - Village.BOTTOM_ROAD_LEFT) + Village.ROAD_W,
            Village.ROAD_W
        )
        paintRoadCircle(Village.WELL_X, Village.WELL_Y, 100f)
        softenPathEdges()
    }

    private fun paintRoadRect(x: Float, y: Float, w: Float, h: Float) {
        val c0 = floor(x / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r0 = floor(y / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val c1 = floor((x + w) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r1 = floor((y + h) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        for (r in r0..r1) for (c in c0..c1) tiles[r][c] = TownTiles.PATH
    }

    private fun paintRoadCircle(cx: Float, cy: Float, radius: Float) {
        val c0 = floor((cx - radius) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r0 = floor((cy - radius) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val c1 = floor((cx + radius) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r1 = floor((cy + radius) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val r2 = radius * radius
        for (r in r0..r1) for (c in c0..c1) {
            val dx = (c + 0.5f) * WORLD_TILE - cx
            val dy = (r + 0.5f) * WORLD_TILE - cy
            if (dx * dx + dy * dy <= r2) tiles[r][c] = TownTiles.PATH
        }
    }

    private fun isPath(c: Int, r: Int): Boolean {
        if (c !in 0 until COLS || r !in 0 until ROWS) return false
        val t = tiles[r][c]
        return t >= TownTiles.PATH_TL && t <= TownTiles.PATH_BR || t == TownTiles.PATH
    }

    private fun softenPathEdges() {
        val copy = Array(ROWS) { r -> tiles[r].clone() }
        for (r in 0 until ROWS) for (c in 0 until COLS) {
            if (tiles[r][c] != TownTiles.PATH &&
                tiles[r][c] != TownTiles.PATH_T &&
                tiles[r][c] != TownTiles.PATH_B &&
                tiles[r][c] != TownTiles.PATH_L &&
                tiles[r][c] != TownTiles.PATH_R &&
                tiles[r][c] != TownTiles.PATH_TL &&
                tiles[r][c] != TownTiles.PATH_TR &&
                tiles[r][c] != TownTiles.PATH_BL &&
                tiles[r][c] != TownTiles.PATH_BR
            ) {
                // only rewrite solid path cells from paint
                if (tiles[r][c] != TownTiles.PATH) continue
            }
            if (tiles[r][c] != TownTiles.PATH) continue
            val n = isPath(c, r - 1)
            val s = isPath(c, r + 1)
            val w = isPath(c - 1, r)
            val e = isPath(c + 1, r)
            copy[r][c] = when {
                !n && !w -> TownTiles.PATH_TL
                !n && !e -> TownTiles.PATH_TR
                !s && !w -> TownTiles.PATH_BL
                !s && !e -> TownTiles.PATH_BR
                !n -> TownTiles.PATH_T
                !s -> TownTiles.PATH_B
                !w -> TownTiles.PATH_L
                !e -> TownTiles.PATH_R
                else -> TownTiles.PATH
            }
        }
        for (r in 0 until ROWS) for (c in 0 until COLS) tiles[r][c] = copy[r][c]
    }
}

fun DrawScope.drawVillageTilemap(atlas: KenneyAtlas) {
    for (r in 0 until ROWS) {
        for (c in 0 until COLS) {
            drawKenneyTile(
                atlas.town,
                VillageGround.tiles[r][c],
                c * WORLD_TILE,
                r * WORLD_TILE,
                WORLD_TILE
            )
        }
    }
}

/** Tiny Town Sample처럼 지붕+벽 타일을 그리드 비율 그대로 그린다 (늘리기 금지). */
fun DrawScope.drawKenneyPlace(atlas: KenneyAtlas, p: Place) {
    drawBuildingRecipe(atlas, p)
}

fun DrawScope.drawKenneyScenery(atlas: KenneyAtlas) {
    Village.trees.forEachIndexed { i, (x, y, _) ->
        val name = when (i % 4) {
            0 -> "tree_g"
            1 -> "tree_o"
            2 -> "bush"
            else -> "mushroom"
        }
        val h = when (name) {
            "bush", "mushroom" -> WORLD_TILE * 1.05f
            else -> WORLD_TILE * 1.35f
        }
        drawKenneySpriteAsset(atlas.sprite(name), x, y, h)
    }
    drawKenneySpriteAsset(atlas.sprite("well"), Village.WELL_X, Village.WELL_Y, WORLD_TILE * 1.25f)
    drawKenneySpriteAsset(atlas.sprite("hive"), Village.WELL_X + 70f, Village.WELL_Y + 10f, WORLD_TILE)
    Village.lamps.forEach { (x, y) ->
        drawKenneySpriteAsset(atlas.sprite("sign"), x, y, WORLD_TILE)
    }
    Village.stalls.forEach { (x, y, _) ->
        drawKenneySpriteAsset(atlas.sprite("crate"), x - 18f, y, WORLD_TILE)
        drawKenneySpriteAsset(atlas.sprite("basket"), x + 18f, y, WORLD_TILE)
    }
    Village.fences.forEach { (x, y) ->
        drawKenneySpriteAsset(atlas.sprite("fence"), x, y, WORLD_TILE * 0.95f)
    }
}
