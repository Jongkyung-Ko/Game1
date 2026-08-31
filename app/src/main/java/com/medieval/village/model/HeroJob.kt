package com.medieval.village.model

/** 주인공이 게임 시작 시 고르는 직업 */
enum class HeroJob(
    val id: String,
    val label: String,
    val title: String,
    val blurb: String,
    val actorClass: ActorClass,
    val starterLine: String,
) {
    KNIGHT(
        id = "knight",
        label = "기사",
        title = "견습 기사",
        blurb = "두꺼운 갑옷과 방패로 전선을 지킨다.",
        actorClass = ActorClass.WARRIOR,
        starterLine = "HP 76 · 힘 10 · 검과 방패",
    ),
    WARRIOR(
        id = "warrior",
        label = "용사",
        title = "견습 모험가",
        blurb = "검과 방패로 무엇이든 맞선다.",
        actorClass = ActorClass.ADVENTURER,
        starterLine = "HP 60 · 균형 · 낡은 검",
    ),
    MAGE(
        id = "mage",
        label = "마법사",
        title = "견습 마법사",
        blurb = "지팡이로 마력을 쏘아낸다.",
        actorClass = ActorClass.MAGE,
        starterLine = "MP 42 · 지능 11 · 지팡이",
    ),
    ARCHER(
        id = "archer",
        label = "궁수",
        title = "견습 궁수",
        blurb = "멀리서 화살로 숨통을 끊는다.",
        actorClass = ActorClass.ARCHER,
        starterLine = "민첩 11 · 짧은 활",
    );

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
            hp = 44,
            maxHp = 44,
            mp = 42,
            maxMp = 42,
            baseAtk = 6,
            baseDef = 3,
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
