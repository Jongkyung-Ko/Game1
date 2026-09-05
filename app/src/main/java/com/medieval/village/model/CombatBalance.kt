package com.medieval.village.model

import kotlin.math.roundToInt
import kotlin.random.Random

/** 보스 등급 — 중간(10층) / 최종(클리어 층) */
enum class BossTier { NONE, MID, FINAL }

/**
 * 탐험 지대. 마을 난이도 사다리에 맞춰 몹 ATK·HP·방어를 뽑는다.
 * 오크헤이븐 무한층은 [levelCap]에서 멈춰 스토리 던전을 건너뛰지 못하게 한다.
 */
enum class BalanceZone(
    val labelKo: String,
    val village: SettlementId,
    val enterLevel: Float,
    val clearLevel: Float,
    val clearFloor: Int,
    val levelCap: Float,
    val stars: String,
) {
    FOREST("동쪽 숲", SettlementId.OAKHAVEN, 1f, 5f, 10, 10f, "★☆☆☆☆"),
    DESERT("남쪽 사막", SettlementId.OAKHAVEN, 2f, 6f, 10, 10f, "★☆☆☆☆"),
    GLACIER("북쪽 빙하", SettlementId.OAKHAVEN, 3f, 7f, 10, 11f, "★★☆☆☆"),
    DUNGEON("좀비 던전", SettlementId.OAKHAVEN, 4f, 8f, 10, 12f, "★★☆☆☆"),
    CASTLE("회색 성", SettlementId.GRAY_CASTLE, 8f, 12f, 10, 12f, "★★★☆☆"),
    IGLOO("이글루 빙하", SettlementId.IGLOO, 10f, 16f, 20, 16f, "★★★☆☆"),
    SEA("바다 동굴", SettlementId.SEASIDE, 13f, 18f, 20, 18f, "★★★★☆"),
    WINTER("겨울성 지하", SettlementId.WINTER_CASTLE, 15f, 20f, 20, 20f, "★★★★★"),
    ;

    fun floorWord(n: Int): String = when (this) {
        FOREST, DESERT, GLACIER, IGLOO -> "${n}지대"
        else -> "${n}층"
    }
}

data class VillageLadderRow(
    val id: SettlementId,
    val order: Int,
    val nameKo: String,
    val stars: String,
    val difficulty: String,
    val enterLo: Int,
    val enterHi: Int,
    val clearLo: Int,
    val clearHi: Int,
    val clearNote: String,
    val promotion: String,
)

data class ExpectedHero(
    val level: Int,
    val hp: Int,
    val atk: Int,
    val def: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val equipAtk: Int,
    val equipDef: Int,
)

data class MonsterRoll(
    val power: Int,
    val hp: Int,
    val armor: Int,
    val targetLevel: Float,
)

/**
 * 마을별 난이도·적정 레벨과, 그 구간에 맞춘 몬스터 공격/수비/체력.
 *
 * 기사 + 상점 장비 가정:
 * - 잡몹은 권장 레벨에서 HP의 약 20%(5대)를 넣고, 기본 공격 4~5대에 쓰러진다.
 * - 보스는 한 방이 나지 않게 HP의 약 33%(3대)이며, 기본 공격 10~13대(특수기 3~4회)다.
 */
object CombatBalance {

    const val BOSS_HIT_MULT = 1.40f
    const val RANGED_HIT_MULT = 0.85f
    const val MIN_HIT = 3

