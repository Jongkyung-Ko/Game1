package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.game.MenuTab
import com.medieval.village.game.Scene
import com.medieval.village.game.isExplorePlace
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.theme.ClassicType
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.theme.romanNumeral

private val tabs = listOf(
    MenuTab.STATUS to "Status",
    MenuTab.INVENTORY to "Bag",
    MenuTab.EQUIPMENT to "Equip",
    MenuTab.WORLD_MAP to "지도",
    MenuTab.SYSTEM to "System"
)

@Composable
fun TopMenuBar(vm: GameViewModel, modifier: Modifier = Modifier) {
    // 몬스터 처치 시 잔여 수 갱신
    @Suppress("UNUSED_EXPRESSION")
    vm.dungeonCombatFrame

    val exploring = vm.scene == Scene.INTERIOR && vm.currentPlace.isExplorePlace()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Palette.WoodLight, Palette.Wood))
            )
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        // 1) 게임 메뉴
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            tabs.forEach { (tab, label) ->
                val selected = vm.menuTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (selected) Palette.Gold else Palette.WoodDark)
                        .border(
                            width = 1.5.dp,
                            color = if (selected) Palette.Parchment else Palette.Gold.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .clickable {
                            vm.menuTab = if (selected) MenuTab.NONE else tab
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) Palette.Ink else Palette.Parchment,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(5.dp))

        // 2) HP · MP · EXP
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lv.${vm.player.level}",
                color = Palette.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            StatBar(
                label = "HP",
                value = "${vm.player.hp}/${vm.player.maxHp}",
                ratio = vm.player.hpRatio,
                color = Palette.Health,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(5.dp))
            StatBar(
                label = "MP",
                value = "${vm.player.mp}/${vm.player.maxMp}",
                ratio = vm.player.mpRatio,
                color = Palette.Mana,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(5.dp))
            StatBar(
                label = "EXP",
                value = "${vm.player.exp}/${vm.player.expToNext}",
                ratio = vm.player.expRatio,
                color = Palette.Exp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${vm.player.gold}G",
                color = Palette.Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        // 3) 던전/탐험 정보 — 맵과 분리된 고정 줄
        if (exploring) {
            Spacer(Modifier.height(5.dp))
            val place = vm.currentPlace
            val floor = vm.dungeonFloorNumber
            val remaining = vm.dungeonFloor?.monsters?.count { it.alive } ?: 0
            val roman = romanNumeral(floor)
            val (name, floorLabel) = when (place) {
                PlaceId.GRAY_CASTLE -> "Gray Castle" to "Floor $roman"
                PlaceId.EAST_FOREST -> "Eastern Wood" to "Reach $roman"
                PlaceId.SOUTH_DESERT -> "Southern Waste" to "Reach $roman"
                PlaceId.NORTH_GLACIER -> "Northern Glacier" to "Reach $roman"
                else -> "Forgotten Crypt" to "Floor $roman"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC1B120A))
                    .border(1.dp, Palette.Gold.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$name · $floorLabel",
                    color = Palette.Gold,
                    style = ClassicType.Title,
                    maxLines = 1
                )
                Text(
                    text = "Foes Remaining · $remaining",
                    color = Palette.Parchment,
                    style = ClassicType.Label,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StatBar(
    label: String,
    value: String,
    ratio: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Palette.Ink)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                .height(16.dp)
                .background(color)
        )
        Text(
            text = "$label $value",
            color = Palette.Parchment,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
