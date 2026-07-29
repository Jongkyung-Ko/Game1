package com.medieval.village.model

enum class ItemType(val label: String) {
    WEAPON("무기"),
    SHIELD("방패"),
    ARMOR("갑옷"),
    HELMET("투구"),
    ACCESSORY("장신구"),
    CONSUMABLE("소모품")
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
    val desc: String = ""
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
    val torch = Item("torch", "횃불", ItemType.CONSUMABLE, 20, desc = "던전 탐험 시 함정을 피하기 쉬워진다.")

    // 무기 - 무기점
    val rustySword = Item("rusty_sword", "낡은 검", ItemType.WEAPON, 80, atk = 5, desc = "아버지가 쓰던 검.")
    val ironSword = Item("iron_sword", "강철 장검", ItemType.WEAPON, 280, atk = 13, desc = "마을 대장장이의 역작.")
    val knightSword = Item("knight_sword", "기사의 검", ItemType.WEAPON, 760, atk = 24, desc = "왕국 기사단 제식 검.")
    val battleAxe = Item("battle_axe", "전투 도끼", ItemType.WEAPON, 520, atk = 20, def = -2, desc = "무겁지만 파괴력이 크다.")

    // 방어구 - 무기점
    val woodShield = Item("wood_shield", "나무 방패", ItemType.SHIELD, 70, def = 4, desc = "가볍고 값싸다.")
    val ironShield = Item("iron_shield", "강철 방패", ItemType.SHIELD, 260, def = 11, desc = "묵직한 방어력.")
    val leatherArmor = Item("leather_armor", "가죽 갑옷", ItemType.ARMOR, 130, def = 6, desc = "여행자의 기본 장비.")
    val chainMail = Item("chain_mail", "사슬 갑옷", ItemType.ARMOR, 420, def = 15, desc = "촘촘한 사슬로 엮었다.")
    val ironHelm = Item("iron_helm", "강철 투구", ItemType.HELMET, 210, def = 8, desc = "머리를 든든히 지켜준다.")

    // 장신구 - 상점 / 마법학교
    val luckyRing = Item("lucky_ring", "행운의 반지", ItemType.ACCESSORY, 320, def = 2, desc = "행운이 조금 따른다.")
    val manaAmulet = Item("mana_amulet", "마나 부적", ItemType.ACCESSORY, 400, def = 1, desc = "마력이 흐르는 부적.")

    val generalGoods = listOf(potion, hiPotion, ether, bread, torch, luckyRing)
    val weaponGoods = listOf(rustySword, ironSword, battleAxe, knightSword, woodShield, ironShield, leatherArmor, chainMail, ironHelm)

    /** 던전에서 드랍될 수 있는 전리품 */
    val dungeonLoot = listOf(potion, ether, bread, rustySword, woodShield, manaAmulet)

    val all: List<Item> = (generalGoods + weaponGoods + dungeonLoot + manaAmulet).distinctBy { it.id }
}
