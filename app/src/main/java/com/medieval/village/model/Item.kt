package com.medieval.village.model

enum class ItemType(val label: String) {
    WEAPON("무기"),
    SHIELD("방패"),
    ARMOR("갑옷"),
    HELMET("투구"),
    ACCESSORY("장신구"),
    CONSUMABLE("소모품")
}

/** 무기 공격 방식 — 던전 패드 전투에서 사용 */
enum class WeaponStyle {
    /** 검·도끼 등 전방 초승달 휘두르기 */
    MELEE,
    /** 화살 발사 */
    BOW,
    /** 마법 탄환 발사 (MP 소모) */
    MAGIC,
}

/** 장비 슬롯 순서 (Equipment 화면 표시 순서) */
val EQUIP_SLOTS = listOf(
    ItemType.WEAPON,
    ItemType.SHIELD,
    ItemType.ARMOR,
    ItemType.HELMET,
    ItemType.ACCESSORY
)

data class Item(
    val id: String,
    val name: String,
    val type: ItemType,
    val price: Int,
    val atk: Int = 0,
    val def: Int = 0,
    val healHp: Int = 0,
    val healMp: Int = 0,
    val desc: String = "",
    val weaponStyle: WeaponStyle = WeaponStyle.MELEE,
    /** 적중 시 공격자가 회복하는 HP */
    val lifestealHp: Int = 0,
    /** 적중 시 MP 회복 확률 (0–100) */
    val onHitMpChance: Int = 0,
    /** 적중 시 회복하는 MP (확률 성공 시) */
    val onHitMp: Int = 0,
    /** 적중 시 방어 계산 전에 더하는 추가 피해 */
    val onHitBonusDamage: Int = 0,
) {
    val isEquipment: Boolean get() = type != ItemType.CONSUMABLE
    /** 되팔 때 가격 */
    val sellPrice: Int get() = (price * 0.4f).toInt().coerceAtLeast(1)
}

/** 가방 한 칸. 강화 수치는 장비할 때만 유지되므로 가방에서는 개수만 관리한다. */
data class InventoryEntry(val item: Item, val count: Int)

/** 착용 중인 장비. plus 는 대장간 강화 단계. */
data class EquippedItem(val item: Item, val plus: Int = 0) {
    val atk: Int get() = item.atk + plus * 2
    val def: Int get() = item.def + plus * 2
    val displayName: String get() = if (plus > 0) "${item.name} +$plus" else item.name
    /** 다음 강화 비용 */
    val upgradeCost: Int get() = 80 + plus * 70
}

object ItemCatalog {

    // 소모품 - 상점
    val potion = Item("potion", "체력 물약", ItemType.CONSUMABLE, 30, healHp = 40, desc = "HP를 40 회복한다.")
    val hiPotion = Item("hi_potion", "고급 체력 물약", ItemType.CONSUMABLE, 90, healHp = 120, desc = "HP를 120 회복한다.")
    val ether = Item("ether", "마나 물약", ItemType.CONSUMABLE, 45, healMp = 25, desc = "MP를 25 회복한다.")
    val bread = Item("bread", "호밀빵", ItemType.CONSUMABLE, 10, healHp = 12, desc = "소박하지만 든든하다. HP 12 회복.")
    val torch = Item("torch", "횃불", ItemType.CONSUMABLE, 20, desc = "오염된 하수도를 비춘다. 좀비 둥지 탐험의 필수품.")
    val portalStone = Item(
        "portal_stone",
        "포털스톤",
        ItemType.CONSUMABLE,
        180,
        desc = "던전에서만 쓸 수 있다. 집으로 이어지는 포털을 연다. 던전을 떠났다가 돌아오면 포털은 사라진다.",
    )

