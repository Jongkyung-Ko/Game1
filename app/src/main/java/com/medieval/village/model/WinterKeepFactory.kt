package com.medieval.village.model

/** 겨울성 지하 던전 — 20층의 납치범 두목을 쓰러뜨리면 성이 봄을 되찾는다. */
object WinterKeepFactory {

    const val MAX_FLOOR = 20

    private val mobs = listOf(
        "kidnapper" to "납치범",
        "ice_guard" to "얼음 경비",
        "winter_wolf" to "겨울늑대",
        "frost_thug" to "서리 폭도",
        "cage_warden" to "감옥 간수",
    )

    fun isFinalFloor(floor: Int): Boolean = floor >= MAX_FLOOR

    fun generate(floor: Int, seed: Int = floor * 6619 + 55): DungeonFloor =
        StoryDungeonFactory.generate(
            floor = floor,
            seed = seed,
            idPrefix = "wk",
            maxFloor = MAX_FLOOR,
            mobs = mobs,
            kindBonus = { kind ->
                when (kind) {
                    "kidnapper_boss", "cage_warden" -> 10
                    "ice_guard", "frost_thug" -> 6
                    "kidnapper", "winter_wolf" -> 4
                    else -> 2
                }
            },
            midBoss = "cage_warden" to "감옥 간수장",
            finalBoss = "kidnapper_boss" to "납치범 두목",
            sewerChance = 0.08f,
            basePower = 16,
            powerPerFloor = 10,
        )
}
