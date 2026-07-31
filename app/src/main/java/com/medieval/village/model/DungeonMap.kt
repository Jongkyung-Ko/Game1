package com.medieval.village.model

import kotlin.math.hypot
import kotlin.random.Random

enum class DungeonTile {
    WALL,
    FLOOR,
    STAIRS_UP,
    STAIRS_DOWN,
    SEWER,
    VAULT
}

data class DungeonMonster(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    val power: Int,
    var alive: Boolean = true
)

data class DungeonFloor(
    val floor: Int,
    val cols: Int,
    val rows: Int,
    val tileSize: Float,
    val tiles: Array<DungeonTile>,
    val spawnX: Float,
    val spawnY: Float,
    val stairsDownX: Float,
    val stairsDownY: Float,
    val monsters: MutableList<DungeonMonster>
) {
    val worldW: Float get() = cols * tileSize
    val worldH: Float get() = rows * tileSize

    fun tileAt(col: Int, row: Int): DungeonTile {
        if (col !in 0 until cols || row !in 0 until rows) return DungeonTile.WALL
        return tiles[row * cols + col]
    }

    fun isWalkable(x: Float, y: Float): Boolean {
        val col = (x / tileSize).toInt()
        val row = (y / tileSize).toInt()
        return when (tileAt(col, row)) {
            DungeonTile.WALL -> false
            else -> true
        }
    }

    fun tileKindAt(x: Float, y: Float): DungeonTile {
        val col = (x / tileSize).toInt()
        val row = (y / tileSize).toInt()
        return tileAt(col, row)
    }
}

/**
 * 라그나로크식 탐험을 위한 층별 미로형 던전 생성기.
 * 지하 보관소·하수도가 좀비의 둥지로 변한 구조를 표현한다.
 */
object DungeonFactory {

    const val TILE = 64f
    const val COLS = 28
    const val ROWS = 22

    private val zombieNames = listOf(
        "굶주린 좀비", "부패한 주민", "검은 눈의 시체",
        "하수구 좀비", "오염된 광부", "뇌 없는 경비"
    )

    fun generate(floor: Int, seed: Int = floor * 7919 + 42): DungeonFloor {
        val rng = Random(seed)
        val tiles = Array(COLS * ROWS) { DungeonTile.WALL }

        fun idx(c: Int, r: Int) = r * COLS + c
        fun carve(c: Int, r: Int, kind: DungeonTile = DungeonTile.FLOOR) {
            if (c in 1 until COLS - 1 && r in 1 until ROWS - 1) {
                tiles[idx(c, r)] = kind
            }
        }

        // 방과 복도를 잇는 미로형 레이아웃
        val rooms = mutableListOf<Pair<IntRange, IntRange>>()
        val roomCount = 5 + floor.coerceAtMost(4)
        repeat(roomCount) {
            val w = rng.nextInt(4, 7)
            val h = rng.nextInt(3, 6)
            val c0 = rng.nextInt(2, COLS - w - 2)
            val r0 = rng.nextInt(2, ROWS - h - 2)
            val cRange = c0 until (c0 + w)
            val rRange = r0 until (r0 + h)
            rooms += cRange to rRange
            for (r in rRange) for (c in cRange) {
                val kind = when {
                    floor >= 3 && rng.nextFloat() < 0.08f -> DungeonTile.VAULT
                    rng.nextFloat() < 0.12f -> DungeonTile.SEWER
                    else -> DungeonTile.FLOOR
                }
                carve(c, r, kind)
            }
        }

        // 방 중심을 복도로 연결
        for (i in 0 until rooms.lastIndex) {
            val (aC, aR) = rooms[i]
            val (bC, bR) = rooms[i + 1]
            var cx = aC.average().toInt()
            var cy = aR.average().toInt()
            val tx = bC.average().toInt()
            val ty = bR.average().toInt()
            while (cx != tx) {
                carve(cx, cy, if (rng.nextFloat() < 0.2f) DungeonTile.SEWER else DungeonTile.FLOOR)
                cx += if (tx > cx) 1 else -1
            }
            while (cy != ty) {
                carve(cx, cy, if (rng.nextFloat() < 0.2f) DungeonTile.SEWER else DungeonTile.FLOOR)
                cy += if (ty > cy) 1 else -1
            }
        }

        val startRoom = rooms.first()
        val endRoom = rooms.last()
        val startC = startRoom.first.average().toInt()
        val startR = startRoom.second.average().toInt()
        val endC = endRoom.first.average().toInt()
        val endR = endRoom.second.average().toInt()
        tiles[idx(startC, startR)] = DungeonTile.STAIRS_UP
        tiles[idx(endC, endR)] = DungeonTile.STAIRS_DOWN

        val spawnX = startC * TILE + TILE / 2f
        val spawnY = startR * TILE + TILE / 2f
        val downX = endC * TILE + TILE / 2f
        val downY = endR * TILE + TILE / 2f

        val monsters = mutableListOf<DungeonMonster>()
        val monsterCount = 4 + floor + rng.nextInt(0, 3)
        var attempts = 0
        while (monsters.size < monsterCount && attempts < 200) {
            attempts++
            val c = rng.nextInt(1, COLS - 1)
            val r = rng.nextInt(1, ROWS - 1)
            if (tiles[idx(c, r)] == DungeonTile.WALL) continue
            if (c == startC && r == startR) continue
            if (c == endC && r == endR) continue
            val x = c * TILE + TILE / 2f
            val y = r * TILE + TILE / 2f
            if (hypot(x - spawnX, y - spawnY) < TILE * 2.5f) continue
            if (monsters.any { hypot(it.x - x, it.y - y) < TILE * 1.4f }) continue
            monsters += DungeonMonster(
                id = "z${floor}_${monsters.size}",
                name = zombieNames.random(rng),
                x = x,
                y = y,
                power = 10 + floor * 8 + rng.nextInt(0, 8)
            )
        }

        return DungeonFloor(
            floor = floor,
            cols = COLS,
            rows = ROWS,
            tileSize = TILE,
            tiles = tiles,
            spawnX = spawnX,
            spawnY = spawnY,
            stairsDownX = downX,
            stairsDownY = downY,
            monsters = monsters
        )
    }
}
