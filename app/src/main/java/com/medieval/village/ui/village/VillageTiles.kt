package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.model.BuildingStyle
import com.medieval.village.model.Place
import com.medieval.village.model.PlaceId
import com.medieval.village.model.Village
import kotlin.math.floor
import kotlin.random.Random

/** World units per Kenney tile — chunky pixel look matching Option A. */
const val WORLD_TILE = 40f

private val COLS = (Village.W / WORLD_TILE).toInt() // 25
private val ROWS = (Village.H / WORLD_TILE).toInt() // 41

/**
 * Precomputed Tiny Town ground layer (grass + roads).
 * Buildings are compact 2~3 tile-tall sprites (not wall-filled rectangles).
 */
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
        // Re-apply soft edges after overlaps
        softenPathEdges()
    }

    private fun paintRoadRect(x: Float, y: Float, w: Float, h: Float) {
        val c0 = floor(x / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r0 = floor(y / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val c1 = floor((x + w) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r1 = floor((y + h) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        for (r in r0..r1) for (c in c0..c1) {
            tiles[r][c] = TownTiles.PATH
        }
    }

    private fun paintRoadCircle(cx: Float, cy: Float, radius: Float) {
        val c0 = floor((cx - radius) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r0 = floor((cy - radius) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val c1 = floor((cx + radius) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r1 = floor((cy + radius) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val r2 = radius * radius
        for (r in r0..r1) for (c in c0..c1) {
            val px = (c + 0.5f) * WORLD_TILE
            val py = (r + 0.5f) * WORLD_TILE
            val dx = px - cx
            val dy = py - cy
            if (dx * dx + dy * dy <= r2) tiles[r][c] = TownTiles.PATH
        }
    }

    private fun isPath(c: Int, r: Int): Boolean {
        if (c !in 0 until COLS || r !in 0 until ROWS) return false
        val t = tiles[r][c]
        return t == TownTiles.PATH || t == TownTiles.PATH_T || t == TownTiles.PATH_B ||
            t == TownTiles.PATH_L || t == TownTiles.PATH_R ||
            t == TownTiles.PATH_TL || t == TownTiles.PATH_TR ||
            t == TownTiles.PATH_BL || t == TownTiles.PATH_BR
    }

    private fun softenPathEdges() {
        val copy = Array(ROWS) { r -> tiles[r].clone() }
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                if (!isPath(c, r)) continue
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
        }
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) tiles[r][c] = copy[r][c]
        }
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

fun DrawScope.drawKenneyPlace(atlas: KenneyAtlas, p: Place) {
    when (p.style) {
        BuildingStyle.CAVE -> drawCaveBuilding(atlas, p)
        BuildingStyle.ARENA -> drawArenaBuilding(atlas, p)
        BuildingStyle.CAMP -> drawCampBuilding(atlas, p)
        BuildingStyle.TOWER -> drawCompactHouse(atlas, p, redRoof = false, stone = true, cols = 4, tall = true)
        BuildingStyle.CHURCH -> drawChurchBuilding(atlas, p)
        BuildingStyle.FORGE -> drawCompactHouse(atlas, p, redRoof = false, stone = true, cols = 4)
        BuildingStyle.STORE -> drawCompactHouse(atlas, p, redRoof = true, stone = false, cols = 4, props = true)
        BuildingStyle.INN -> drawCompactHouse(atlas, p, redRoof = true, stone = false, cols = 5, tall = true)
        BuildingStyle.PUB -> drawCompactHouse(atlas, p, redRoof = true, stone = false, cols = 3)
        BuildingStyle.CLINIC -> drawCompactHouse(atlas, p, redRoof = false, stone = true, cols = 4)
        BuildingStyle.ARMORY -> drawCompactHouse(atlas, p, redRoof = true, stone = true, cols = 4)
        else -> drawCompactHouse(atlas, p, redRoof = true, stone = false, cols = 4)
    }
}

/**
 * Tiny Town 샘플처럼 지붕 1줄 + 벽/문 1~2줄의 작은 집.
 * 장소 사각형 전체를 벽으로 채우지 않는다.
 */
private fun DrawScope.drawCompactHouse(
    atlas: KenneyAtlas,
    p: Place,
    redRoof: Boolean,
    stone: Boolean,
    cols: Int,
    tall: Boolean = false,
    props: Boolean = false,
) {
    val tw = WORLD_TILE
    val rows = if (tall) 3 else 2
    val originX = p.cx - cols * tw / 2f
    // 문 바로 위에 붙도록 바닥 정렬
    val originY = p.bottom - rows * tw

    val roofL = if (redRoof) TownTiles.ROOF_RED_L else TownTiles.ROOF_BLUE_L
    val roofM = if (redRoof) TownTiles.ROOF_RED_M else TownTiles.ROOF_BLUE_M
    val roofR = if (redRoof) TownTiles.ROOF_RED_R else TownTiles.ROOF_BLUE_R
    val win = if (stone) TownTiles.WALL_STONE_WIN else TownTiles.WALL_WOOD_WIN
    val door = if (stone) TownTiles.WALL_STONE_DOOR else TownTiles.WALL_WOOD_DOOR
    val mid = if (stone) TownTiles.WALL_STONE_M else TownTiles.WALL_WOOD_M
    val right = if (stone) TownTiles.WALL_STONE_R else TownTiles.WALL_WOOD_R
    val doorCol = (cols - 1) / 2

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when (r) {
                0 -> when (c) {
                    0 -> roofL
                    cols - 1 -> roofR
                    else -> roofM
                }
                rows - 1 -> when (c) {
                    doorCol -> door
                    cols - 1 -> right
                    doorCol - 1, doorCol + 1 -> if (c in 0 until cols) win else mid
                    else -> mid
                }
                else -> when (c) {
                    cols - 1 -> right
                    0, cols - 2 -> win
                    else -> mid
                }
            }
            drawKenneyTile(atlas.town, tid, originX + c * tw, originY + r * tw, tw)
        }
    }
    if (props) {
        drawKenneyTile(atlas.town, TownTiles.SIGN, p.cx + cols * tw * 0.35f, p.bottom - tw * 1.5f, tw * 0.85f)
        drawKenneyTile(atlas.town, TownTiles.CRATE, originX - tw * 0.7f, p.bottom - tw, tw)
        drawKenneyTile(atlas.town, TownTiles.BASKET, originX + cols * tw, p.bottom - tw, tw)
    }
}

private fun DrawScope.drawChurchBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 5
    val rows = 3
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when (r) {
                0 -> when (c) {
                    0 -> TownTiles.ROOF_BLUE_L
                    cols - 1 -> TownTiles.ROOF_BLUE_R
                    else -> TownTiles.ROOF_BLUE_M
                }
                1 -> when (c) {
                    0, cols - 1 -> TownTiles.WALL_STONE_R
                    1, 3 -> TownTiles.WALL_STONE_WIN
                    else -> TownTiles.WALL_STONE_M
                }
                else -> when (c) {
                    2 -> TownTiles.WALL_STONE_DOOR
                    cols - 1 -> TownTiles.WALL_STONE_R
                    else -> TownTiles.WALL_STONE_M
                }
            }
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
}

private fun DrawScope.drawCaveBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 4
    val rows = 3
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    // 돌 아치 입구
    val grid = arrayOf(
        intArrayOf(TownTiles.CASTLE_TL, TownTiles.CASTLE_TM, TownTiles.CASTLE_TM, TownTiles.CASTLE_TR),
        intArrayOf(TownTiles.CASTLE_BL, 111, 112, TownTiles.CASTLE_BR),
        intArrayOf(TownTiles.CASTLE_BL, 113, TownTiles.CASTLE_BM, TownTiles.CASTLE_BR),
    )
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            drawKenneyTile(atlas.town, grid[r][c], ox + c * tw, oy + r * tw, tw)
        }
    }
}

