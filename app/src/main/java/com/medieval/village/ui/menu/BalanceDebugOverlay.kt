package com.medieval.village.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.BalanceZone
import com.medieval.village.model.BossTier
import com.medieval.village.model.CombatBalance
import com.medieval.village.ui.ParchmentPanel
import com.medieval.village.ui.SectionTitle
import com.medieval.village.ui.ThinDivider
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun BalanceDebugOverlay(vm: GameViewModel, modifier: Modifier = Modifier) {
    if (!vm.debugPanelOpen) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEE0A0704))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { vm.closeDebugPanel() }
            .padding(10.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        ParchmentPanel(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Debug · 마을 난이도 / 전투 밸런스",
                    color = Palette.Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                WoodButton("닫기") { vm.closeDebugPanel() }
            }
            Spacer(Modifier.height(6.dp))
            ThinDivider()
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CurrentSnapshot(vm)
                Spacer(Modifier.height(8.dp))
                SectionTitle("마을 난이도 사다리")
                Text(
                    "월드맵 이동 제한은 없습니다. 아래는 권장 구간입니다.",
                    color = Palette.ParchmentDim,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
                Spacer(Modifier.height(4.dp))
                VillageTable(vm)
                Spacer(Modifier.height(10.dp))
                SectionTitle("오크헤이븐 탐험 순서")
                ZoneTable()
                Spacer(Modifier.height(10.dp))
                SectionTitle("기사 기준 기대 스탯 (상점 장비 가정)")
                HeroCurveTable()
                Spacer(Modifier.height(10.dp))
                SectionTitle("전투 공식")
                Text(
                    "플레이어 공격 = ATK − 몹 방어 (최소 1)\n" +
                        "몹 근접 = (power − DEF) 최소 ${CombatBalance.MIN_HIT}" +
                        " · 보스 ×${"%.2f".format(CombatBalance.BOSS_HIT_MULT)}\n" +
                        "몹 원거리 = 근접과 같은 DEF, ×${"%.2f".format(CombatBalance.RANGED_HIT_MULT)}\n" +
                        "잡몹: 권장 레벨에서 HP 약 20%(5대) · 기본 공격 4~5대에 사망\n" +
                        "보스: HP 약 33%(3대) · 기본 10~13대 / 특수기 3~4회\n" +
                        "오크헤이븐 무한층은 회색 성 클리어 레벨(12)을 넘지 않습니다.",
                    color = Palette.Parchment,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun CurrentSnapshot(vm: GameViewModel) {
    val row = CombatBalance.village(vm.currentSettlement)
    val zone = CombatBalance.zoneOf(vm.currentPlace)
    val floor = vm.dungeonFloorNumber
    val you = vm.player
    Text(
        "현재  ${you.name}  ${you.heroJob.label}  Lv.${you.level}   HP ${you.maxHp}   ATK ${vm.totalAtk}   DEF ${vm.totalDef}",
        color = Palette.Parchment,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    )
    Text(
        "마을  ${row.nameKo}  ${row.stars}  입장 Lv.${CombatBalance.enterLabel(row)}  클리어 Lv.${CombatBalance.clearLabel(row)}",
        color = Palette.Gold,
        fontSize = 12.sp,
    )
    if (zone != null && vm.dungeonFloor != null) {
        val target = CombatBalance.targetLevel(zone, floor)
        val trash = CombatBalance.roll(zone, floor, boss = BossTier.NONE, rng = Random(0))
        val boss = CombatBalance.roll(
            zone, floor, rng = Random(0),
            boss = if (floor >= zone.clearFloor) BossTier.FINAL else BossTier.MID,
        )
        val inHit = CombatBalance.meleeHit(trash.power, vm.totalDef, false)
        val outHit = CombatBalance.playerHit(vm.totalAtk, trash.armor)
        val bossHit = CombatBalance.meleeHit(boss.power, vm.totalDef, true)
        Text(
            "${zone.labelKo} ${zone.floorWord(floor)}  권장 Lv.${"%.1f".format(target)}\n" +
                "잡몹 power ${trash.power} / HP ${trash.hp} / 방어 ${trash.armor}  → 맞기 $inHit · 때리기 $outHit\n" +
                "보스 power ${boss.power} / HP ${boss.hp} / 방어 ${boss.armor}  → 맞기 $bossHit",
            color = Palette.ParchmentDim,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        )
    } else {
        val expected = CombatBalance.heroAt(you.level)
        Text(
            "권장 기사 스탯  HP ${expected.hp}  ATK ${expected.atk}  DEF ${expected.def}  (장비 ATK ${expected.equipAtk} / DEF ${expected.equipDef})",
            color = Palette.ParchmentDim,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun VillageTable(vm: GameViewModel) {
    DebugHeader("마을", "난이도", "입장", "클리어", "전직")
    CombatBalance.villages.forEach { row ->
        val here = row.id == vm.currentSettlement
        DebugRow(
            a = "${row.order}. ${row.nameKo}",
            b = "${row.stars} ${row.difficulty}",
            c = "Lv.${CombatBalance.enterLabel(row)}",
            d = "Lv.${CombatBalance.clearLabel(row)}\n${row.clearNote}",
            e = row.promotion,
            highlight = here,
        )
    }
}

@Composable
private fun ZoneTable() {
    DebugHeader("지대", "난이도", "1층", "클리어층", "상한")
    BalanceZone.entries.forEach { z ->
        DebugRow(
            a = z.labelKo,
            b = z.stars,
            c = "Lv.${z.enterLevel.roundToInt()}",
            d = "${z.floorWord(z.clearFloor)} · Lv.${z.clearLevel.roundToInt()}",
            e = "Lv.${z.levelCap.roundToInt()}",
        )
    }
}

@Composable
private fun HeroCurveTable() {
    DebugHeader("Lv", "HP", "ATK", "DEF", "장비")
    CombatBalance.milestoneHeroes().forEach { h ->
        DebugRow(
            a = "Lv.${h.level}",
            b = "${h.hp}",
            c = "${h.atk}",
            d = "${h.def}",
            e = "+${h.equipAtk}/+${h.equipDef}",
        )
    }
}

@Composable
private fun DebugHeader(a: String, b: String, c: String, d: String, e: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x553A2A18), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        DebugCell(a, 1.35f, Palette.Gold, bold = true)
        DebugCell(b, 1.15f, Palette.Gold, bold = true)
        DebugCell(c, 0.75f, Palette.Gold, bold = true)
        DebugCell(d, 1.2f, Palette.Gold, bold = true)
        DebugCell(e, 0.85f, Palette.Gold, bold = true)
    }
}

@Composable
private fun DebugRow(
    a: String,
    b: String,
    c: String,
    d: String,
    e: String,
    highlight: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (highlight) Color(0x445A4020) else Color.Transparent,
                RoundedCornerShape(4.dp),
            )
            .then(
                if (highlight) Modifier.border(1.dp, Palette.Gold.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                else Modifier
            )
            .padding(horizontal = 6.dp, vertical = 5.dp),
    ) {
        DebugCell(a, 1.35f, if (highlight) Palette.Gold else Palette.Parchment, bold = true)
        DebugCell(b, 1.15f, Palette.ParchmentDim)
        DebugCell(c, 0.75f, Palette.Parchment)
        DebugCell(d, 1.2f, Palette.ParchmentDim)
        DebugCell(e, 0.85f, Palette.Parchment)
    }
}

@Composable
private fun RowScope.DebugCell(
    text: String,
    weight: Float,
    color: Color,
    bold: Boolean = false,
) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        lineHeight = 13.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.weight(weight),
    )
}