    val villages: List<VillageLadderRow> = listOf(
        VillageLadderRow(
            SettlementId.OAKHAVEN, 1, "오크헤이븐", "★☆☆☆☆", "초반",
            1, 1, 5, 8, "던전·숲 10층", "1차 (Lv5)",
        ),
        VillageLadderRow(
            SettlementId.ASHBROOK, 2, "애쉬브룩", "★★☆☆☆", "초중반",
            4, 6, 6, 8, "같은 루트 심층", "—",
        ),
        VillageLadderRow(
            SettlementId.GRAY_CASTLE, 3, "회색 성", "★★★☆☆", "중반",
            8, 10, 10, 12, "해골왕 (10층)", "2차 (Lv10)",
        ),
        VillageLadderRow(
            SettlementId.IGLOO, 4, "이글루 마을", "★★★☆☆", "중후반",
            10, 12, 14, 16, "얼음북극곰 (20층)", "—",
        ),
        VillageLadderRow(
            SettlementId.SEASIDE, 5, "바닷가 폐허", "★★★★☆", "후반",
            13, 15, 15, 18, "대왕문어 (20층)", "3차 (Lv15)",
        ),
        VillageLadderRow(
            SettlementId.WINTER_CASTLE, 6, "겨울성", "★★★★★", "최종",
            15, 17, 18, 20, "납치범 두목 (20층)", "각성 (Lv20)",
        ),
    )

    fun village(id: SettlementId): VillageLadderRow =
        villages.first { it.id == id }

    fun zoneOf(place: PlaceId?): BalanceZone? = when (place) {
        PlaceId.EAST_FOREST -> BalanceZone.FOREST
        PlaceId.SOUTH_DESERT -> BalanceZone.DESERT
        PlaceId.NORTH_GLACIER -> BalanceZone.GLACIER
        PlaceId.DUNGEON -> BalanceZone.DUNGEON
        PlaceId.GRAY_CASTLE -> BalanceZone.CASTLE
        PlaceId.IGLOO_GLACIER -> BalanceZone.IGLOO
        PlaceId.SEA_CAVE -> BalanceZone.SEA
        PlaceId.WINTER_KEEP -> BalanceZone.WINTER
        else -> null
    }

    /** 이 층의 권장 레벨 (1층=입장, 클리어층=클리어 레벨). */
    fun targetLevel(zone: BalanceZone, floor: Int): Float {
        val f = floor.coerceAtLeast(1).toFloat()
        val span = (zone.clearFloor - 1).coerceAtLeast(1).toFloat()
        val along = if (f <= zone.clearFloor) {
            zone.enterLevel + (zone.clearLevel - zone.enterLevel) * (f - 1f) / span
        } else {
            zone.clearLevel + (f - zone.clearFloor) * 0.25f
        }
        return along.coerceIn(1f, zone.levelCap)
    }

    fun heroAt(level: Int): ExpectedHero {
        val lv = level.coerceIn(1, CURVE.last().level)
        return CURVE[lv - 1]
    }

    fun heroAt(level: Float): ExpectedHero {
        val lo = level.toInt().coerceIn(1, 20)
        val hi = (lo + 1).coerceAtMost(20)
        val t = (level - lo).coerceIn(0f, 1f)
        if (t <= 0.001f || lo == hi) return heroAt(lo)
        val a = heroAt(lo)
        val b = heroAt(hi)
        return ExpectedHero(
            level = if (t < 0.5f) lo else hi,
            hp = lerp(a.hp, b.hp, t),
            atk = lerp(a.atk, b.atk, t),
            def = lerp(a.def, b.def, t),
            baseAtk = lerp(a.baseAtk, b.baseAtk, t),
            baseDef = lerp(a.baseDef, b.baseDef, t),
            equipAtk = lerp(a.equipAtk, b.equipAtk, t),
            equipDef = lerp(a.equipDef, b.equipDef, t),
        )
    }

