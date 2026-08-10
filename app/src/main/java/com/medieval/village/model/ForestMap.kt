package com.medieval.village.model

/**
 * 동쪽 숲 지대 생성기.
 * 던전과 같은 [DungeonFloor] 구조를 쓰되, 타일은 숲길·덤불·수풀로 해석한다.
 */
object ForestFactory {

    const val TILE = WildZoneGenerator.TILE
    const val COLS = WildZoneGenerator.COLS
    const val ROWS = WildZoneGenerator.ROWS

    private val shallowAnimals = listOf(
        "rabbit" to "숲토끼",
        "fox" to "여우",
        "deer" to "사슴",
        "wolf" to "늑대",
        "boar" to "멧돼지",
        "snake" to "독사",
        "owl" to "부엉이",
    )

    private val deepAnimals = listOf(
        "dire_wolf" to "흉포한 늑대",
        "bear" to "불곰",
        "giant_boar" to "거대 멧돼지",
        "forest_spider" to "거미줄 독거미",
        "stag" to "뿔사슴",
    )

    fun generate(floor: Int, seed: Int = floor * 5303 + 91): DungeonFloor =
        WildZoneGenerator.generate(
            floor = floor,
            seed = seed,
            idPrefix = "f",
            shallow = shallowAnimals,
            deep = deepAnimals,
            kindBonus = { kind ->
                when (kind) {
                    "bear", "giant_boar", "dire_wolf" -> 8
                    "boar", "forest_spider", "stag" -> 5
                    "wolf", "snake" -> 3
                    "fox", "owl" -> 1
                    else -> 0
                }
            },
        )
}
