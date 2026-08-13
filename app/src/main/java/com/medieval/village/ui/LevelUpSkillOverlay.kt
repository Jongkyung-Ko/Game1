package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.SpecialSkillCatalog
import com.medieval.village.ui.theme.Palette

/** 레벨업 시 특별스킬 슬롯(최대 3)을 갱신하는 모달 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelUpSkillOverlay(vm: GameViewModel) {
    val offer = vm.levelUpSkillOffer ?: return
    var selectedSlot by remember(offer.actorKey, offer.newLevel) { mutableIntStateOf(0) }
    val known = vm.knownSpecialSkills(offer.actorKey)
    val slots = vm.slottedSpecialIds(offer.actorKey)
    val newly = offer.newlyUnlockedIds.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC120C08))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.WoodDark, RoundedCornerShape(14.dp))
                .border(2.dp, Palette.Gold, RoundedCornerShape(14.dp))
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "레벨 업! ${offer.actorName} Lv.${offer.newLevel}",
                color = Palette.Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${offer.actorClass.label} 특별스킬을 슬롯에 장착하세요. (최대 ${SpecialSkillCatalog.MAX_SLOTS}개)",
                color = Palette.ParchmentDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
            )

            Text("장착 슬롯 — 탭해서 선택", color = Palette.Parchment, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slots.forEachIndexed { index, id ->
                    val def = id?.let { SpecialSkillCatalog.byId(it) }
                    val selected = selectedSlot == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) Color(0xFF5A3A22) else Color(0xFF2A1C12),
                                RoundedCornerShape(10.dp),
                            )
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) Palette.Gold else Color(0xFF6A5040),
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { selectedSlot = index }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("슬롯 ${index + 1}", color = Palette.ParchmentDim, fontSize = 11.sp)
                        Text(
                            def?.shortName ?: "비움",
                            color = if (def != null) Palette.Gold else Palette.ParchmentDim,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (def != null) {
                            Text("MP ${def.mpCost}", color = Palette.Mana, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("보유 스킬 — 탭하면 선택 슬롯에 장착", color = Palette.Parchment, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            if (known.isEmpty()) {
                Text("아직 해금된 특별스킬이 없다. (Lv.2부터)", color = Palette.ParchmentDim, fontSize = 12.sp)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    known.forEach { skill ->
                        val isNew = skill.id in newly
                        val equipped = skill.id in slots
                        Column(
                            modifier = Modifier
                                .background(
                                    when {
                                        isNew -> Color(0xFF4A3020)
                                        equipped -> Color(0xFF3A2A18)
                                        else -> Color(0xFF241810)
                                    },
                                    RoundedCornerShape(8.dp),
                                )
                                .border(
                                    1.dp,
                                    if (isNew) Palette.Gold else Color(0xFF6A5040),
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable {
                                    vm.setSpecialSlot(offer.actorKey, selectedSlot, skill.id)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${skill.name}${if (isNew) " · NEW" else ""}${if (equipped) " · 장착" else ""}",
                                color = Palette.Parchment,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "×${"%.1f".format(skill.damageMult)} · MP ${skill.mpCost} · ${skill.desc}",
                                color = Palette.ParchmentDim,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            WoodButton("슬롯 비우기", Modifier.fillMaxWidth()) {
                vm.clearSpecialSlot(offer.actorKey, selectedSlot)
            }
            Spacer(Modifier.height(6.dp))
            WoodButton("완료", Modifier.fillMaxWidth(), highlight = true) {
                vm.confirmLevelUpSkillOffer()
            }
        }
    }
}
