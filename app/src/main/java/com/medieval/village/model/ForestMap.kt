package com.medieval.village.model

import kotlin.math.hypot
import kotlin.random.Random

/**
 * 동쪽 숲 지대 생성기.
 * 던전과 같은 [DungeonFloor] 구조를 쓰되, 타일은 숲길·덤불·수풀로 해석한다.
 * 깊어질수록 더 강한 동물 몬스터가 나온다.
 */
object ForestFactory {

    const val TILE = DungeonFactory.TILE
    const val COLS = DungeonFactory.COLS
    const val ROWS = DungeonFactory.ROWS

    /** kind → 한국어 이름. 깊이에 따라 풀이 확장된다. */
    private val shallowAnimals = listOf(
        "rabbit" to "숲토끼",
        "fox" to "여우",
        "deer" to "사슴",
        "wolf" to "늑대",
        "boar" to "멧돼지",
        "snake" to "독사",
        "owl" to "부엉이",
    )

    private val deepAnimals = listOf(
        "dire_wolf" to "흉포한 늑대",
        "bear" to "불곰",
        "giant_boar" to "거대 멧돼지",
        "forest_spider" to "거미줄 독거미",
        "stag" to "뿔사슴",
    )

    fun generate(floor: Int, seed: Int = floor * 5303 + 91): DungeonFloor {
        val rng = Random(seed)
        val tiles = Array(COLS * ROWS) { DungeonTile.WALL }

        fun idx(c: Int, r: Int) = r * COLS + c
        fun carve(c: Int, r: Int, kind: DungeonTile = DungeonTile.FLOOR) {
            if (c in 1 until COLS - 1 && r in 1 until ROWS - 1) {
                tiles[idx(c, r)] = kind
            }
        }

        val clearings = mutableListOf<Pair<IntRange, IntRange>>()
        val clearingCount = 5 + floor.coerceAtMost(4)
        repeat(clearingCount) {
            val w = rng.nextInt(4, 8)
            val h = rng.nextInt(3, 6)
            val c0 = rng.nextInt(2, COLS - w - 2)
            val r0 = rng.nextInt(2, ROWS - h - 2)
            val cRange = c0 until (c0 + w)
            val rRange = r0 until (r0 + h)
            clearings += cRange to rRange
            for (r in rRange) for (c in cRange) {
                // SEWER = 덤불/키 큰 풀 (통과 가능)
                val kind = if (rng.nextFloat() < 0.18f) DungeonTile.SEWER else DungeonTile.FLOOR
                carve(c, r, kind)
            }
        }

        for (i in 0 until clearings.lastIndex) {
            val (aC, aR) = clearings[i]
            val (bC, bR) = clearings[i + 1]
            var cx = aC.average().toInt()
            var cy = aR.average().toInt()
            val tx = bC.average().toInt()
            val ty = bR.average().toInt()
            while (cx != tx) {
                carve(cx, cy, if (rng.nextFloat() < 0.25f) DungeonTile.SEWER else DungeonTile.FLOOR)
                cx += if (tx > cx) 1 else -1
            }
            while (cy != ty) {
                carve(cx, cy, if (rng.nextFloat() < 0.25f) DungeonTile.SEWER else DungeonTile.FLOOR)
                cy += if (ty > cy) 1 else -1
            }
        }

        val start = clearings.first()
        val end = clearings.last()
        val startC = start.first.average().toInt()
        val startR = start.second.average().toInt()
        val endC = end.first.average().toInt()
        val endR = end.second.average().toInt()
        tiles[idx(startC, startR)] = DungeonTile.STAIRS_UP
        tiles[idx(endC, endR)] = DungeonTile.STAIRS_DOWN

        val spawnX = startC * TILE + TILE / 2f
        val spawnY = startR * TILE + TILE / 2f
        val downX = endC * TILE + TILE / 2f
        val downY = endR * TILE + TILE / 2f

        // 숲속 은닉 보물 (둥지·낡은 상자)
        val chestTarget = (1 + floor / 2).coerceAtMost(3) + if (rng.nextFloat() < 0.4f) 1 else 0
        var chests = 0
        var chestAttempts = 0
        while (chests < chestTarget && chestAttempts < 120) {
            chestAttempts++
            val room = clearings[rng.nextInt(clearings.size)]
            val c = room.first.random(rng)
            val r = room.second.random(rng)
            if (c == startC && r == startR) continue
            if (c == endC && r == endR) continue
            val cell = tiles[idx(c, r)]
            if (cell != DungeonTile.FLOOR && cell != DungeonTile.SEWER) continue
            val x = c * TILE + TILE / 2f
            val y = r * TILE + TILE / 2f
            if (hypot(x - spawnX, y - spawnY) < TILE * 2f) continue
            if (hypot(x - downX, y - downY) < TILE * 1.5f) continue
            tiles[idx(c, r)] = DungeonTile.VAULT
            chests++
        }

        val pool = if (floor >= 3) shallowAnimals + deepAnimals else shallowAnimals
        val monsters = mutableListOf<DungeonMonster>()
        val monsterCount = 4 + floor + rng.nextInt(0, 3)
        var attempts = 0
        while (monsters.size < monsterCount && attempts < 200) {
            attempts++
            val c = rng.nextInt(1, COLS - 1)
            val r = rng.nextInt(1, ROWS - 1)
            val cell = tiles[idx(c, r)]
            if (cell == DungeonTile.WALL || cell == DungeonTile.VAULT) continue
            if (c == startC && r == startR) continue
            if (c == endC && r == endR) continue
            val x = c * TILE + TILE / 2f
            val y = r * TILE + TILE / 2f
            if (hypot(x - spawnX, y - spawnY) < TILE * 2.5f) continue
            if (monsters.any { hypot(it.x - x, it.y - y) < TILE * 1.4f }) continue
            val (kind, name) = pool.random(rng)
            val kindBonus = when (kind) {
                "bear", "giant_boar", "dire_wolf" -> 8
                "boar", "forest_spider", "stag" -> 5
                "wolf", "snake" -> 3
                "fox", "owl" -> 1
                else -> 0
            }
            monsters += DungeonMonster(
                id = "f${floor}_${monsters.size}",
                name = name,
                kind = kind,
                x = x,
                y = y,
                power = 8 + floor * 7 + kindBonus + rng.nextInt(0, 7)
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
