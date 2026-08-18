package com.medieval.village.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.SpecialSkillCatalog
import com.medieval.village.model.SpecialSkillDef
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.rememberCustomArtOrNull

/**
 * 직군별 스킬맵 — 레벨업/메뉴에서 스킬을 배우거나 랭크업하고, 전투 슬롯에 장착한다.
 */
@Composable
fun LevelUpSkillOverlay(vm: GameViewModel) {
    val offer = vm.levelUpSkillOffer ?: return
    @Suppress("UNUSED_EXPRESSION")
    vm.specialSkillRevision
    val art = rememberCustomArtOrNull()

    val tree = remember(offer.actorClass) { SpecialSkillCatalog.forClass(offer.actorClass) }
    var selectedId by remember(offer.actorKey, offer.actorLevel) {
        mutableStateOf(tree.firstOrNull()?.id)
    }
    var selectedSlot by remember(offer.actorKey) { mutableIntStateOf(0) }
    val points = vm.skillPointsOf(offer.actorKey)
    val slots = vm.slottedSpecialIds(offer.actorKey)
    val selected = selectedId?.let { SpecialSkillCatalog.byId(it) }
    val rank = selectedId?.let { vm.skillRankOf(offer.actorKey, it) } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC120C08))
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.WoodDark, RoundedCornerShape(14.dp))
                .border(2.dp, Palette.Gold, RoundedCornerShape(14.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (offer.fromLevelUp) {
                    "레벨 업! ${offer.actorName} Lv.${offer.actorLevel}"
                } else {
                    "${offer.actorName} 스킬맵"
                },
                color = Palette.Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${offer.actorClass.label} 전용 · SP $points" +
                    if (offer.fromLevelUp && offer.pointsGranted > 0) " (이번에 +${offer.pointsGranted})" else "",
                color = Palette.ParchmentDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )

            Text("스킬맵 — 노드를 눌러 선택", color = Palette.Parchment, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            SkillMapGraph(
                skills = tree,
                actorKey = offer.actorKey,
                vm = vm,
                selectedId = selectedId,
                onSelect = { selectedId = it },
                art = art,
            )

            Spacer(Modifier.height(10.dp))
            if (selected != null) {
                SkillDetailPanel(
                    skill = selected,
                    rank = rank,
                    actorKey = offer.actorKey,
                    actorLevel = offer.actorLevel,
                    points = points,
                    vm = vm,
                    selectedSlot = selectedSlot,
                    art = art,
                )
            }

            Spacer(Modifier.height(10.dp))
            Text("전투 장착 슬롯 (최대 ${SpecialSkillCatalog.MAX_SLOTS})", color = Palette.Parchment, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                slots.forEachIndexed { index, id ->
                    val def = id?.let { SpecialSkillCatalog.byId(it) }
                    val r = if (id != null) vm.skillRankOf(offer.actorKey, id) else 0
                    val selectedSlotUi = selectedSlot == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedSlotUi) Color(0xFF5A3A22) else Color(0xFF2A1C12),
                                RoundedCornerShape(10.dp),
                            )
                            .border(
                                if (selectedSlotUi) 2.dp else 1.dp,
                                if (selectedSlotUi) Palette.Gold else Color(0xFF6A5040),
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { selectedSlot = index }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("슬롯 ${index + 1}", color = Palette.ParchmentDim, fontSize = 10.sp)
                        Spacer(Modifier.height(4.dp))
                        SkillIcon(
                            skillId = id,
                            size = 44.dp,
                            art = art,
                            enabled = def != null,
                            circular = true,
                        )
                        Text(
                            when {
                                def == null -> "비움"
                                r > 1 -> "Lv.$r"
                                else -> def.shortName
                            },
                            color = if (def != null) Palette.Gold else Palette.ParchmentDim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                WoodButton("슬롯에 장착", Modifier.weight(1f)) {
                    val id = selectedId ?: return@WoodButton
                    if (vm.skillRankOf(offer.actorKey, id) > 0) {
                        vm.setSpecialSlot(offer.actorKey, selectedSlot, id)
                    } else {
                        vm.say("먼저 스킬을 배워야 장착할 수 있다.")
                    }
                }
                WoodButton("비우기", Modifier.weight(1f)) {
                    vm.clearSpecialSlot(offer.actorKey, selectedSlot)
                }
            }
            Spacer(Modifier.height(8.dp))
            WoodButton("완료", Modifier.fillMaxWidth(), highlight = true) {
                vm.confirmLevelUpSkillOffer()
            }
        }
    }
}

