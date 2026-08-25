package com.medieval.village.model

/** 남쪽 사막 지대 — 전갈·사막여우·낙타거미 등 */
object DesertFactory {

    const val TILE = WildZoneGenerator.TILE

    private val shallow = listOf(
        "scorpion" to "전갈",
        "desert_fox" to "사막여우",
        "camel_spider" to "낙타거미",
        "sand_snake" to "모래뱀",
        "vulture" to "독수리",
        "sidewinder" to "뿔살무사",
        "dung_beetle" to "사막풍뎅이",
    )

    private val deep = listOf(
        "giant_scorpion" to "거대전갈",
        "deathstalker" to "데스스토커",
        "dune_worm" to "모래벌레",
        "sand_golem" to "모래골렘",
        "desert_drake" to "사막 드레이크",
    )

    fun generate(floor: Int, seed: Int = floor * 6113 + 17): DungeonFloor =
        WildZoneGenerator.generate(
            floor = floor,
            seed = seed,
            idPrefix = "d",
            shallow = shallow,
            deep = deep,
            kindBonus = { kind ->
                when (kind) {
                    "giant_scorpion", "desert_drake", "sand_golem" -> 9
                    "deathstalker", "dune_worm" -> 6
                    "scorpion", "camel_spider", "sidewinder" -> 4
                    "desert_fox", "vulture" -> 2
                    else -> 0
                }
            },
            basePower = 9,
            powerPerFloor = 7,
            sewerChance = 0.16f,
            midBoss = "giant_scorpion" to "모래폭풍의 전갈왕",
        )
}
