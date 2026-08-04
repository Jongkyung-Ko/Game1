package com.medieval.village.model

import kotlin.random.Random

/** 던전 격자 셀 종류 */
enum class DungeonCell {
    WALL,
    FLOOR,
    ENTRANCE,   // 지상으로 올라가는 계단
    EXIT,       // 더 깊은 층으로 내려가는 계단
    CHEST,
    GATE,
    TORCH
}

data class DungeonPoint(val x: Int, val y: Int)

/**
 * 한 층의 탑다운 지도.
 * 그리드는 타일 단위이고, 화면에서는 [CELL] 픽셀로 그린다.
 */
data class DungeonFloor(
    val floor: Int,
    val cols: Int,
    val rows: Int,
    val cells: Array<Array<DungeonCell>>,
    val hero: DungeonPoint,
    val slime: DungeonPoint?,
    val companions: List<DungeonPoint>
) {
    fun cell(x: Int, y: Int): DungeonCell =
        if (x in 0 until cols && y in 0 until rows) cells[y][x] else DungeonCell.WALL

    override fun equals(other: Any?): Boolean = other is DungeonFloor && floor == other.floor
    override fun hashCode(): Int = floor
}

object DungeonLayout {
    const val WORLD_W = 900f
    const val WORLD_H = 1100f
    const val COLS = 15
    const val ROWS = 18
    const val CELL = 52f
    const val PAD_X = (WORLD_W - COLS * CELL) / 2f
    const val PAD_Y = 70f

    fun forFloor(floor: Int): DungeonFloor {
        val rng = Random(floor * 7919L + 42L)
        val cells = Array(ROWS) { Array(COLS) { DungeonCell.WALL } }

        // 손맛 나는 고정 골격 + 층마다 가지 통로를 살짝 바꾼다.
        carveRoom(cells, 5, 13, 5, 4)          // 입구 홀
        carveRoom(cells, 2, 8, 4, 4)           // 왼쪽 방
        carveRoom(cells, 9, 8, 4, 4)           // 오른쪽 방
        carveRoom(cells, 5, 3, 5, 4)           // 상단 보스/출구 방
        carveRoom(cells, 5, 8, 5, 3)           // 중앙 교차

        // 복도
        carveHall(cells, 7, 12, 7, 10)         // 입구 → 중앙
        carveHall(cells, 7, 8, 7, 6)           // 중앙 → 상단
        carveHall(cells, 5, 9, 3, 9)           // 왼쪽 연결
        carveHall(cells, 9, 9, 11, 9)          // 오른쪽 연결

        // 층마다 추가 곁방 / 막힌 길
        when (floor % 3) {
            0 -> {
                carveRoom(cells, 1, 3, 3, 3)
                carveHall(cells, 3, 5, 5, 5)
            }
            1 -> {
                carveRoom(cells, 11, 3, 3, 3)
                carveHall(cells, 11, 5, 9, 5)
            }
            else -> {
                carveRoom(cells, 1, 12, 3, 3)
                carveHall(cells, 3, 13, 5, 13)
            }
        }

        // 테두리 벽 보강
        for (x in 0 until COLS) {
            cells[0][x] = DungeonCell.WALL
            cells[ROWS - 1][x] = DungeonCell.WALL
        }
        for (y in 0 until ROWS) {
            cells[y][0] = DungeonCell.WALL
            cells[y][COLS - 1] = DungeonCell.WALL
        }

        // 특수 타일
        cells[15][7] = DungeonCell.ENTRANCE
        cells[4][7] = DungeonCell.EXIT
        cells[9][11] = DungeonCell.CHEST
        cells[9][3] = DungeonCell.GATE

        // 횃불 (벽 옆 바닥)
        placeTorch(cells, 5, 14)
        placeTorch(cells, 9, 14)
        placeTorch(cells, 3, 10)
        placeTorch(cells, 11, 10)
        placeTorch(cells, 5, 5)
        placeTorch(cells, 9, 5)

        val floors = mutableListOf<DungeonPoint>()
        for (y in 0 until ROWS) {
            for (x in 0 until COLS) {
                if (cells[y][x] != DungeonCell.WALL) floors += DungeonPoint(x, y)
            }
        }

        val hero = DungeonPoint(7, 14)
        val slimePool = floors.filter {
            it != hero &&
                cells[it.y][it.x] == DungeonCell.FLOOR &&
                kotlin.math.abs(it.x - hero.x) + kotlin.math.abs(it.y - hero.y) > 3
        }
        val slime = slimePool.getOrNull(rng.nextInt(slimePool.size.coerceAtLeast(1)))

        val companionSpots = listOf(
            DungeonPoint(6, 15),
            DungeonPoint(8, 15)
        ).filter { cells[it.y][it.x] != DungeonCell.WALL }

        return DungeonFloor(
            floor = floor,
            cols = COLS,
            rows = ROWS,
            cells = cells,
            hero = hero,
            slime = slime,
            companions = companionSpots
        )
    }

    fun cellCenter(x: Int, y: Int): Pair<Float, Float> {
        val cx = PAD_X + x * CELL + CELL / 2f
        val cy = PAD_Y + y * CELL + CELL / 2f
        return cx to cy
    }

    private fun carveRoom(cells: Array<Array<DungeonCell>>, x: Int, y: Int, w: Int, h: Int) {
        for (yy in y until (y + h).coerceAtMost(ROWS - 1)) {
            for (xx in x until (x + w).coerceAtMost(COLS - 1)) {
                if (xx > 0 && yy > 0) cells[yy][xx] = DungeonCell.FLOOR
            }
        }
    }

    private fun carveHall(cells: Array<Array<DungeonCell>>, x0: Int, y0: Int, x1: Int, y1: Int) {
        var x = x0
        var y = y0
        while (x != x1) {
            if (x in 1 until COLS - 1 && y in 1 until ROWS - 1) cells[y][x] = DungeonCell.FLOOR
            x += if (x1 > x) 1 else -1
        }
        while (y != y1) {
            if (x in 1 until COLS - 1 && y in 1 until ROWS - 1) cells[y][x] = DungeonCell.FLOOR
            y += if (y1 > y) 1 else -1
        }
        if (x in 1 until COLS - 1 && y in 1 until ROWS - 1) cells[y][x] = DungeonCell.FLOOR
    }

    private fun placeTorch(cells: Array<Array<DungeonCell>>, x: Int, y: Int) {
        if (x in 0 until COLS && y in 0 until ROWS && cells[y][x] == DungeonCell.FLOOR) {
            cells[y][x] = DungeonCell.TORCH
        }
    }
}
