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
import com.medieval.village.ui.theme.Palette

private val tabs = listOf(
    MenuTab.STATUS to "Status",
    MenuTab.INVENTORY to "Inventory",
    MenuTab.EQUIPMENT to "Equipment",
    MenuTab.SYSTEM to "System"
)

@Composable
fun TopMenuBar(vm: GameViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Palette.WoodLight, Palette.Wood))
            )
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(5.dp))

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
            Spacer(Modifier.width(7.dp))
            StatBar(
                label = "HP",
                value = "${vm.player.hp}/${vm.player.maxHp}",
                ratio = vm.player.hpRatio,
                color = Palette.Health,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            StatBar(
                label = "MP",
                value = "${vm.player.mp}/${vm.player.maxMp}",
                ratio = vm.player.mpRatio,
                color = Palette.Mana,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "${vm.player.gold}G",
                color = Palette.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${vm.player.day}일",
                color = Palette.ParchmentDim,
                fontSize = 11.sp
            )
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
