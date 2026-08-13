package com.medieval.village.model

/** 전투용 특별스킬 직업군 (주인공=모험가, 용병=역할) */
enum class ActorClass(val label: String) {
    ADVENTURER("모험가"),
    WARRIOR("전사"),
    ROGUE("도적"),
    MAGE("마법사"),
    PALADIN("성기사"),
}

data class SpecialSkillDef(
    val id: String,
    val name: String,
    val shortName: String,
    val actorClass: ActorClass,
    val unlockLevel: Int,
    val mpCost: Int,
    /** 일반 공격 대비 배수 (~3배) */
    val damageMult: Float,
    val style: WeaponStyle,
    val desc: String,
)

data class SkillSlotUi(
    val slotIndex: Int,
    val skillId: String?,
    val shortName: String,
    val enabled: Boolean,
    val mpCost: Int,
)

/** 레벨업 시 슬롯 재배치용 제안 */
data class LevelUpSkillOffer(
    val actorKey: String,
    val actorName: String,
    val actorClass: ActorClass,
    val newLevel: Int,
    val newlyUnlockedIds: List<String>,
)

object SpecialSkillCatalog {
    const val MAX_SLOTS = 3

    val all: List<SpecialSkillDef> = buildList {
        // —— 모험가 (주인공) ——
        addAll(
            listOf(
                SpecialSkillDef("adv_smash", "강타", "강타", ActorClass.ADVENTURER, 2, 8, 3.0f, WeaponStyle.MELEE, "힘을 모아 내려찍는다."),
                SpecialSkillDef("adv_flurry", "연속베기", "연베", ActorClass.ADVENTURER, 3, 10, 2.9f, WeaponStyle.MELEE, "빠른 참격으로 몰아친다."),
                SpecialSkillDef("adv_charge", "돌진", "돌진", ActorClass.ADVENTURER, 4, 11, 3.0f, WeaponStyle.MELEE, "적에게 쇄도하며 벤다."),
                SpecialSkillDef("adv_shot", "집중사격", "집중", ActorClass.ADVENTURER, 5, 12, 3.0f, WeaponStyle.BOW, "호흡을 가다듬고 한 발을 쏜다."),
                SpecialSkillDef("adv_bolt", "화염탄", "화탄", ActorClass.ADVENTURER, 6, 14, 3.1f, WeaponStyle.MAGIC, "조잡하지만 뜨거운 마력탄."),
                SpecialSkillDef("adv_finisher", "필살일격", "필살", ActorClass.ADVENTURER, 7, 16, 3.4f, WeaponStyle.MELEE, "모든 힘을 실은 최후의 일격."),
            )
        )
        // —— 전사 ——
        addAll(
            listOf(
                SpecialSkillDef("war_kill", "일격필살", "필살", ActorClass.WARRIOR, 2, 9, 3.2f, WeaponStyle.MELEE, "약점을 노린 치명타."),
                SpecialSkillDef("war_bash", "방패강타", "강타", ActorClass.WARRIOR, 3, 10, 2.8f, WeaponStyle.MELEE, "방패로 강하게 들이받는다."),
                SpecialSkillDef("war_spin", "회전참", "회전", ActorClass.WARRIOR, 4, 12, 3.0f, WeaponStyle.MELEE, "몸을 돌리며 넓게 벤다."),
                SpecialSkillDef("war_rush", "돌진베기", "돌진", ActorClass.WARRIOR, 5, 13, 3.1f, WeaponStyle.MELEE, "기세를 몰아 돌진 참격."),
                SpecialSkillDef("war_rage", "분노의일격", "분노", ActorClass.WARRIOR, 6, 15, 3.3f, WeaponStyle.MELEE, "분노를 담아 내려찍는다."),
                SpecialSkillDef("war_quake", "대지분쇄", "분쇄", ActorClass.WARRIOR, 7, 17, 3.5f, WeaponStyle.MELEE, "대지를 울리는 초강력 일격."),
            )
        )
        // —— 도적 ——
        addAll(
            listOf(
                SpecialSkillDef("rog_assassinate", "암살", "암살", ActorClass.ROGUE, 2, 9, 3.2f, WeaponStyle.MELEE, "그림자에서 급소를 찌른다."),
                SpecialSkillDef("rog_stab", "그림자찌르기", "찌름", ActorClass.ROGUE, 3, 10, 2.9f, WeaponStyle.MELEE, "보이지 않는 각도에서 찌른다."),
                SpecialSkillDef("rog_smoke", "연막습격", "연막", ActorClass.ROGUE, 4, 11, 2.8f, WeaponStyle.MELEE, "연막 속에서 파고든다."),
                SpecialSkillDef("rog_vital", "급소베기", "급소", ActorClass.ROGUE, 5, 13, 3.1f, WeaponStyle.MELEE, "혈을 노려 깊게 벤다."),
                SpecialSkillDef("rog_dual", "이중검무", "쌍검", ActorClass.ROGUE, 6, 15, 3.2f, WeaponStyle.MELEE, "쌍단검으로 연속 타격."),
                SpecialSkillDef("rog_execute", "처형", "처형", ActorClass.ROGUE, 7, 17, 3.5f, WeaponStyle.MELEE, "숨통을 끊는 최후 처형."),
            )
        )
        // —— 마법사 ——
        addAll(
            listOf(
                SpecialSkillDef("mag_blast", "화염폭발", "화폭", ActorClass.MAGE, 2, 10, 3.0f, WeaponStyle.MAGIC, "화염을 폭발시킨다."),
                SpecialSkillDef("mag_ice", "빙결창", "빙결", ActorClass.MAGE, 3, 11, 2.9f, WeaponStyle.MAGIC, "얼음 창을 쏘아보낸다."),
                SpecialSkillDef("mag_chain", "번개연쇄", "번개", ActorClass.MAGE, 4, 13, 3.1f, WeaponStyle.MAGIC, "번개가 적을 관통한다."),
                SpecialSkillDef("mag_orb", "마력탄", "마탄", ActorClass.MAGE, 5, 12, 3.0f, WeaponStyle.MAGIC, "응축된 마력탄을 발사한다."),
                SpecialSkillDef("mag_meteor", "운석", "운석", ActorClass.MAGE, 6, 16, 3.3f, WeaponStyle.MAGIC, "작은 운석을 떨어뜨린다."),
                SpecialSkillDef("mag_ruin", "멸마의빛", "멸마", ActorClass.MAGE, 7, 18, 3.5f, WeaponStyle.MAGIC, "마력을 쏟아붓는 최종 주문."),
            )
        )
        // —— 성기사 ——
        addAll(
            listOf(
                SpecialSkillDef("pal_smite", "성스러운일격", "성격", ActorClass.PALADIN, 2, 9, 3.0f, WeaponStyle.MELEE, "성스러운 힘으로 타격한다."),
                SpecialSkillDef("pal_judge", "심판", "심판", ActorClass.PALADIN, 3, 11, 3.1f, WeaponStyle.MELEE, "악을 단죄하는 일격."),
                SpecialSkillDef("pal_slash", "성광참", "성광", ActorClass.PALADIN, 4, 12, 2.9f, WeaponStyle.MELEE, "빛줄기를 담아 벤다."),
                SpecialSkillDef("pal_guard", "수호의검", "수호", ActorClass.PALADIN, 5, 13, 2.8f, WeaponStyle.MELEE, "수호의 기운을 실은 참격."),
                SpecialSkillDef("pal_wrath", "천벌", "천벌", ActorClass.PALADIN, 6, 15, 3.3f, WeaponStyle.MELEE, "하늘이 내리는 벌."),
                SpecialSkillDef("pal_holy", "성역강림", "성역", ActorClass.PALADIN, 7, 17, 3.5f, WeaponStyle.MELEE, "성역을 불러 적을 분쇄한다."),
            )
        )
    }

    fun byId(id: String): SpecialSkillDef? = all.firstOrNull { it.id == id }

    fun forClass(cls: ActorClass): List<SpecialSkillDef> =
        all.filter { it.actorClass == cls }.sortedBy { it.unlockLevel }

    fun unlocksAt(cls: ActorClass, level: Int): List<SpecialSkillDef> =
        forClass(cls).filter { it.unlockLevel == level }

    fun unlockedUpTo(cls: ActorClass, level: Int): List<SpecialSkillDef> =
        forClass(cls).filter { it.unlockLevel <= level }

    fun actorClassOf(merc: Mercenary?): ActorClass = when (merc?.spriteKey) {
        "warrior" -> ActorClass.WARRIOR
        "rogue" -> ActorClass.ROGUE
        "mage" -> ActorClass.MAGE
        "paladin" -> ActorClass.PALADIN
        else -> ActorClass.ADVENTURER
    }

    fun actorClassOfSprite(spriteKey: String): ActorClass = when (spriteKey) {
        "warrior" -> ActorClass.WARRIOR
        "rogue" -> ActorClass.ROGUE
        "mage" -> ActorClass.MAGE
        "paladin" -> ActorClass.PALADIN
        else -> ActorClass.ADVENTURER
    }
}
