package com.medieval.village.model

/** 이글루 마을 유일한 던전 — 빙하지대. 20층의 얼음북극곰을 쓰러뜨리면 온기가 돌아온다. */
object IglooFactory {

    const val MAX_FLOOR = 20

    private val shallow = listOf(
        "penguin" to "펭귄",
        "ice_fox" to "눈여우",
        "seal" to "바다표범",
        "snow_hare" to "눈토끼",
        "ice_wolf" to "빙하늑대",
        "frost_owl" to "서리부엉이",
        "ice_penguin" to "얼음 펭귄",
        "icicle_penguin" to "고드름 펭귄",
    )

    private val deep = listOf(
        "yeti" to "설인",
        "ice_elemental" to "얼음정령",
        "frost_penguin" to "서리 거대펭귄",
        "ice_spider" to "빙하독거미",
        "frost_shaman" to "서리 주술사",
        "ice_wolf" to "빙하늑대",
    )

    fun isFinalFloor(floor: Int): Boolean = floor >= MAX_FLOOR

    fun generate(floor: Int, seed: Int = floor * 9101 + 41): DungeonFloor =
        WildZoneGenerator.generate(
            floor = floor.coerceIn(1, MAX_FLOOR),
            seed = seed,
            idPrefix = "ig",
            shallow = shallow,
            deep = deep,
            kindBonus = { kind ->
                when (kind) {
                    "yeti", "ice_elemental", "ice_star_bear" -> 10
                    "frost_penguin", "ice_spider", "frost_shaman" -> 6
                    "ice_wolf", "ice_penguin", "icicle_penguin" -> 4
                    else -> 1
                }
            },
            basePower = 11,
            powerPerFloor = 8,
            sewerChance = 0.22f,
            midBoss = "yeti" to "설인 우두머리",
            maxFloor = MAX_FLOOR,
            finalBoss = "ice_star_bear" to "얼음북극곰",
        )
}
