package com.medieval.village.audio

import com.medieval.village.model.PlaceId
import com.medieval.village.model.SettlementId
import com.medieval.village.model.WorldFlags

enum class MusicMood {
    OAKHAVEN,
    ASHBROOK,
    GRAY_CURSED,
    GRAY_LIBERATED,
    IGLOO_CURSED,
    IGLOO_LIBERATED,
    SEASIDE_CURSED,
    SEASIDE_LIBERATED,
    WINTER_CURSED,
    WINTER_LIBERATED,
    COZY,
    TENSE,
}

private val EXPLORE_OR_ARENA = setOf(
    PlaceId.DUNGEON,
    PlaceId.EAST_FOREST,
    PlaceId.SOUTH_DESERT,
    PlaceId.NORTH_GLACIER,
    PlaceId.GRAY_CASTLE,
    PlaceId.IGLOO_GLACIER,
    PlaceId.SEA_CAVE,
    PlaceId.WINTER_KEEP,
    PlaceId.ARENA,
)

/** 마을·성 BGM: 정착지마다 다르고, 저주/해방 상태가 갈린다. 탐험·대련은 긴장 테마. */
fun resolveMusicMood(
    inVillage: Boolean,
    place: PlaceId?,
    settlement: SettlementId,
    flags: WorldFlags,
): MusicMood {
    if (!inVillage && place in EXPLORE_OR_ARENA) return MusicMood.TENSE
    return settlementTheme(settlement, flags)
}

fun settlementTheme(settlement: SettlementId, flags: WorldFlags): MusicMood = when (settlement) {
    SettlementId.OAKHAVEN -> MusicMood.OAKHAVEN
    SettlementId.ASHBROOK -> MusicMood.ASHBROOK
    SettlementId.GRAY_CASTLE ->
        if (flags.castleCleared) MusicMood.GRAY_LIBERATED else MusicMood.GRAY_CURSED
    SettlementId.IGLOO ->
        if (flags.iglooCleared) MusicMood.IGLOO_LIBERATED else MusicMood.IGLOO_CURSED
    SettlementId.SEASIDE ->
        if (flags.seasideCleared) MusicMood.SEASIDE_LIBERATED else MusicMood.SEASIDE_CURSED
    SettlementId.WINTER_CASTLE ->
        if (flags.winterCleared) MusicMood.WINTER_LIBERATED else MusicMood.WINTER_CURSED
}
