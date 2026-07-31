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
 * Buildings/trees/props are drawn as sprites on top from place coordinates.
 */
object VillageGround {
    val tiles: Array<IntArray> = Array(ROWS) { IntArray(COLS) { TownTiles.GRASS } }

    init {
        val rng = Random(42)
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                tiles[r][c] = when (rng.nextInt(18)) {
                    0 -> TownTiles.GRASS_TUFT
                    1 -> TownTiles.GRASS_FLOWER
                    else -> TownTiles.GRASS
                }
            }
        }
        // Vertical road
        paintRoadRect(
            Village.ROAD_X - Village.ROAD_W / 2f,
            Village.ROAD_TOP - Village.ROAD_W / 2f,
            Village.ROAD_W,
            Village.BOTTOM_ROAD_Y - Village.ROAD_TOP + Village.ROAD_W
        )
        // Horizontal row roads
        Village.rowRoads.forEach { y ->
            paintRoadRect(
                Village.ROW_ROAD_LEFT - Village.ROAD_W / 2f,
                y - Village.ROAD_W / 2f,
                (Village.ROW_ROAD_RIGHT - Village.ROW_ROAD_LEFT) + Village.ROAD_W,
                Village.ROAD_W
            )
        }
        // Bottom road
        paintRoadRect(
            Village.BOTTOM_ROAD_LEFT - Village.ROAD_W / 2f,
            Village.BOTTOM_ROAD_Y - Village.ROAD_W / 2f,
            (Village.BOTTOM_ROAD_RIGHT - Village.BOTTOM_ROAD_LEFT) + Village.ROAD_W,
            Village.ROAD_W
        )
        // Plaza around well
        paintRoadCircle(Village.WELL_X, Village.WELL_Y, 110f)
    }

    private fun paintRoadRect(x: Float, y: Float, w: Float, h: Float) {
        val c0 = floor(x / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r0 = floor(y / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        val c1 = floor((x + w) / WORLD_TILE).toInt().coerceIn(0, COLS - 1)
        val r1 = floor((y + h) / WORLD_TILE).toInt().coerceIn(0, ROWS - 1)
        for (r in r0..r1) for (c in c0..c1) {
            tiles[r][c] = pathTile(c, r, c0, r0, c1, r1)
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
            if (dx * dx + dy * dy <= r2) {
                tiles[r][c] = TownTiles.PATH
            }
        }
    }

    private fun pathTile(c: Int, r: Int, c0: Int, r0: Int, c1: Int, r1: Int): Int {
        val left = c == c0
        val right = c == c1
        val top = r == r0
        val bottom = r == r1
        return when {
            top && left -> TownTiles.PATH_TL
            top && right -> TownTiles.PATH_TR
            bottom && left -> TownTiles.PATH_BL
            bottom && right -> TownTiles.PATH_BR
            top -> TownTiles.PATH_T
            bottom -> TownTiles.PATH_B
            left -> TownTiles.PATH_L
            right -> TownTiles.PATH_R
            else -> TownTiles.PATH
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
        BuildingStyle.TOWER -> drawTowerBuilding(atlas, p)
        BuildingStyle.CHURCH -> drawChurchBuilding(atlas, p)
        BuildingStyle.FORGE -> drawHouseBuilding(atlas, p, redRoof = false, greyWall = true)
        BuildingStyle.STORE -> drawHouseBuilding(atlas, p, redRoof = true, greyWall = false, awning = true)
        BuildingStyle.INN, BuildingStyle.PUB -> drawHouseBuilding(atlas, p, redRoof = true, greyWall = false, tall = true)
        BuildingStyle.CLINIC -> drawHouseBuilding(atlas, p, redRoof = false, greyWall = true)
        BuildingStyle.ARMORY -> drawHouseBuilding(atlas, p, redRoof = true, greyWall = true)
        else -> drawHouseBuilding(atlas, p, redRoof = true, greyWall = false)
    }
}

private fun DrawScope.drawHouseBuilding(
    atlas: KenneyAtlas,
    p: Place,
    redRoof: Boolean,
    greyWall: Boolean,
    tall: Boolean = false,
    awning: Boolean = false,
) {
    val tw = WORLD_TILE
    val cols = (p.w / tw).toInt().coerceIn(4, 7)
    val rows = if (tall) 5 else 4
    val originX = p.cx - cols * tw / 2f
    val originY = p.bottom - rows * tw

    val roofL = if (redRoof) TownTiles.ROOF_RED_L else TownTiles.ROOF_BLUE_L
    val roofM = if (redRoof) TownTiles.ROOF_RED_M else TownTiles.ROOF_BLUE_M
    val roofR = if (redRoof) TownTiles.ROOF_RED_R else TownTiles.ROOF_BLUE_R
    val win = if (greyWall) TownTiles.WALL_STONE_WIN else TownTiles.WALL_WOOD_WIN
    val door = if (greyWall) TownTiles.WALL_STONE_DOOR else TownTiles.WALL_WOOD_DOOR
    val mid = if (greyWall) TownTiles.WALL_STONE_M else TownTiles.WALL_WOOD_M
    val right = if (greyWall) TownTiles.WALL_STONE_R else TownTiles.WALL_WOOD_R
    val doorCol = cols / 2

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when (r) {
                0 -> when (c) {
                    0 -> roofL
                    cols - 1 -> roofR
                    else -> roofM
                }
                rows - 1 -> when (c) {
                    cols - 1 -> right
                    doorCol -> door
                    else -> mid
                }
                rows - 2 -> when (c) {
                    cols - 1 -> right
                    1, cols - 2 -> win
                    else -> mid
                }
                else -> when (c) {
                    cols - 1 -> right
                    else -> mid
                }
            }
            drawKenneyTile(atlas.town, tid, originX + c * tw, originY + r * tw, tw)
        }
    }
    if (awning) {
        drawKenneyTile(atlas.town, TownTiles.SIGN, p.cx - tw / 2f, p.bottom - tw * 1.2f, tw)
        drawKenneyTile(atlas.town, TownTiles.CRATE, p.left + 4f, p.bottom - tw, tw)
        drawKenneyTile(atlas.town, TownTiles.BASKET, p.right - tw - 4f, p.bottom - tw, tw)
    }
}

