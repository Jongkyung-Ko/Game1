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
    val dungeonDepth: Int = 0,
    /** 도달한 동쪽 숲 최고 지대 */
    val forestDepth: Int = 0,
    /** 도달한 남쪽 사막 최고 지대 */
    val desertDepth: Int = 0,
    /** 도달한 북쪽 빙하 최고 지대 */
    val glacierDepth: Int = 0
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
    /** 고용 시 기본 전투 기여 (레벨·장비로 증가) */
    val basePower: Int,
    val desc: String,
    /** custom/chars 스프라이트 키 (얼굴·전신) */
    val spriteKey: String,
    val level: Int = 1,
    val exp: Int = 0,
    val equipment: Map<ItemType, EquippedItem> = emptyMap(),
) {
    val expToNext: Int get() = 40 + (level - 1) * 30
    val expRatio: Float get() = if (expToNext <= 0) 0f else exp.toFloat() / expToNext
    val equipAtk: Int get() = equipment.values.sumOf { it.atk }
    val equipDef: Int get() = equipment.values.sumOf { it.def }
    /** 던전 전투에 합산되는 최종 기여치 */
    val power: Int get() = basePower + (level - 1) * 2 + equipAtk + equipDef / 2
    /** 선두로 나섰을 때 버틸 수 있는 체력 */
    val maxHp: Int get() = 40 + basePower * 3 + (level - 1) * 8
    /** 역할별 공격 방식 */
    val weaponStyle: WeaponStyle get() = when (spriteKey) {
        "mage" -> WeaponStyle.MAGIC
        "rogue" -> WeaponStyle.MELEE
        else -> WeaponStyle.MELEE
    }
}

object MercenaryCatalog {
    val all = listOf(
        Mercenary("bern", "베른", "전사", 250, 12, "방패와 검으로 앞장서는 베테랑 전사.", "warrior"),
        Mercenary("shade", "셰이드", "도적", 320, 14, "그림자처럼 파고드는 쌍단검 도적.", "rogue"),
        Mercenary("elara", "엘라라", "성기사", 420, 18, "태양의 방패를 든 성기사.", "paladin"),
        Mercenary("aldric", "알드릭", "마법사", 560, 22, "해독과 화염을 다루는 노련한 마법사.", "mage"),
    )
}
