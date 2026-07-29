package com.medieval.village.model

data class Player(
    val name: String = "아서",
    val title: String = "견습 모험가",
    val level: Int = 1,
    val exp: Int = 0,
    val hp: Int = 60,
    val maxHp: Int = 60,
    val mp: Int = 20,
    val maxMp: Int = 20,
    val baseAtk: Int = 8,
    val baseDef: Int = 4,
    val str: Int = 8,
    val agi: Int = 6,
    val intel: Int = 5,
    val luck: Int = 5,
    val gold: Int = 300,
    val day: Int = 1,
    /** 교회 축복 남은 일수 */
    val blessing: Int = 0,
    /** 도달한 던전 최고 층 */
    val dungeonDepth: Int = 0
) {
    val expToNext: Int get() = 60 + (level - 1) * 45
    val hpRatio: Float get() = if (maxHp <= 0) 0f else hp.toFloat() / maxHp
    val mpRatio: Float get() = if (maxMp <= 0) 0f else mp.toFloat() / maxMp
    val expRatio: Float get() = exp.toFloat() / expToNext
}

data class Skill(
    val id: String,
    val name: String,
    val cost: Int,
    val mpCost: Int,
    val power: Int,
    val desc: String
)

object SkillCatalog {
    val all = listOf(
        Skill("fireball", "화염구", 220, 8, 18, "적을 불태운다. 던전 전투력이 크게 오른다."),
        Skill("heal", "치유술", 300, 10, 0, "전투 중 스스로를 회복한다."),
        Skill("thunder", "번개창", 620, 16, 34, "상급 공격 마법."),
        Skill("barrier", "마법 방벽", 480, 12, 0, "피해를 줄여준다.")
    )
}

data class Mercenary(
    val id: String,
    val name: String,
    val role: String,
    val cost: Int,
    val power: Int,
    val desc: String
)

object MercenaryCatalog {
    val all = listOf(
        Mercenary("bern", "베른", "검사", 250, 10, "묵묵히 앞장서는 베테랑 검사."),
        Mercenary("lyra", "리라", "궁수", 320, 13, "백발백중의 사냥꾼."),
        Mercenary("gorm", "고름", "방패병", 400, 16, "산더미 같은 체격의 방패병."),
        Mercenary("sela", "셀라", "마법사", 560, 22, "마법학교를 갓 졸업한 재원.")
    )
}