private fun DrawScope.drawChurchBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 6
    val rows = 5
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when {
                r == 0 && c in 2..3 -> TownTiles.CASTLE_TM
                r == 0 -> TownTiles.ROOF_BLUE_M
                r == 1 && c == 0 -> TownTiles.CASTLE_TL
                r == 1 && c == cols - 1 -> TownTiles.CASTLE_TR
                r == 1 -> TownTiles.STONE_B
                r == rows - 1 && c == cols / 2 -> TownTiles.WALL_GREY_WIN
                c == 0 -> TownTiles.WALL_GREY_L
                c == cols - 1 -> TownTiles.WALL_GREY_R
                r == 2 && c in 1..2 -> TownTiles.WALL_GREY_WIN
                else -> TownTiles.WALL_GREY_M
            }
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
}

private fun DrawScope.drawTowerBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 5
    val rows = 5
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when {
                r == 0 && c == cols - 1 -> TownTiles.CASTLE_TM
                r == 0 -> TownTiles.ROOF_BLUE_M
                r <= 2 && c == cols - 1 -> TownTiles.STONE_A
                r == rows - 1 && c == 1 -> TownTiles.WALL_GREY_WIN
                c == 0 -> TownTiles.WALL_TAN_L
                c == cols - 1 -> TownTiles.WALL_TAN_R
                else -> TownTiles.WALL_TAN_M
            }
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
}

