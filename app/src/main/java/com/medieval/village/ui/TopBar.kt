package com.medieval.village.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.medieval.village.ui.skin.SkinInsets
import com.medieval.village.ui.skin.drawNineSlice
import com.medieval.village.ui.skin.nineSliceBackground
import com.medieval.village.ui.skin.rememberUiSkin
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
        // 1) 게임 메뉴 — 도안의 각인 목판
        val skin = rememberUiSkin()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEach { (tab, label) ->
                val selected = vm.menuTab == tab
                val plate = skin?.buttonWood
                val tile = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable { vm.menuTab = if (selected) MenuTab.NONE else tab }
                Box(
                    modifier = if (plate != null) {
                        tile.nineSliceBackground(plate, SkinInsets.Button)
                    } else {
                        tile.clip(RoundedCornerShape(7.dp))
                            .background(if (selected) Palette.Gold else Palette.WoodDark)
                            .border(
                                width = 1.5.dp,
                                color = if (selected) Palette.Parchment else Palette.Gold.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(7.dp)
                            )
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = when {
                            plate == null && selected -> Palette.Ink
                            selected -> Color(0xFFFFE9A8)
                            else -> Color(0xFFCDB68A)
                        },
                        style = ClassicType.Tab,
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
            Text("HP", color = Color(0xFFD9B972), style = ClassicType.Bar)
            Spacer(Modifier.width(3.dp))
            StatBar(
                label = "HP",
                value = "${vm.player.hp}/${vm.player.maxHp}",
                ratio = vm.player.hpRatio,
                color = Color(0xFFA82C22),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            Text("MP", color = Color(0xFFD9B972), style = ClassicType.Bar)
            Spacer(Modifier.width(3.dp))
            StatBar(
                label = "MP",
                value = "${vm.player.mp}/${vm.player.maxMp}",
                ratio = vm.player.mpRatio,
                color = Color(0xFF2E6FB5),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            Text("EXP", color = Color(0xFFD9B972), style = ClassicType.Bar)
            Spacer(Modifier.width(3.dp))
            StatBar(
                label = "EXP",
                value = "${vm.player.exp}/${vm.player.expToNext}",
                ratio = vm.player.expRatio,
                color = Color(0xFFB98D1E),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${vm.player.gold}G",
                color = Palette.Gold,
                style = ClassicType.Bar,
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
            val strip = skin?.infoStrip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (strip != null) {
                            Modifier.nineSliceBackground(strip, SkinInsets.InfoStrip)
                        } else {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC1B120A))
                                .border(1.dp, Palette.Gold.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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
    val frame = rememberUiSkin()?.barFrame
    if (frame != null) {
        // 채워지는 양은 액자 안쪽 홈에만 그리고, 금테는 텍스처 그대로 위에 얹는다
        Box(modifier = modifier.height(19.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.matchParentSize()) {
                val inset = size.height * 0.30f
                val capW = size.height * 0.62f
                val channel = (size.width - capW * 2f).coerceAtLeast(1f)
                drawRect(
                    color = color,
                    topLeft = Offset(capW, inset),
                    size = Size(channel * ratio.coerceIn(0f, 1f), size.height - inset * 2f),
                )
                drawNineSlice(frame, SkinInsets.BarFrame, size.width, size.height)
            }
            Text(
                text = value,
                color = Color(0xFFF6EAD0),
                style = ClassicType.Bar,
                maxLines = 1,
            )
        }
        return
    }
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
