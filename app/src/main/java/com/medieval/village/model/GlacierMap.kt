package com.medieval.village.model

/** 북쪽 빙하 지대 — 북극곰·펭귄·얼음 짐승 등 */
object GlacierFactory {

    const val TILE = WildZoneGenerator.TILE

    private val shallow = listOf(
        "penguin" to "펭귄",
        "ice_fox" to "눈여우",
        "seal" to "바다표범",
        "snow_hare" to "눈토끼",
        "ice_wolf" to "빙하늑대",
        "frost_owl" to "서리부엉이",
        "ice_penguin" to "얼음 펭귄",
    )

    private val deep = listOf(
        "polar_bear" to "북극곰",
        "yeti" to "설인",
        "ice_elemental" to "얼음정령",
        "frost_penguin" to "서리 거대펭귄",
        "ice_spider" to "빙하독거미",
    )

    fun generate(floor: Int, seed: Int = floor * 4729 + 33): DungeonFloor =
        WildZoneGenerator.generate(
            floor = floor,
            seed = seed,
            idPrefix = "g",
            shallow = shallow,
            deep = deep,
            kindBonus = { kind ->
                when (kind) {
                    "polar_bear", "yeti", "ice_elemental" -> 9
                    "frost_penguin", "ice_spider" -> 6
                    "ice_wolf", "ice_penguin" -> 4
                    "penguin", "seal", "ice_fox" -> 1
                    else -> 0
                }
            },
            basePower = 9,
            powerPerFloor = 8,
            sewerChance = 0.20f,
            midBoss = "polar_bear" to "빙하의 군주",
        )
}
