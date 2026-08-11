package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.medieval.village.game.GameViewModel
import com.medieval.village.ui.theme.Palette

/**
 * 맵 위 선두 교대 UI — 헤더에 가려지지 않도록 오버레이로 둔다.
 */
@Composable
fun PartySwitchBar(
    vm: GameViewModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Color(0xCC1B120A), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chip(vm.frontStatusLabel(), Palette.Gold)
        WoodButton(
            text = "교대",
            highlight = true,
            enabled = vm.partyActorCount > 1,
        ) {
            vm.cyclePartyFront()
        }
    }
}