@Composable
private fun SkillDetailPanel(
    skill: SpecialSkillDef,
    rank: Int,
    actorKey: String,
    actorLevel: Int,
    points: Int,
    vm: GameViewModel,
    selectedSlot: Int,
    art: com.medieval.village.ui.village.CustomArt?,
) {
    val learned = rank > 0
    val canLearn = vm.canUnlockSkill(actorKey, skill.id)
    val canRank = vm.canRankUpSkill(actorKey, skill.id)
    val multNow = if (learned) SpecialSkillCatalog.damageMultAt(skill, rank) else skill.damageMult
    val multNext = SpecialSkillCatalog.damageMultAt(skill, (rank + 1).coerceAtMost(skill.maxRank))
    val mpNow = if (learned) SpecialSkillCatalog.mpCostAt(skill, rank) else skill.mpCost
    val prereqNames = skill.requires.mapNotNull { SpecialSkillCatalog.byId(it)?.name }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF241810), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF6A5040), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkillIcon(skillId = skill.id, size = 52.dp, art = art, enabled = learned || canLearn)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(skill.name, color = Palette.Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(skill.desc, color = Palette.ParchmentDim, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            when {
                learned -> "랭크 $rank/${skill.maxRank} · ×${"%.1f".format(multNow)} · MP $mpNow"
                else -> "미습득 · 기본 ×${"%.1f".format(skill.damageMult)} · MP ${skill.mpCost} · 필요 Lv.${skill.unlockLevel}"
            },
            color = Palette.Parchment,
            fontSize = 12.sp,
        )
        if (prereqNames.isNotEmpty()) {
            Text("선행: ${prereqNames.joinToString(" · ")}", color = Palette.ParchmentDim, fontSize = 11.sp)
        }
        if (actorLevel < skill.unlockLevel && !learned) {
            Text("캐릭터 Lv.${skill.unlockLevel} 이상 필요 (현재 $actorLevel)", color = Palette.Health, fontSize = 11.sp)
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            when {
                !learned -> WoodButton(
                    "배우기 (${skill.learnCost}SP)",
                    Modifier.weight(1f),
                    highlight = canLearn && points >= skill.learnCost,
                ) {
                    if (vm.learnSpecialSkill(actorKey, skill.id)) {
                        vm.setSpecialSlot(actorKey, selectedSlot, skill.id)
                    }
                }
                canRank -> WoodButton(
                    "강화 (${skill.rankUpCost}SP) →×${"%.1f".format(multNext)}",
                    Modifier.weight(1f),
                    highlight = points >= skill.rankUpCost,
                ) {
                    vm.rankUpSpecialSkill(actorKey, skill.id)
                }
                else -> Text(
                    "최대 랭크",
                    color = Palette.Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SkillMapGraph(
    skills: List<SpecialSkillDef>,
    actorKey: String,
    vm: GameViewModel,
    selectedId: String?,
    onSelect: (String) -> Unit,
    art: com.medieval.village.ui.village.CustomArt?,
) {
    val cols = (skills.maxOfOrNull { it.mapCol } ?: 0) + 1
    val rows = (skills.maxOfOrNull { it.mapRow } ?: 0) + 1
    val cellW = 86.dp
    val cellH = 92.dp
    val byPos = skills.associateBy { it.mapCol to it.mapRow }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF1A120C), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF5A4030), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.width(cellW * cols).height(cellH * rows)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cw = size.width / cols
                val ch = size.height / rows
                skills.forEach { skill ->
                    skill.requires.forEach { reqId ->
                        val req = SpecialSkillCatalog.byId(reqId) ?: return@forEach
                        val x0 = (req.mapCol + 0.5f) * cw
                        val y0 = (req.mapRow + 0.5f) * ch
                        val x1 = (skill.mapCol + 0.5f) * cw
                        val y1 = (skill.mapRow + 0.5f) * ch
                        val learned = vm.skillRankOf(actorKey, reqId) > 0
                        drawLine(
                            color = if (learned) Color(0xFFD9A441) else Color(0xFF5A4030),
                            start = Offset(x0, y0),
                            end = Offset(x1, y1),
                            strokeWidth = if (learned) 3.5f else 2f,
                            cap = StrokeCap.Round,
                            pathEffect = if (learned) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                        )
                    }
                }
            }
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val skill = byPos[c to r] ?: continue
                    val rank = vm.skillRankOf(actorKey, skill.id)
                    val learned = rank > 0
                    val available = vm.canUnlockSkill(actorKey, skill.id)
                    val selected = selectedId == skill.id
                    Column(
                        modifier = Modifier
                            .offset(x = cellW * c, y = cellH * r)
                            .size(cellW, cellH)
                            .padding(4.dp)
                            .background(
                                when {
                                    selected -> Color(0xFF5A3A18)
                                    learned -> Color(0xFF3A2A18)
                                    available -> Color(0xFF2A2418)
                                    else -> Color(0xFF1A1410)
                                },
                                RoundedCornerShape(10.dp),
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = when {
                                    selected -> Palette.Gold
                                    learned -> Color(0xFFC8A050)
                                    available -> Color(0xFF8A7050)
                                    else -> Color(0xFF4A3A2A)
                                },
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { onSelect(skill.id) }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        SkillIcon(
                            skillId = skill.id,
                            size = 40.dp,
                            art = art,
                            enabled = learned || available,
                            showBorder = false,
                        )
                        Text(
                            skill.shortName,
                            color = when {
                                learned || available -> Palette.Parchment
                                else -> Color(0xFF6A5A4A)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                        Text(
                            when {
                                learned -> "Lv.$rank"
                                available -> "배움"
                                else -> "Lv.${skill.unlockLevel}"
                            },
                            color = when {
                                learned -> Palette.Gold
                                available -> Palette.Mana
                                else -> Color(0xFF5A4A3A)
                            },
                            fontSize = 9.sp,
                        )
                    }
                }
            }
        }
    }
}
