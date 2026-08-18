package com.medieval.village.model

/** 전투용 특별스킬 직업군 (주인공=모험가, 용병=역할) */
enum class ActorClass(val label: String) {
    ADVENTURER("모험가"),
    WARRIOR("전사"),
    ROGUE("도적"),
    MAGE("마법사"),
    PALADIN("성기사"),
}

/**
 * 직업 전용 스킬 정의.
 * - [actorClass] 직군만 배울 수 있다.
 * - [requires] 선행 스킬을 모두 배운 뒤에만 습득 가능.
 * - [mapCol]/[mapRow] 스킬맵 배치.
 */
data class SpecialSkillDef(
    val id: String,
    val name: String,
    val shortName: String,
    val actorClass: ActorClass,
    /** 캐릭터 레벨이 이 이상이어야 습득 가능 */
    val unlockLevel: Int,
    val mpCost: Int,
    /** 1랭크 기준 피해 배수 */
    val damageMult: Float,
    val style: WeaponStyle,
    val desc: String,
    /** 선행 스킬 id (모두 습득 필요) */
    val requires: List<String> = emptyList(),
    val mapCol: Int = 0,
    val mapRow: Int = 0,
    val maxRank: Int = SpecialSkillCatalog.MAX_RANK,
    val learnCost: Int = 1,
    val rankUpCost: Int = 1,
)

data class SkillSlotUi(
    val slotIndex: Int,
    val skillId: String?,
    val shortName: String,
    val enabled: Boolean,
    val mpCost: Int,
    val rank: Int = 0,
)

/** 레벨업·메뉴에서 여는 스킬맵 세션 */
data class SkillMapOffer(
    val actorKey: String,
    val actorName: String,
    val actorClass: ActorClass,
    val actorLevel: Int,
    /** 이번 레벨업으로 받은 SP (메뉴 오픈 시 0) */
    val pointsGranted: Int = 0,
    val fromLevelUp: Boolean = false,
)

/** @deprecated 호환용 별칭 */
typealias LevelUpSkillOffer = SkillMapOffer

/** 주인공 특별스킬 → 애니 시트 / 근접·탄환 FX 매핑 */
data class SpecialVfxSpec(
    val animSet: String? = null,
    val meleeFxKey: String? = null,
    val meleeFxScale: Float = 1.45f,
    val meleeFxDuration: Float = 0.55f,
    val projectileFxKey: String? = null,
    val impactFxKey: String? = null,
    val animDuration: Float = 0.55f,
)

object SpecialSkillCatalog {
    const val MAX_SLOTS = 3
    const val MAX_RANK = 5
    /** 랭크마다 피해 배수 증가 */
    const val RANK_MULT_STEP = 0.18f
    /** 랭크마다 MP 추가 소모 */
    const val RANK_MP_STEP = 1

    fun vfxFor(skillId: String): SpecialVfxSpec? = when (skillId) {
        "adv_smash" -> SpecialVfxSpec(
            animSet = "adv_smash",
            meleeFxKey = "adv_fx_smash",
            meleeFxScale = 1.55f,
        )
        "adv_flurry" -> SpecialVfxSpec(
            animSet = "adv_flurry",
            meleeFxKey = "adv_fx_flurry",
            meleeFxScale = 1.5f,
            meleeFxDuration = 0.62f,
            animDuration = 0.58f,
        )
        "adv_charge" -> SpecialVfxSpec(
            animSet = "adv_charge",
            meleeFxKey = "adv_fx_charge",
            meleeFxScale = 1.6f,
            animDuration = 0.52f,
        )
        "adv_shot" -> SpecialVfxSpec(
            animSet = "adv_shot",
            projectileFxKey = "adv_fx_arrow",
            animDuration = 0.5f,
        )
        "adv_bolt" -> SpecialVfxSpec(
            animSet = "adv_bolt",
            projectileFxKey = "adv_fx_firebolt",
            impactFxKey = "adv_fx_fireburst",
            animDuration = 0.52f,
        )
        "adv_finisher" -> SpecialVfxSpec(
            animSet = "adv_finisher",
            meleeFxKey = "adv_fx_finisher",
            meleeFxScale = 1.75f,
            meleeFxDuration = 0.65f,
            animDuration = 0.62f,
        )
        else -> null
    }

