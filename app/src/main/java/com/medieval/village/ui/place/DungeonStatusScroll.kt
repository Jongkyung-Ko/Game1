package com.medieval.village.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.ClassicScroll
import com.medieval.village.ui.theme.romanNumeral

/**
 * 교대 버튼 우측에 놓이는 작은 고전 영문 상태 두루마리.
 */
@Composable
fun DungeonStatusScroll(vm: GameViewModel, modifier: Modifier = Modifier) {
    @Suppress("UNUSED_EXPRESSION")
    vm.dungeonCombatFrame

    val floor = vm.dungeonFloorNumber
    val place = vm.currentPlace
    val siteName = when (place) {
        PlaceId.GRAY_CASTLE -> "Gray Castle"
        PlaceId.EAST_FOREST -> "Eastern Wood"
        PlaceId.SOUTH_DESERT -> "Southern Waste"
        PlaceId.NORTH_GLACIER -> "Northern Glacier"
        PlaceId.IGLOO_GLACIER -> "Igloo Glacier"
        PlaceId.SEA_CAVE -> "Sea Cave"
        PlaceId.WINTER_KEEP -> "Winter Keep"
        else -> "Forgotten Crypt"
    }
    val depthWord = if (place.isWildSite()) "Reach" else "Floor"
    val foes = vm.dungeonFloor?.monsters?.count { it.alive } ?: 0

    val lines = buildList {
        add(
            when {
                vm.dungeonFloor == null -> "The map is yet unrolled."
                foes == 0 -> "The way lies still. No foe stirs."
                foes == 1 -> "One foe yet prowls these halls."
                else -> "$foes foes yet prowl these halls."
            }
        )
        when (vm.dungeonHint) {
            "stairs_up" -> add("Stairs ascend beneath thy feet. Escape at will.")
            "stairs_down" -> add("A descent opens below. Venture deeper?")
            "portal" -> add("A shimmering gate would bear thee home.")
            "chest" -> add("A sealed coffer rests here. Open it.")
            "chest_open" -> add("The coffer lies emptied and open.")
            else -> add("Damp stone and guttering torchlight surround thee.")
        }
        val hpRatio = vm.player.hpRatio
        add(
            when {
                hpRatio <= 0.25f -> "Thy wounds run deep — drink a potion."
                hpRatio <= 0.55f -> "Blood is spent. Guard thyself well."
                vm.spellShieldTime > 0f -> "A warding veil hums about thee."
                else -> "Thou art hale and steady of hand."
            }
        )
        if (vm.debugMode) {
            val zone = com.medieval.village.model.CombatBalance.zoneOf(place)
            if (zone != null) {
                val target = com.medieval.village.model.CombatBalance.targetLevel(zone, floor)
                add("Debug · 권장 Lv.${"%.1f".format(target)}  ${zone.stars}")
                val trash = com.medieval.village.model.CombatBalance.roll(
                    zone, floor, rng = kotlin.random.Random(0),
                )
                add("잡몹 P${trash.power} HP${trash.hp} AR${trash.armor}  너 ATK${vm.totalAtk}/DEF${vm.totalDef}")
            }
        }
    }

    ClassicScroll(
        heading = "$siteName · $depthWord ${romanNumeral(floor)}",
        lines = lines,
        modifier = modifier,
    )
}

private fun PlaceId?.isWildSite(): Boolean =
    this == PlaceId.EAST_FOREST || this == PlaceId.SOUTH_DESERT ||
        this == PlaceId.NORTH_GLACIER || this == PlaceId.IGLOO_GLACIER
