package com.medieval.village.model

import kotlin.math.hypot
import kotlin.random.Random

/**
 * Gray Castle 고성 던전 — 해골·유령 병력이 지키는 10층.
 * 10층을 모두 정리하면 저주가 풀려 White Castle이 된다.
 */
object CastleFactory {

    const val TILE = DungeonFactory.TILE
    const val COLS = DungeonFactory.COLS
    const val ROWS = DungeonFactory.ROWS
    const val MAX_FLOOR = 10

    private val undeadKinds = listOf(
        "skel_soldier" to "해골병사",
        "ghost_cavalry" to "유령기마병",
        "skel_archer" to "해골궁수",
    )

    private val boss = "boss_skel_king" to "해골 왕"

    fun isFinalFloor(floor: Int): Boolean = floor >= MAX_FLOOR

    fun generate(floor: Int, seed: Int = floor * 8803 + 117): DungeonFloor {
        val f = floor.coerceIn(1, MAX_FLOOR)
        val rng = Random(seed)
        val tiles = Array(COLS * ROWS) { DungeonTile.WALL }

        fun idx(c: Int, r: Int) = r * COLS + c
        fun carve(c: Int, r: Int, kind: DungeonTile = DungeonTile.FLOOR) {
            if (c in 1 until COLS - 1 && r in 1 until ROWS - 1) {
                tiles[idx(c, r)] = kind
            }
        }

        val rooms = mutableListOf<Pair<IntRange, IntRange>>()
        val roomCount = 5 + f.coerceAtMost(4)
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
                    rng.nextFloat() < 0.08f -> DungeonTile.SEWER
                    else -> DungeonTile.FLOOR
                }
                carve(c, r, kind)
            }
        }

        for (i in 0 until rooms.lastIndex) {
            val (aC, aR) = rooms[i]
            val (bC, bR) = rooms[i + 1]
            var cx = aC.average().toInt()
            var cy = aR.average().toInt()
            val tx = bC.average().toInt()
            val ty = bR.average().toInt()
            while (cx != tx) {
                carve(cx, cy)
                cx += if (tx > cx) 1 else -1
            }
            while (cy != ty) {
                carve(cx, cy)
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
        // 최심층에는 하층 계단이 없다 — 저주를 끊는 결전장
        if (!isFinalFloor(f)) {
            tiles[idx(endC, endR)] = DungeonTile.STAIRS_DOWN
        }

        val spawnX = startC * TILE + TILE / 2f
        val spawnY = startR * TILE + TILE / 2f
        val downX = endC * TILE + TILE / 2f
        val downY = endR * TILE + TILE / 2f

        val chestTarget = (1 + f / 2).coerceAtMost(3) + if (rng.nextFloat() < 0.35f) 1 else 0
        var chests = 0
        var chestAttempts = 0
        while (chests < chestTarget && chestAttempts < 120) {
            chestAttempts++
            val room = rooms[rng.nextInt(rooms.size)]
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

        val monsters = mutableListOf<DungeonMonster>()
        val finalFloor = isFinalFloor(f)
        val monsterCount = (4 + f + rng.nextInt(0, 3) - if (finalFloor) 2 else 0).coerceAtLeast(3)
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
            val (kind, name) = undeadKinds.random(rng)
            val kindBonus = when (kind) {
                "ghost_cavalry" -> 9
                "skel_archer" -> 5
                else -> 3
            }
            monsters += DungeonMonster(
                id = "c${f}_${monsters.size}",
                name = name,
                kind = kind,
                x = x,
                y = y,
                power = 16 + f * 11 + kindBonus + rng.nextInt(0, 10),
                armor = 2 + f / 3,
            )
        }

        if (finalFloor) {
            val (kind, name) = boss
            val power = 62 + f * 24 + rng.nextInt(0, 16)
            val maxHp = (power * 15).coerceAtLeast(360)
            val offsets = listOf(
                -TILE * 1.6f to 0f,
                TILE * 1.6f to 0f,
                0f to -TILE * 1.4f,
                0f to TILE * 1.4f,
            )
            var placed = false
            for ((ox, oy) in offsets.shuffled(rng)) {
                val bx = (downX + ox).coerceIn(TILE * 1.5f, (COLS - 1.5f) * TILE)
                val by = (downY + oy).coerceIn(TILE * 1.5f, (ROWS - 1.5f) * TILE)
                if (!isWalkableTile(tiles, bx, by)) continue
                if (hypot(bx - spawnX, by - spawnY) < TILE * 3f) continue
                monsters += DungeonMonster(
                    id = "boss_castle_$f",
                    name = "중간 보스 · $name",
                    kind = kind,
                    x = bx,
                    y = by,
                    power = power,
                    isBoss = true,
                    hp = maxHp,
                    maxHp = maxHp,
                    armor = 12,
                )
                placed = true
                break
            }
            if (!placed) {
                monsters += DungeonMonster(
                    id = "boss_castle_$f",
                    name = "중간 보스 · $name",
                    kind = kind,
                    x = downX,
                    y = downY,
                    power = power,
                    isBoss = true,
                    hp = maxHp,
                    maxHp = maxHp,
                    armor = 12,
                )
            }
        }

        return DungeonFloor(
            floor = f,
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

    private fun isWalkableTile(tiles: Array<DungeonTile>, x: Float, y: Float): Boolean {
        val c = (x / TILE).toInt()
        val r = (y / TILE).toInt()
        if (c !in 0 until COLS || r !in 0 until ROWS) return false
        return tiles[r * COLS + c] != DungeonTile.WALL
    }
}
