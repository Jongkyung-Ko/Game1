package com.medieval.village.model

/** 레벨 5/10/15/20 전직. 성장 배수는 이전 단계의 3배. */
object HeroAdvancement {
    val LEVELS = listOf(5, 10, 15, 20)
    const val MAX_SPRITE_RANK = 3

    fun rankIndexAt(level: Int): Int = when {
        level >= 20 -> 4
        level >= 15 -> 3
        level >= 10 -> 2
        level >= 5 -> 1
        else -> 0
    }

    fun spriteRankAt(level: Int): Int = rankIndexAt(level).coerceIn(0, MAX_SPRITE_RANK)

    /** 해당 레벨에 도달했을 때 적용할 성장 배수 (1, 3, 9, 27, 81) */
    fun growthMultAt(level: Int): Int {
        var m = 1
        repeat(rankIndexAt(level)) { m *= 3 }
        return m
    }

    fun isAdvanceLevel(level: Int): Boolean = level in LEVELS
}

data class HeroRankDef(
    val rank: Int,
    val unlockLevel: Int,
    val title: String,
)

data class JobAdvanceOffer(
    val job: HeroJob,
    val fromTitle: String,
    val toTitle: String,
    val newLevel: Int,
    val spriteRank: Int,
    val growthMult: Int,
    val awakening: Boolean,
)

/** 주인공이 게임 시작 시 고르는 직업 */
enum class HeroJob(
    val id: String,
    val label: String,
    val title: String,
    val blurb: String,
    val actorClass: ActorClass,
    val starterLine: String,
    val pathLine: String,
    private val rankTitles: List<String>,
) {
    KNIGHT(
        id = "knight",
        label = "기사",
        title = "견습 기사",
        blurb = "두꺼운 갑옷과 방패로 전선을 지킨다.",
        actorClass = ActorClass.WARRIOR,
        starterLine = "HP 76 · 힘 10 · 검과 방패",
        pathLine = "기사 → 수호기사 → 성기사 → 천상의 성벽",
        rankTitles = listOf("견습 기사", "수호기사", "성기사", "천상의 성벽", "천상의 성벽"),
    ),
    WARRIOR(
        id = "warrior",
        label = "용사",
        title = "견습 모험가",
        blurb = "검과 방패로 무엇이든 맞선다.",
        actorClass = ActorClass.ADVENTURER,
        starterLine = "HP 60 · 균형 · 낡은 검",
        pathLine = "용사 → 광전사 → 전쟁군주 → 종말의 검성",
        rankTitles = listOf("견습 모험가", "광전사", "전쟁군주", "종말의 검성", "종말의 검성"),
    ),
    MAGE(
        id = "mage",
        label = "마법사",
        title = "견습 마법사",
        blurb = "지팡이로 마력을 쏘아낸다.",
        actorClass = ActorClass.MAGE,
        starterLine = "HP 56 · MP 42 · 지능 11 · 지팡이",
        pathLine = "마법사 → 원소술사 → 대마도사 → 세계의 아크메이지",
        rankTitles = listOf("견습 마법사", "원소술사", "대마도사", "세계의 아크메이지", "세계의 아크메이지"),
    ),
    ARCHER(
        id = "archer",
        label = "궁수",
        title = "견습 궁수",
        blurb = "멀리서 화살로 숨통을 끊는다.",
        actorClass = ActorClass.ARCHER,
        starterLine = "민첩 11 · 짧은 활",
        pathLine = "궁수 → 사냥꾼 → 저격수 → 별빛의 명사수",
        rankTitles = listOf("견습 궁수", "사냥꾼", "저격수", "별빛의 명사수", "별빛의 명사수"),
    );

    fun titleAt(level: Int): String {
        val i = HeroAdvancement.rankIndexAt(level).coerceIn(0, rankTitles.lastIndex)
        return rankTitles[i]
    }

    fun ranks(): List<HeroRankDef> = listOf(
        HeroRankDef(0, 1, rankTitles[0]),
        HeroRankDef(1, 5, rankTitles[1]),
        HeroRankDef(2, 10, rankTitles[2]),
        HeroRankDef(3, 15, rankTitles[3]),
        HeroRankDef(4, 20, rankTitles[4]),
    )

    fun startingPlayer(): Player = when (this) {
        KNIGHT -> Player(
            title = title,
            heroJob = this,
            hp = 76,
            maxHp = 76,
            mp = 12,
            maxMp = 12,
            baseAtk = 9,
            baseDef = 7,
            str = 10,
            agi = 4,
            intel = 4,
            luck = 5,
        )
        WARRIOR -> Player(
            title = title,
            heroJob = this,
        )
        MAGE -> Player(
            title = title,
            heroJob = this,
            hp = 56,
            maxHp = 56,
            mp = 42,
            maxMp = 42,
            baseAtk = 6,
            baseDef = 5,
            str = 4,
            agi = 5,
            intel = 11,
            luck = 5,
        )
        ARCHER -> Player(
            title = title,
            heroJob = this,
            hp = 52,
            maxHp = 52,
            mp = 16,
            maxMp = 16,
            baseAtk = 8,
            baseDef = 4,
            str = 6,
            agi = 11,
            intel = 5,
            luck = 6,
        )
    }

    companion object {
        fun fromId(id: String?): HeroJob =
            entries.firstOrNull { it.id == id || it.name == id } ?: WARRIOR
    }
}