    // 무기 - 무기점 (근접)
    val rustySword = Item(
        "rusty_sword", "낡은 검", ItemType.WEAPON, 80, atk = 5,
        desc = "아버지가 쓰던 검. 전방에 초승달 참격을 가한다.",
        weaponStyle = WeaponStyle.MELEE,
    )
    val ironSword = Item(
        "iron_sword", "강철 장검", ItemType.WEAPON, 280, atk = 13,
        desc = "좀비 뼈를 가르라고 다듬은 대장장이의 역작.",
        weaponStyle = WeaponStyle.MELEE,
    )
    val knightSword = Item(
        "knight_sword", "기사의 검", ItemType.WEAPON, 760, atk = 24,
        desc = "저주가 퍼지기 전, 영주 경호대가 쓰던 제식 검.",
        weaponStyle = WeaponStyle.MELEE,
    )
    val battleAxe = Item(
        "battle_axe", "전투 도끼", ItemType.WEAPON, 520, atk = 20, def = -2,
        desc = "무겁지만 부패한 육체를 부수는 데 좋다.",
        weaponStyle = WeaponStyle.MELEE,
    )
    // 원거리
    val shortBow = Item(
        "short_bow", "짧은 활", ItemType.WEAPON, 220, atk = 11,
        desc = "화살을 발사해 먼 적을 맞춘다.",
        weaponStyle = WeaponStyle.BOW,
    )
    val hunterBow = Item(
        "hunter_bow", "사냥꾼의 활", ItemType.WEAPON, 580, atk = 18,
        desc = "숲길을 누비던 사냥꾼의 활. 화살이 빠르다.",
        weaponStyle = WeaponStyle.BOW,
    )
    // 마법
    val oakStaff = Item(
        "oak_staff", "참나무 지팡이", ItemType.WEAPON, 240, atk = 10,
        desc = "기본 마력탄은 MP 없이 쏜다. 마법·특별스킬만 마나를 쓴다.",
        weaponStyle = WeaponStyle.MAGIC,
    )
    val flameWand = Item(
        "flame_wand", "화염 지팡이", ItemType.WEAPON, 640, atk = 19,
        desc = "기본 공격은 MP 없이 마력탄을 쏜다. 화염 계열 주문은 마나를 쓴다.",
        weaponStyle = WeaponStyle.MAGIC,
    )

    // 방어구 - 무기점
    val woodShield = Item("wood_shield", "나무 방패", ItemType.SHIELD, 70, def = 4, desc = "가볍고 값싸다.")
    val ironShield = Item("iron_shield", "강철 방패", ItemType.SHIELD, 260, def = 11, desc = "묵직한 방어력.")
    val leatherArmor = Item("leather_armor", "가죽 갑옷", ItemType.ARMOR, 130, def = 6, desc = "여행자의 기본 장비.")
    val chainMail = Item("chain_mail", "사슬 갑옷", ItemType.ARMOR, 420, def = 15, desc = "촘촘한 사슬로 엮었다.")
    val ironHelm = Item("iron_helm", "강철 투구", ItemType.HELMET, 210, def = 8, desc = "머리를 든든히 지켜준다.")

    // 장신구 - 상점 / 마법학교
    val luckyRing = Item("lucky_ring", "행운의 반지", ItemType.ACCESSORY, 320, def = 2, desc = "행운이 조금 따른다.")
    val manaAmulet = Item("mana_amulet", "마나 부적", ItemType.ACCESSORY, 400, def = 1, desc = "마력이 흐르는 부적.")

    val generalGoods = listOf(potion, hiPotion, ether, bread, torch, portalStone, luckyRing)
    val weaponGoods = listOf(
        rustySword, ironSword, battleAxe, knightSword,
        shortBow, hunterBow, oakStaff, flameWand,
        woodShield, ironShield, leatherArmor, chainMail, ironHelm,
    )

    /** 던전에서 드랍될 수 있는 전리품 */
    val dungeonLoot = listOf(potion, ether, bread, portalStone, rustySword, shortBow, woodShield, manaAmulet)

    /** 동쪽 숲 동물·은닉 상자 전리품 */
    val forestLoot = listOf(potion, bread, ether, torch, woodShield, leatherArmor, luckyRing, rustySword, shortBow)

    /** 남쪽 사막 전리품 */
    val desertLoot = listOf(potion, hiPotion, ether, torch, woodShield, ironShield, leatherArmor, rustySword, oakStaff)

    /** 북쪽 빙하 전리품 */
    val glacierLoot = listOf(potion, hiPotion, ether, portalStone, ironSword, hunterBow, chainMail, manaAmulet, luckyRing)