    fun damageMultAt(def: SpecialSkillDef, rank: Int): Float {
        val r = rank.coerceAtLeast(1)
        return def.damageMult + (r - 1) * RANK_MULT_STEP
    }

    fun mpCostAt(def: SpecialSkillDef, rank: Int): Int {
        val r = rank.coerceAtLeast(1)
        return def.mpCost + (r - 1) * RANK_MP_STEP
    }

    /** 스킬별 효과음 키 (GameRoot → Sfx 매핑) */
    fun sfxKeyFor(skillId: String): String = when (skillId) {
        "adv_smash", "war_rage" -> "skill_smash"
        "adv_flurry", "rog_vital", "pal_slash" -> "skill_slash"
        "adv_charge", "war_rush" -> "skill_charge"
        "adv_shot" -> "skill_bow"
        "adv_bolt", "mag_blast", "mag_meteor" -> "skill_fire"
        "adv_finisher" -> "skill_finisher"
        "war_quake" -> "skill_quake"
        "war_kill", "rog_assassinate" -> "skill_crit"
        "war_bash" -> "skill_bash"
        "war_spin" -> "skill_spin"
        "rog_stab", "rog_execute" -> "skill_execute"
        "rog_smoke" -> "skill_smoke"
        "rog_dual" -> "skill_slash"
        "mag_ice" -> "skill_ice"
        "mag_chain" -> "skill_lightning"
        "mag_orb" -> "skill_orb"
        "mag_ruin", "pal_holy", "pal_smite", "pal_judge", "pal_wrath", "pal_guard" -> "skill_holy"
        else -> when (byId(skillId)?.style) {
            WeaponStyle.BOW -> "skill_bow"
            WeaponStyle.MAGIC -> "skill_orb"
            else -> "skill_slash"
        }
    }