private fun DrawScope.drawCaveBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 5
    val rows = 4
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when {
                r == 0 && c == 0 -> TownTiles.CASTLE_TL
                r == 0 && c == cols - 1 -> TownTiles.CASTLE_TR
                r == 0 -> TownTiles.CASTLE_TM
                r == rows - 1 && c == cols / 2 -> TownTiles.CASTLE_BM
                r == rows - 1 && c == 0 -> TownTiles.CASTLE_BL
                r == rows - 1 && c == cols - 1 -> TownTiles.CASTLE_BR
                else -> TownTiles.STONE_C
            }
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
}

private fun DrawScope.drawArenaBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    val cols = 5
    val rows = 4
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val edge = r == 0 || r == rows - 1 || c == 0 || c == cols - 1
            val tid = if (edge) TownTiles.FENCE_H else TownTiles.PATH
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
    drawKenneyTile(atlas.town, TownTiles.TARGET, p.cx - tw / 2f, p.cy - tw / 2f, tw)
}

private fun DrawScope.drawCampBuilding(atlas: KenneyAtlas, p: Place) {
    val tw = WORLD_TILE
    // Tent-like: red roof triangle over path floor
    val cols = 5
    val rows = 3
    val ox = p.cx - cols * tw / 2f
    val oy = p.bottom - rows * tw
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tid = when (r) {
                0 -> when (c) {
                    0 -> TownTiles.ROOF_RED_L
                    cols - 1 -> TownTiles.ROOF_RED_R
                    else -> TownTiles.ROOF_RED_M
                }
                else -> when (c) {
                    0, cols - 1 -> TownTiles.FENCE_V
                    cols / 2 -> TownTiles.WALL_TAN_WIN
                    else -> TownTiles.PATH
                }
            }
            drawKenneyTile(atlas.town, tid, ox + c * tw, oy + r * tw, tw)
        }
    }
    drawKenneyTile(atlas.town, TownTiles.BARREL, p.left, p.bottom - tw, tw)
}

fun DrawScope.drawKenneyScenery(atlas: KenneyAtlas) {
    Village.trees.forEachIndexed { i, (x, y, _) ->
        val tid = when (i % 4) {
            0 -> TownTiles.TREE_GREEN
            1 -> TownTiles.TREE_ORANGE
            2 -> TownTiles.BUSH
            else -> TownTiles.TREE_GREEN
        }
        drawKenneyTile(atlas.town, tid, x - WORLD_TILE / 2f, y - WORLD_TILE, WORLD_TILE * 1.35f)
    }
    // Well
    drawKenneyTile(
        atlas.town,
        TownTiles.WELL,
        Village.WELL_X - WORLD_TILE / 2f,
        Village.WELL_Y - WORLD_TILE,
        WORLD_TILE * 1.2f
    )
    // Lamps → fence posts with sign flair
    Village.lamps.forEach { (x, y) ->
        drawKenneyTile(atlas.town, TownTiles.FENCE_V, x - WORLD_TILE * 0.35f, y - WORLD_TILE, WORLD_TILE * 0.9f)
        drawKenneyTile(atlas.town, TownTiles.SIGN, x - WORLD_TILE * 0.15f, y - WORLD_TILE * 1.35f, WORLD_TILE * 0.7f)
    }
    // Market stalls
    Village.stalls.forEach { (x, y, _) ->
        drawKenneyTile(atlas.town, TownTiles.ROOF_RED_M, x - WORLD_TILE, y - WORLD_TILE * 1.6f, WORLD_TILE)
        drawKenneyTile(atlas.town, TownTiles.ROOF_RED_M, x, y - WORLD_TILE * 1.6f, WORLD_TILE)
        drawKenneyTile(atlas.town, TownTiles.CRATE, x - WORLD_TILE * 0.7f, y - WORLD_TILE, WORLD_TILE)
        drawKenneyTile(atlas.town, TownTiles.BARREL, x + WORLD_TILE * 0.2f, y - WORLD_TILE, WORLD_TILE)
    }
    // Mushrooms near home
    val home = Village.of(PlaceId.HOME)
    drawKenneyTile(atlas.town, TownTiles.MUSHROOM, home.left - WORLD_TILE, home.doorY - WORLD_TILE, WORLD_TILE * 0.85f)
}