    // 중간 보스 전용 특수 무기 — 상점 판매 없음
    val vampireBlade = Item(
        "vampire_blade", "흡혈검", ItemType.WEAPON, 980, atk = 22,
        desc = "지하 감시자의 검. 때릴 때마다 HP를 8 흡수한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 8,
    )
    val plagueGreatsword = Item(
        "plague_greatsword", "역병의 대검", ItemType.WEAPON, 1120, atk = 26,
        desc = "역병 흉물의 살점 검. 적중 시 추가 피해를 주고 HP를 5 흡수한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 5,
        onHitBonusDamage = 6,
    )
    val magicSword = Item(
        "magic_sword", "매직소드", ItemType.WEAPON, 1080, atk = 20,
        desc = "리치의 마력 검. 때릴 때마다 35% 확률로 MP를 6 회복한다.",
        weaponStyle = WeaponStyle.MELEE,
        onHitMpChance = 35,
        onHitMp = 6,
    )
    val guardianFang = Item(
        "guardian_fang", "수호자의 송곳니", ItemType.WEAPON, 940, atk = 21,
        desc = "숲의 수호자가 남긴 이빨. 때릴 때마다 HP를 10 흡수한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 10,
    )
    val scorpionFangBlade = Item(
        "scorpion_fang_blade", "전갈왕의 독월도", ItemType.WEAPON, 1000, atk = 23,
        desc = "모래폭풍의 독침을 벼린 도. 적중 시 추가 피해를 주고 HP를 4 흡수한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 4,
        onHitBonusDamage = 8,
    )
    val frostMagicSword = Item(
        "frost_magic_sword", "서리 마력검", ItemType.WEAPON, 1060, atk = 19,
        desc = "빙하의 군주가 품던 검. 때릴 때마다 40% 확률로 MP를 8 회복한다.",
        weaponStyle = WeaponStyle.MELEE,
        onHitMpChance = 40,
        onHitMp = 8,
    )
    val soulKingBlade = Item(
        "soul_king_blade", "해골왕의 영혼검", ItemType.WEAPON, 1280, atk = 28,
        desc = "해방된 왕관의 검. 때릴 때마다 HP를 6 흡수하고 25% 확률로 MP를 5 회복한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 6,
        onHitMpChance = 25,
        onHitMp = 5,
    )
    val iceStarSpear = Item(
        "ice_star_spear", "얼음 별의 창", ItemType.WEAPON, 1320, atk = 27,
        desc = "얼음북극곰이 지키던 별의 창. 때릴 때마다 45% 확률로 MP를 8 회복하고 HP를 4 흡수한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 4,
        onHitMpChance = 45,
        onHitMp = 8,
    )
    val tentacleSaber = Item(
        "tentacle_saber", "촉수의 사브르", ItemType.WEAPON, 1300, atk = 26,
        desc = "대왕문어의 촉수를 벼린 칼. 적중 시 추가 피해를 주고 HP를 7 흡수한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 7,
        onHitBonusDamage = 9,
    )
    val kidnapperDirk = Item(
        "kidnapper_dirk", "납치범의 단검", ItemType.WEAPON, 1260, atk = 25,
        desc = "두목이 쥐던 단검. 때릴 때마다 HP를 5 흡수하고 30% 확률로 MP를 6 회복한다.",
        weaponStyle = WeaponStyle.MELEE,
        lifestealHp = 5,
        onHitMpChance = 30,
        onHitMp = 6,
    )

    val bossRelics = listOf(
        vampireBlade, plagueGreatsword, magicSword,
        guardianFang, scorpionFangBlade, frostMagicSword, soulKingBlade,
        iceStarSpear, tentacleSaber, kidnapperDirk,
    )

    val all: List<Item> = (
        generalGoods + weaponGoods + dungeonLoot + forestLoot + desertLoot + glacierLoot + bossRelics
        ).distinctBy { it.id }

    fun byId(id: String): Item? = all.firstOrNull { it.id == id }

    /** 중간 보스 kind → 전용 특수 무기 */
    fun relicForBossKind(kind: String): Item? = when (kind) {
        "boss_warden" -> vampireBlade
        "boss_abomination" -> plagueGreatsword
        "boss_lich" -> magicSword
        "bear" -> guardianFang
        "giant_scorpion" -> scorpionFangBlade
        "polar_bear" -> frostMagicSword
        "boss_skel_king" -> soulKingBlade
        "yeti" -> frostMagicSword
        "ice_star_bear" -> iceStarSpear
        "shark" -> tentacleSaber
        "giant_octopus" -> tentacleSaber
        "cage_warden" -> kidnapperDirk
        "kidnapper_boss" -> kidnapperDirk
        else -> null
    }
}