    fun roll(
        zone: BalanceZone,
        floor: Int,
        kindBonus: Int = 0,
        rng: Random = Random.Default,
        boss: BossTier = BossTier.NONE,
    ): MonsterRoll {
        val target = targetLevel(zone, floor)
        val hero = heroAt(target)
        val hitsOnPlayer = when (boss) {
            BossTier.NONE -> 5.0f
            BossTier.MID -> 3.2f
            BossTier.FINAL -> 3.0f
        }
        val hitsToKill = when (boss) {
            BossTier.NONE -> 4.2f
            BossTier.MID -> 10.0f
            BossTier.FINAL -> 13.0f
        }
        val desiredHit = (hero.hp / hitsOnPlayer).coerceAtLeast(MIN_HIT.toFloat())
        val incoming = if (boss == BossTier.NONE) {
            desiredHit
        } else {
            desiredHit / BOSS_HIT_MULT
        }
        val kindExtra = (kindBonus * hero.atk / 50f).roundToInt()
        val spread = (2 + hero.atk / 22).coerceAtLeast(3)
        val jitter = if (spread <= 1) 0 else rng.nextInt(0, spread)
        val power = (hero.def + incoming + kindExtra + jitter).roundToInt().coerceAtLeast(6)
        val armor = when (boss) {
            BossTier.NONE -> (hero.atk * 0.06f).roundToInt().coerceIn(0, 16)
            BossTier.MID -> (hero.atk * 0.10f).roundToInt().coerceIn(4, 28)
            BossTier.FINAL -> (hero.atk * 0.12f).roundToInt().coerceIn(6, 36)
        }
        val strike = (hero.atk - armor).coerceAtLeast(6)
        val hp = (strike * hitsToKill).roundToInt().coerceAtLeast(
            when (boss) {
                BossTier.NONE -> 24
                BossTier.MID -> 160
                BossTier.FINAL -> 280
            }
        )
        return MonsterRoll(power = power, hp = hp, armor = armor, targetLevel = target)
    }

    fun meleeHit(power: Int, defense: Int, boss: Boolean): Int {
        val base = (power - defense).coerceAtLeast(MIN_HIT)
        return if (boss) {
            (base * BOSS_HIT_MULT).toInt().coerceAtLeast(base + 4)
        } else {
            base
        }
    }

    fun rangedHit(power: Int, defense: Int): Int {
        val base = (power - defense).coerceAtLeast(MIN_HIT)
        return (base * RANGED_HIT_MULT).roundToInt().coerceAtLeast(2)
    }

    fun playerHit(atk: Int, armor: Int): Int = (atk - armor).coerceAtLeast(1)

    fun enterLabel(row: VillageLadderRow): String =
        if (row.enterLo == row.enterHi) "${row.enterLo}" else "${row.enterLo}–${row.enterHi}"

    fun clearLabel(row: VillageLadderRow): String = "${row.clearLo}–${row.clearHi}"

    fun milestoneHeroes(): List<ExpectedHero> = listOf(1, 5, 10, 12, 15, 16, 18, 20).map { heroAt(it) }

    private fun lerp(a: Int, b: Int, t: Float): Int = (a + (b - a) * t).roundToInt()

    private fun assumedEquipAtk(level: Int): Int = when {
        level >= 15 -> 26
        level >= 10 -> 24
        level >= 5 -> 13
        else -> 5
    }

    private fun assumedEquipDef(level: Int): Int = when {
        level >= 15 -> 28
        level >= 10 -> 19
        level >= 5 -> 11
        else -> 4
    }

    private val CURVE: List<ExpectedHero> = buildList {
        var p = HeroJob.KNIGHT.startingPlayer()
        fun snap(): ExpectedHero {
            val eqAtk = assumedEquipAtk(p.level)
            val eqDef = assumedEquipDef(p.level)
            return ExpectedHero(
                level = p.level,
                hp = p.maxHp,
                atk = p.baseAtk + eqAtk + p.str / 2,
                def = p.baseDef + eqDef + p.agi / 3,
                baseAtk = p.baseAtk,
                baseDef = p.baseDef,
                equipAtk = eqAtk,
                equipDef = eqDef,
            )
        }
        add(snap())
        for (lv in 2..20) {
            val m = HeroAdvancement.growthMultAt(lv)
            p = p.copy(
                level = lv,
                maxHp = p.maxHp + 12 * m,
                baseAtk = p.baseAtk + 2 * m,
                baseDef = p.baseDef + 1 * m,
                str = p.str + 1 * m,
                agi = p.agi + 1 * m,
            )
            add(snap())
        }
    }
}
