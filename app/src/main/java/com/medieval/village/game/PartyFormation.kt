package com.medieval.village.game

import com.medieval.village.model.Mercenary

/** 마을·던전에서 일렬 종대 배치 */
object PartyFormation {
    const val SPACING = 38f

    /**
     * @param slotBehind 0 = 선두(오프셋 없음), 1 = 바로 뒤…
     */
    fun behindOffset(facing: Facing, slotBehind: Int): Pair<Float, Float> {
        val s = slotBehind * SPACING
        return -facing.dirX() * s to -facing.dirY() * s
    }

    /**
     * 선두가 index 0이 되도록 정렬한 전투 열.
     * null 은 주인공.
     */
    fun battleLine(frontIndex: Int, party: List<Mercenary>): List<Mercenary?> {
        val actors: List<Mercenary?> = listOf(null) + party
        if (actors.isEmpty()) return emptyList()
        val idx = frontIndex.coerceIn(0, actors.lastIndex)
        return actors.drop(idx) + actors.take(idx)
    }
}