private fun DrawScope.drawArenaBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 4
    val rows = 3
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val edge = r == 0 || r == rows - 1 || c == 0 || c == cols - 1
            drawKenneyTile(
                atlas.town,
                if (edge) TownTiles.FENCE_H else TownTiles.PATH,
                ox + c * tw,
                oy + r * tw,
                tw
            )
        }
    }
    drawKenneyTile(atlas.town, TownTiles.TARGET, p.cx - tw / 2f, p.cy - tw * 0.2f, tw)
}

private fun DrawScope.drawCampBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 4
    val rows = 2
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = if (r == 0) {
                when (c) {
                    0 -> TownTiles.ROOF_RED_L
                    cols - 1 -> TownTiles.ROOF_RED_R
                    else -> TownTiles.ROOF_RED_M
                }
            } else {
                when (c) {
                    1 -> TownTiles.WALL_WOOD_DOOR
                    0, cols - 1 -> TownTiles.FENCE_V
                    else -> TownTiles.PATH
                }
            }
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
    drawKenneyTile(atlas.town, TownTiles.BARREL, ox - tw * 0.8f, p.bottom - tw, tw)
}

fun DrawScope.drawKenneyScenery(atlas: KenneyAtlas) {
    Village.trees.forEachIndexed { i, (x, y, _) ->
        val tid = when (i % 3) {
            0 -> 16 // round green tree with trunk
            1 -> 15 // orange tree
            else -> 17 // bush
        }
        val size = WORLD_TILE * 1.6f
        drawKenneyTile(atlas.town, tid, x - size / 2f, y - size * 0.85f, size)
    }
    // Well (Kenney Tiny Town tile 104)
    drawKenneyTile(
        atlas.town,
        104,
        Village.WELL_X - WORLD_TILE * 0.6f,
        Village.WELL_Y - WORLD_TILE * 1.1f,
        WORLD_TILE * 1.25f
    )
    Village.lamps.forEach { (x, y) ->
        drawKenneyTile(atlas.town, TownTiles.FENCE_V, x - WORLD_TILE * 0.3f, y - WORLD_TILE, WORLD_TILE)
        drawKenneyTile(atlas.town, 83, x - WORLD_TILE * 0.15f, y - WORLD_TILE * 1.4f, WORLD_TILE * 0.75f)
    }
    Village.stalls.forEach { (x, y, _) ->
        drawKenneyTile(atlas.town, TownTiles.ROOF_RED_M, x - WORLD_TILE, y - WORLD_TILE * 1.5f, WORLD_TILE)
        drawKenneyTile(atlas.town, TownTiles.ROOF_RED_M, x, y - WORLD_TILE * 1.5f, WORLD_TILE)
        drawKenneyTile(atlas.town, TownTiles.CRATE, x - WORLD_TILE * 0.6f, y - WORLD_TILE, WORLD_TILE)
        drawKenneyTile(atlas.town, TownTiles.BASKET, x + WORLD_TILE * 0.15f, y - WORLD_TILE, WORLD_TILE)
    }
    val home = Village.of(PlaceId.HOME)
    drawKenneyTile(atlas.town, TownTiles.MUSHROOM, home.left - WORLD_TILE, home.doorY - WORLD_TILE, WORLD_TILE)
}
