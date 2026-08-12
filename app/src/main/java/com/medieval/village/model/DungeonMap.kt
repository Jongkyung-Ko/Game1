package com.medieval.village.model

import kotlin.math.hypot
import kotlin.random.Random

enum class DungeonTile {
    WALL,
    FLOOR,
    STAIRS_UP,
    STAIRS_DOWN,
    SEWER,
    /** 닫힌 보물상자 — 열면 아이템을 얻고 CHEST_OPEN 이 된다. */
    VAULT,
    /** 이미 연 보물상자 */
    CHEST_OPEN,
    /** 포털스톤으로 연 일시 포털 — 집(HOME)으로 귀환. 던전을 떠나면 사라진다. */
    PORTAL
}

data class DungeonMonster(
    val id: String,
    val name: String,
    /** shambler / runner / bloater / armored / blacksmith / farmer / golem / boss_* */
    val kind: String,
    var x: Float,
    var y: Float,
    val power: Int,
    /** 10층마다 등장하는 보스 */
    val isBoss: Boolean = false,
    var alive: Boolean = true,
    var hp: Int = (power * 4).coerceAtLeast(24),
    val maxHp: Int = (power * 4).coerceAtLeast(24),
    /** 연출·AI 상태 (탐험 중 갱신) */
    var facingLeft: Boolean = false,
    var moving: Boolean = false,
    var attacking: Boolean = false,
    var animFrame: Int = 0,
    var animTime: Float = 0f,
    var attackCooldown: Float = 0f,
    var attackHitApplied: Boolean = false,
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

    fun setTile(col: Int, row: Int, tile: DungeonTile) {
        if (col !in 0 until cols || row !in 0 until rows) return
        tiles[row * cols + col] = tile
    }

    fun clearPortals(replaceWith: DungeonTile = DungeonTile.FLOOR) {
        for (i in tiles.indices) {
            if (tiles[i] == DungeonTile.PORTAL) tiles[i] = replaceWith
        }
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

    private val zombieKinds = listOf(
        "shambler" to "심블러 좀비",
        "runner" to "러너 좀비",
        "bloater" to "블로터 좀비",
        "armored" to "갑옷 좀비",
        "blacksmith" to "빙의된 대장장이",
        "farmer" to "감염된 농부",
        "golem" to "저주받은 선생님",
    )

    /** 10 / 20 / 30층… 순환 보스 */
    private val bossRoster = listOf(
        "boss_warden" to "지하 감시자",
        "boss_abomination" to "역병 흉물",
        "boss_lich" to "저주받은 리치",
    )

    fun isBossFloor(floor: Int): Boolean = floor > 0 && floor % 10 == 0

    fun bossForFloor(floor: Int): Pair<String, String> {
        val idx = ((floor / 10) - 1).coerceAtLeast(0) % bossRoster.size
        return bossRoster[idx]
    }

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

        // 보물상자: 층마다 1~3개 정도, 계단에서 떨어진 바닥/하수도에 배치
        val chestTarget = (1 + floor / 2).coerceAtMost(3) + if (rng.nextFloat() < 0.35f) 1 else 0
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
        val bossFloor = isBossFloor(floor)
        // 보스층은 일반 몹을 조금 줄여 보스가 돋보이게
        val monsterCount = (4 + floor + rng.nextInt(0, 3) - if (bossFloor) 2 else 0).coerceAtLeast(3)
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
            val (kind, name) = zombieKinds.random(rng)
            val kindBonus = when (kind) {
                "bloater", "armored", "blacksmith" -> 6
                "runner" -> 3
                "golem" -> 4
                else -> 0
            }
            monsters += DungeonMonster(
                id = "z${floor}_${monsters.size}",
                name = name,
                kind = kind,
                x = x,
                y = y,
                power = 10 + floor * 8 + kindBonus + rng.nextInt(0, 8)
            )
        }

        if (bossFloor) {
            val (kind, name) = bossForFloor(floor)
            val power = 28 + floor * 16 + rng.nextInt(0, 12)
            val maxHp = (power * 11).coerceAtLeast(180)
            // 하층 계단 근처 끝방 — 내려가기 전 마주치게
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
                    id = "boss_${floor}",
                    name = name,
                    kind = kind,
                    x = bx,
                    y = by,
                    power = power,
                    isBoss = true,
                    hp = maxHp,
                    maxHp = maxHp,
                )
                placed = true
                break
            }
            if (!placed) {
                val bx = (downX - TILE * 1.2f).coerceIn(TILE * 1.5f, (COLS - 1.5f) * TILE)
                val by = downY.coerceIn(TILE * 1.5f, (ROWS - 1.5f) * TILE)
                monsters += DungeonMonster(
                    id = "boss_${floor}",
                    name = name,
                    kind = kind,
                    x = bx,
                    y = by,
                    power = power,
                    isBoss = true,
                    hp = maxHp,
                    maxHp = maxHp,
                )
            }
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

    private fun isWalkableTile(tiles: Array<DungeonTile>, x: Float, y: Float): Boolean {
        val c = (x / TILE).toInt()
        val r = (y / TILE).toInt()
        if (c !in 0 until COLS || r !in 0 until ROWS) return false
        return tiles[r * COLS + c] != DungeonTile.WALL
    }
}
