package com.medieval.village.model

/** 바닷가 폐허의 바다 동굴 — 20층의 대왕문어를 쓰러뜨리면 마을이 회복된다. */
object SeaCaveFactory {

    const val MAX_FLOOR = 20

    private val mobs = listOf(
        "crab" to "바위게",
        "jellyfish" to "해파리",
        "shark" to "상어",
        "drowned" to "익사자",
        "pirate_ghost" to "해적 유령",
        "sea_snake" to "바다뱀",
    )

    fun isFinalFloor(floor: Int): Boolean = floor >= MAX_FLOOR

    fun generate(floor: Int, seed: Int = floor * 7723 + 88): DungeonFloor =
        StoryDungeonFactory.generate(
            floor = floor,
            seed = seed,
            idPrefix = "sea",
            maxFloor = MAX_FLOOR,
            mobs = mobs,
            kindBonus = { kind ->
                when (kind) {
                    "shark", "giant_octopus" -> 10
                    "drowned", "pirate_ghost" -> 6
                    "jellyfish", "sea_snake" -> 4
                    else -> 2
                }
            },
            midBoss = "shark" to "상어 두목",
            finalBoss = "giant_octopus" to "대왕문어",
            sewerChance = 0.16f,
            basePower = 15,
            powerPerFloor = 10,
        )
}