    val all: List<SpecialSkillDef> = buildList {
        // —— 모험가: 근접 가지 / 원거리 가지 → 필살 ——
        addAll(
            listOf(
                SpecialSkillDef(
                    "adv_smash", "강타", "강타", ActorClass.ADVENTURER, 2, 8, 3.0f, WeaponStyle.MELEE,
                    "힘을 모아 내려찍는다.", mapCol = 0, mapRow = 0,
                ),
                SpecialSkillDef(
                    "adv_flurry", "연속베기", "연베", ActorClass.ADVENTURER, 3, 10, 2.9f, WeaponStyle.MELEE,
                    "빠른 참격으로 몰아친다.", requires = listOf("adv_smash"), mapCol = 1, mapRow = 0,
                ),
                SpecialSkillDef(
                    "adv_charge", "돌진", "돌진", ActorClass.ADVENTURER, 4, 11, 3.0f, WeaponStyle.MELEE,
                    "적에게 쇄도하며 벤다.", requires = listOf("adv_flurry"), mapCol = 2, mapRow = 0,
                ),
                SpecialSkillDef(
                    "adv_shot", "집중사격", "집중", ActorClass.ADVENTURER, 4, 12, 3.0f, WeaponStyle.BOW,
                    "호흡을 가다듬고 한 발을 쏜다.", requires = listOf("adv_smash"), mapCol = 1, mapRow = 1,
                ),
                SpecialSkillDef(
                    "adv_bolt", "화염탄", "화탄", ActorClass.ADVENTURER, 5, 14, 3.1f, WeaponStyle.MAGIC,
                    "조잡하지만 뜨거운 마력탄.", requires = listOf("adv_shot"), mapCol = 2, mapRow = 1,
                ),
                SpecialSkillDef(
                    "adv_finisher", "필살일격", "필살", ActorClass.ADVENTURER, 7, 16, 3.4f, WeaponStyle.MELEE,
                    "모든 힘을 실은 최후의 일격.",
                    requires = listOf("adv_charge", "adv_bolt"), mapCol = 3, mapRow = 0,
                ),
            )
        )
        // —— 전사: 참격 / 돌진 → 분쇄 ——
        addAll(
            listOf(
                SpecialSkillDef(
                    "war_kill", "일격필살", "필살", ActorClass.WARRIOR, 2, 9, 3.2f, WeaponStyle.MELEE,
                    "약점을 노린 치명타.", mapCol = 0, mapRow = 0,
                ),
                SpecialSkillDef(
                    "war_bash", "방패강타", "강타", ActorClass.WARRIOR, 3, 10, 2.8f, WeaponStyle.MELEE,
                    "방패로 강하게 들이받는다.", requires = listOf("war_kill"), mapCol = 1, mapRow = 0,
                ),
                SpecialSkillDef(
                    "war_spin", "회전참", "회전", ActorClass.WARRIOR, 4, 12, 3.0f, WeaponStyle.MELEE,
                    "몸을 돌리며 넓게 벤다.", requires = listOf("war_bash"), mapCol = 2, mapRow = 0,
                ),
                SpecialSkillDef(
                    "war_rush", "돌진베기", "돌진", ActorClass.WARRIOR, 4, 13, 3.1f, WeaponStyle.MELEE,
                    "기세를 몰아 돌진 참격.", requires = listOf("war_kill"), mapCol = 1, mapRow = 1,
                ),
                SpecialSkillDef(
                    "war_rage", "분노의일격", "분노", ActorClass.WARRIOR, 5, 15, 3.3f, WeaponStyle.MELEE,
                    "분노를 담아 내려찍는다.", requires = listOf("war_rush"), mapCol = 2, mapRow = 1,
                ),
                SpecialSkillDef(
                    "war_quake", "대지분쇄", "분쇄", ActorClass.WARRIOR, 7, 17, 3.5f, WeaponStyle.MELEE,
                    "대지를 울리는 초강력 일격.",
                    requires = listOf("war_spin", "war_rage"), mapCol = 3, mapRow = 0,
                ),
            )
        )
        // —— 도적: 암살 / 교란 → 처형 ——
        addAll(
            listOf(
                SpecialSkillDef(
                    "rog_assassinate", "암살", "암살", ActorClass.ROGUE, 2, 9, 3.2f, WeaponStyle.MELEE,
                    "그림자에서 급소를 찌른다.", mapCol = 0, mapRow = 0,
                ),
                SpecialSkillDef(
                    "rog_stab", "그림자찌르기", "찌름", ActorClass.ROGUE, 3, 10, 2.9f, WeaponStyle.MELEE,
                    "보이지 않는 각도에서 찌른다.", requires = listOf("rog_assassinate"), mapCol = 1, mapRow = 0,
                ),
                SpecialSkillDef(
                    "rog_vital", "급소베기", "급소", ActorClass.ROGUE, 4, 13, 3.1f, WeaponStyle.MELEE,
                    "혈을 노려 깊게 벤다.", requires = listOf("rog_stab"), mapCol = 2, mapRow = 0,
                ),
                SpecialSkillDef(
                    "rog_smoke", "연막습격", "연막", ActorClass.ROGUE, 4, 11, 2.8f, WeaponStyle.MELEE,
                    "연막 속에서 파고든다.", requires = listOf("rog_assassinate"), mapCol = 1, mapRow = 1,
                ),
                SpecialSkillDef(
                    "rog_dual", "이중검무", "쌍검", ActorClass.ROGUE, 5, 15, 3.2f, WeaponStyle.MELEE,
                    "쌍단검으로 연속 타격.", requires = listOf("rog_smoke"), mapCol = 2, mapRow = 1,
                ),
                SpecialSkillDef(
                    "rog_execute", "처형", "처형", ActorClass.ROGUE, 7, 17, 3.5f, WeaponStyle.MELEE,
                    "숨통을 끊는 최후 처형.",
                    requires = listOf("rog_vital", "rog_dual"), mapCol = 3, mapRow = 0,
                ),
            )
        )
        // —— 마법사: 화염 / 빙뢰 → 멸마 ——
        addAll(
            listOf(
                SpecialSkillDef(
                    "mag_blast", "화염폭발", "화폭", ActorClass.MAGE, 2, 10, 3.0f, WeaponStyle.MAGIC,
                    "화염을 폭발시킨다.", mapCol = 0, mapRow = 0,
                ),
                SpecialSkillDef(
                    "mag_orb", "마력탄", "마탄", ActorClass.MAGE, 3, 12, 3.0f, WeaponStyle.MAGIC,
                    "응축된 마력탄을 발사한다.", requires = listOf("mag_blast"), mapCol = 1, mapRow = 0,
                ),
                SpecialSkillDef(
                    "mag_meteor", "운석", "운석", ActorClass.MAGE, 5, 16, 3.3f, WeaponStyle.MAGIC,
                    "작은 운석을 떨어뜨린다.", requires = listOf("mag_orb"), mapCol = 2, mapRow = 0,
                ),
                SpecialSkillDef(
                    "mag_ice", "빙결창", "빙결", ActorClass.MAGE, 3, 11, 2.9f, WeaponStyle.MAGIC,
                    "얼음 창을 쏘아보낸다.", requires = listOf("mag_blast"), mapCol = 1, mapRow = 1,
                ),
                SpecialSkillDef(
                    "mag_chain", "번개연쇄", "번개", ActorClass.MAGE, 4, 13, 3.1f, WeaponStyle.MAGIC,
                    "번개가 적을 관통한다.", requires = listOf("mag_ice"), mapCol = 2, mapRow = 1,
                ),
                SpecialSkillDef(
                    "mag_ruin", "멸마의빛", "멸마", ActorClass.MAGE, 7, 18, 3.5f, WeaponStyle.MAGIC,
                    "마력을 쏟아붓는 최종 주문.",
                    requires = listOf("mag_meteor", "mag_chain"), mapCol = 3, mapRow = 0,
                ),
            )
        )
        // —— 성기사: 성격 / 수호 → 성역 ——
        addAll(
            listOf(
                SpecialSkillDef(
                    "pal_smite", "성스러운일격", "성격", ActorClass.PALADIN, 2, 9, 3.0f, WeaponStyle.MELEE,
                    "성스러운 힘으로 타격한다.", mapCol = 0, mapRow = 0,
                ),
                SpecialSkillDef(
                    "pal_judge", "심판", "심판", ActorClass.PALADIN, 3, 11, 3.1f, WeaponStyle.MELEE,
                    "악을 단죄하는 일격.", requires = listOf("pal_smite"), mapCol = 1, mapRow = 0,
                ),
                SpecialSkillDef(
                    "pal_wrath", "천벌", "천벌", ActorClass.PALADIN, 5, 15, 3.3f, WeaponStyle.MELEE,
                    "하늘이 내리는 벌.", requires = listOf("pal_judge"), mapCol = 2, mapRow = 0,
                ),
                SpecialSkillDef(
                    "pal_slash", "성광참", "성광", ActorClass.PALADIN, 3, 12, 2.9f, WeaponStyle.MELEE,
                    "빛줄기를 담아 벤다.", requires = listOf("pal_smite"), mapCol = 1, mapRow = 1,
                ),
                SpecialSkillDef(
                    "pal_guard", "수호의검", "수호", ActorClass.PALADIN, 4, 13, 2.8f, WeaponStyle.MELEE,
                    "수호의 기운을 실은 참격.", requires = listOf("pal_slash"), mapCol = 2, mapRow = 1,
                ),
                SpecialSkillDef(
                    "pal_holy", "성역강림", "성역", ActorClass.PALADIN, 7, 17, 3.5f, WeaponStyle.MELEE,
                    "성역을 불러 적을 분쇄한다.",
                    requires = listOf("pal_wrath", "pal_guard"), mapCol = 3, mapRow = 0,
                ),
            )
        )
    }

    fun byId(id: String): SpecialSkillDef? = all.firstOrNull { it.id == id }

    fun forClass(cls: ActorClass): List<SpecialSkillDef> =
        all.filter { it.actorClass == cls }.sortedWith(compareBy({ it.mapCol }, { it.mapRow }))

    fun unlocksAt(cls: ActorClass, level: Int): List<SpecialSkillDef> =
        forClass(cls).filter { it.unlockLevel == level }

    fun unlockedUpTo(cls: ActorClass, level: Int): List<SpecialSkillDef> =
        forClass(cls).filter { it.unlockLevel <= level }

    fun mapWidth(cls: ActorClass): Int =
        forClass(cls).maxOfOrNull { it.mapCol }?.plus(1) ?: 1

    fun mapHeight(cls: ActorClass): Int =
        forClass(cls).maxOfOrNull { it.mapRow }?.plus(1) ?: 1

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
