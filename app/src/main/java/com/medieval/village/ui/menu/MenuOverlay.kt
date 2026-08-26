package com.medieval.village.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.game.MenuTab
import com.medieval.village.model.ActorClass
import com.medieval.village.model.EQUIP_SLOTS
import com.medieval.village.model.ItemType
import com.medieval.village.model.SpecialSkillCatalog
import com.medieval.village.ui.Chip
import com.medieval.village.ui.EquipmentDoll
import com.medieval.village.ui.ItemIcon
import com.medieval.village.ui.ListRow
import com.medieval.village.ui.MercPortrait
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.ParchmentPanel
import com.medieval.village.ui.SectionTitle
import com.medieval.village.ui.StatBar
import com.medieval.village.ui.ThinDivider
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette

@Composable
fun MenuOverlay(vm: GameViewModel, modifier: Modifier = Modifier) {
    val tab = vm.menuTab
    if (tab == MenuTab.NONE || tab == MenuTab.WORLD_MAP) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC120C07))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { vm.menuTab = MenuTab.NONE }
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        ParchmentPanel(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when (tab) {
                        MenuTab.STATUS -> "Status · 상태"
                        MenuTab.INVENTORY -> "Inventory · 가방"
                        MenuTab.EQUIPMENT -> "Equipment · 장비"
                        else -> "System · 설정"
                    },
                    color = Palette.Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                WoodButton("닫기") { vm.menuTab = MenuTab.NONE }
            }
            Spacer(Modifier.height(8.dp))
            ThinDivider()
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (tab) {
                    MenuTab.STATUS -> StatusTab(vm)
                    MenuTab.INVENTORY -> InventoryTab(vm)
                    MenuTab.EQUIPMENT -> EquipmentTab(vm)
                    MenuTab.SYSTEM -> SystemTab(vm)
                    else -> Unit
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.StatusTab(vm: GameViewModel) {
    val p = vm.player
    Text("${p.name} · ${p.title}", color = Palette.Parchment, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Text("Lv.${p.level}   ${p.day}일차", color = Palette.ParchmentDim, fontSize = 12.sp)
    Spacer(Modifier.height(10.dp))

    StatBar("HP", "${p.hp}/${p.maxHp}", p.hpRatio, Palette.Health, Modifier.fillMaxWidth())
    Spacer(Modifier.height(5.dp))
    StatBar("MP", "${p.mp}/${p.maxMp}", p.mpRatio, Palette.Mana, Modifier.fillMaxWidth())
    Spacer(Modifier.height(5.dp))
    StatBar("EXP", "${p.exp}/${p.expToNext}", p.expRatio, Palette.Exp, Modifier.fillMaxWidth())

    Spacer(Modifier.height(12.dp))
    SectionTitle("전투력")
    ListRow("공격력", "기본 ${p.baseAtk} + 장비 ${vm.equipAtk} + 마법 ${vm.skillPower}") {
        Chip("${vm.totalAtk}", Palette.Blood)
    }
    ListRow("방어력", "기본 ${p.baseDef} + 장비 ${vm.equipDef}") {
        Chip("${vm.totalDef}", Palette.Sky)
    }

    Spacer(Modifier.height(8.dp))
    SectionTitle("능력치")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("힘 ${p.str}")
        Chip("민첩 ${p.agi}")
        Chip("지능 ${p.intel}")
        Chip("행운 ${p.luck}")
        Chip("소지금 ${p.gold}G", Palette.WoodLight)
    }

    Spacer(Modifier.height(10.dp))
    SectionTitle("기록")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("좀비 둥지 ${p.dungeonDepth}층")
        Chip("동쪽 숲 ${p.forestDepth}지대")
        Chip("남쪽 사막 ${p.desertDepth}지대")
        Chip("북쪽 빙하 ${p.glacierDepth}지대")
        Chip(
            if (p.castleCleared) "White Castle 해방"
            else "Gray Castle ${p.castleDepth}층"
        )
        Chip("대련 ${vm.arenaWins}승 ${vm.arenaLosses}패")
        Chip(if (p.blessing > 0) "축복 ${p.blessing}일 남음" else "축복 없음")
    }

    Spacer(Modifier.height(10.dp))
    SectionTitle("익힌 마법")
    if (vm.skills.isEmpty()) {
        Text("아직 없다. 마법학교에 가보자.", color = Palette.ParchmentDim, fontSize = 12.sp)
    } else {
        vm.skills.forEach { s ->
            ListRow(s.name, "${s.desc} (MP ${s.mpCost})")
        }
    }

    Spacer(Modifier.height(10.dp))
    SectionTitle("직업 스킬맵 (전투)")
    Text(
        "직군별로만 배울 수 있다. 레벨업으로 SP를 모아 스킬을 배우거나 강화한다.",
        color = Palette.ParchmentDim,
        fontSize = 11.sp,
    )
    Spacer(Modifier.height(6.dp))
    SpecialSkillSlotEditor(
        vm = vm,
        actorKey = GameViewModel.HERO_SKILL_KEY,
        actorLabel = "${vm.player.name} · 모험가",
        actorClass = ActorClass.ADVENTURER,
    )
    vm.activeParty.forEach { merc ->
        Spacer(Modifier.height(8.dp))
        SpecialSkillSlotEditor(
            vm = vm,
            actorKey = merc.id,
            actorLabel = "${merc.name} · ${merc.role}",
            actorClass = SpecialSkillCatalog.actorClassOf(merc),
        )
    }

    Spacer(Modifier.height(10.dp))
    SectionTitle("용병 원정대 (${vm.activeParty.size}/${GameViewModel.MAX_ACTIVE_MERCENARY})")
    var gearMercId by remember { mutableStateOf<String?>(null) }
    if (vm.party.isEmpty()) {
        Text("고용한 용병이 없다. 용병고용소를 찾아가자.", color = Palette.ParchmentDim, fontSize = 12.sp)
    } else {
        vm.party.forEach { m ->
            val active = m.id in vm.activeMercenaryIds
            val selected = gearMercId == m.id
            ListRow(
                title = "${m.name} (${m.role}) · Lv.${m.level}",
                subtitle = "기여 +${m.power} · EXP ${m.exp}/${m.expToNext} · ${if (active) "동행 중" else "대기 중"}",
                leading = { MercPortrait(m, size = 52.dp) },
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        WoodButton(
                            text = if (active) "선택 해제" else "선택",
                            enabled = active || vm.activeParty.size < GameViewModel.MAX_ACTIVE_MERCENARY,
                            highlight = active
                        ) {
                            vm.toggleMercenaryActive(m)
                        }
                        WoodButton(
                            text = if (selected) "장비 닫기" else "장비",
                            highlight = selected
                        ) {
                            gearMercId = if (selected) null else m.id
                        }
                    }
                }
            )
            StatBar("EXP", "${m.exp}/${m.expToNext}", m.expRatio, Palette.Gold, Modifier.fillMaxWidth())
            if (selected) {
                MercGearPanel(vm, m.id)
            }
            ThinDivider()
        }
        Text(
            "선택한 최대 2명만 동행하며 좀비 둥지에서 경험치를 얻고 레벨업합니다. 장비는 가방에서 나눠 줄 수 있습니다.",
            color = Palette.ParchmentDim,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
private fun ColumnScope.MercGearPanel(vm: GameViewModel, mercId: String) {
    val merc = vm.party.firstOrNull { it.id == mercId } ?: return
    Text(
        "${merc.name} 장비 · 기여 +${merc.power}",
        color = Palette.Parchment,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    EquipmentDoll(
        equipment = merc.equipment,
        onSlotClick = { slot -> vm.unequipMerc(mercId, slot) },
        modifier = Modifier.padding(bottom = 6.dp)
    )
    Text(
        "가방에서 장착",
        color = Palette.Gold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
    val gear = vm.inventory.toList().filter { it.item.isEquipment }
    if (gear.isEmpty()) {
        Text("장착할 장비가 가방에 없다.", color = Palette.ParchmentDim, fontSize = 11.sp)
    } else {
        gear.forEach { entry ->
            ListRow(
                title = "${entry.item.name} x${entry.count}",
                subtitle = "${entry.item.type.label} · 공격 ${entry.item.atk} · 방어 ${entry.item.def}",
                leading = { ItemIcon(entry.item) },
                trailing = {
                    WoodButton("장착", highlight = true) { vm.equipMerc(mercId, entry.item) }
                }
            )
        }
    }
}

@Composable
private fun ColumnScope.InventoryTab(vm: GameViewModel) {
    Text("소지금 ${vm.player.gold}G", color = Palette.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    if (vm.inventory.isEmpty()) {
        Text("가방이 비어 있다.", color = Palette.ParchmentDim, fontSize = 13.sp)
        return
    }
    vm.inventory.toList().forEach { entry ->
        ListRow(
            title = "${entry.item.name} x${entry.count}",
            subtitle = buildString {
                append(entry.item.type.label)
                if (entry.item.atk != 0) append(" · 공격 ${entry.item.atk}")
                if (entry.item.def != 0) append(" · 방어 ${entry.item.def}")
                if (entry.item.desc.isNotEmpty()) append("\n${entry.item.desc}")
            },
            leading = { ItemIcon(entry.item) },
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (entry.item.type == ItemType.CONSUMABLE) {
                        WoodButton("사용", highlight = true) { vm.useItem(entry.item) }
                    } else {
                        WoodButton("장착", highlight = true) { vm.equip(entry.item) }
                    }
                }
            }
        )
        ThinDivider()
    }
    Spacer(Modifier.height(8.dp))
    MessageLog(vm.log)
}

@Composable
private fun ColumnScope.EquipmentTab(vm: GameViewModel) {
    SectionTitle("착용 중")
    Text(
        "사람 윤곽 옆 상자에 장착 장비가 표시됩니다. 상자를 누르면 해제합니다.",
        color = Palette.ParchmentDim,
        fontSize = 11.sp
    )
    Spacer(Modifier.height(6.dp))
    EquipmentDoll(
        equipment = vm.equipment,
        onSlotClick = { slot -> vm.unequip(slot) }
    )
    Spacer(Modifier.height(8.dp))
    EQUIP_SLOTS.forEach { slot ->
        val eq = vm.equipment[slot]
        ListRow(
            title = "[${slot.label}] ${eq?.displayName ?: "―"}",
            subtitle = if (eq == null) "비어 있음" else "공격 ${eq.atk} · 방어 ${eq.def}",
            leading = { ItemIcon(eq?.item) },
            trailing = {
                if (eq != null) WoodButton("해제") { vm.unequip(slot) }
            }
        )
        ThinDivider()
    }

    Spacer(Modifier.height(10.dp))
    SectionTitle("가방 속 장비")
    val gear = vm.inventory.toList().filter { it.item.isEquipment }
    if (gear.isEmpty()) {
        Text("장착할 수 있는 장비가 없다.", color = Palette.ParchmentDim, fontSize = 12.sp)
    } else {
        gear.forEach { entry ->
            ListRow(
                title = "${entry.item.name} x${entry.count}",
                subtitle = "${entry.item.type.label} · 공격 ${entry.item.atk} · 방어 ${entry.item.def}",
                leading = { ItemIcon(entry.item) },
                trailing = {
                    WoodButton("장착", highlight = true) { vm.equip(entry.item) }
                }
            )
        }
    }

    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("총 공격 ${vm.totalAtk}", Palette.Blood)
        Chip("총 방어 ${vm.totalDef}", Palette.Sky)
    }
    Spacer(Modifier.height(8.dp))
    MessageLog(vm.log)
}

@Composable
private fun ColumnScope.SystemTab(vm: GameViewModel) {
    var confirmNew by remember { mutableStateOf(false) }
    var confirmLoad by remember { mutableStateOf<Int?>(null) }
    var confirmDelete by remember { mutableStateOf<Int?>(null) }
    @Suppress("UNUSED_VARIABLE")
    val saveTick = vm.saveRevision
    val slots = remember(saveTick) { vm.saveSlotInfos() }

    SectionTitle("저장 / 불러오기")
    Text(
        "슬롯 5개에 진행 상황을 저장할 수 있습니다. 탐험 중 저장하면 마을로 정리된 뒤 저장됩니다.",
        color = Palette.ParchmentDim,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
    Spacer(Modifier.height(8.dp))
    slots.forEach { slot ->
        val summary = if (slot.empty) {
            "비어 있음"
        } else {
            "${slot.playerName} · Lv.${slot.level} · ${slot.day}일 · ${slot.gold}G\n${slot.settlementName} · ${slot.placeLabel}"
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x332A1C12), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text("슬롯 ${slot.slot}", color = Palette.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(summary, color = Palette.Parchment, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WoodButton("저장", highlight = true) { vm.saveGame(slot.slot) }
                WoodButton("불러오기", enabled = !slot.empty) { confirmLoad = slot.slot }
                WoodButton("삭제", enabled = !slot.empty) { confirmDelete = slot.slot }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    confirmLoad?.let { slot ->
        Text("슬롯 $slot 을(를) 불러올까요? 현재 진행은 사라집니다.", color = Palette.Health, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WoodButton("불러오기", highlight = true) {
                vm.loadGame(slot)
                confirmLoad = null
            }
            WoodButton("취소") { confirmLoad = null }
        }
        Spacer(Modifier.height(10.dp))
    }
    confirmDelete?.let { slot ->
        Text("슬롯 $slot 저장을 지울까요?", color = Palette.Health, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WoodButton("삭제", highlight = true) {
                vm.deleteSave(slot)
                confirmDelete = null
            }
            WoodButton("취소") { confirmDelete = null }
        }
        Spacer(Modifier.height(10.dp))
    }

    SectionTitle("조작 방법")
    Text(
        "· 마을 화면에서 건물을 누르면 그곳까지 걸어가 자동으로 들어갑니다.\n" +
            "· 빈 땅을 누르면 길을 따라 그 지점으로 이동합니다.\n" +
            "· 문 앞에 서면 아래에 '들어가기' 버튼이 나타납니다.\n" +
            "· 상단 메뉴로 상태·가방·장비를 언제든 확인할 수 있습니다.\n" +
            "· 던전에서는 맵 아래 패드로 이동하고, 반투명 특별스킬·하단 공격 버튼으로 싸웁니다.",
        color = Palette.Parchment,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )

    Spacer(Modifier.height(12.dp))
    SectionTitle("게임 정보")
    Text(
        "중세마을 이야기 v0.4.37\nKotlin · Jetpack Compose 로 제작된 초안입니다.",
        color = Palette.ParchmentDim,
        fontSize = 12.sp,
        lineHeight = 17.sp
    )

    Spacer(Modifier.height(14.dp))
    SectionTitle("새 게임")
    if (!confirmNew) {
        WoodButton("처음부터 다시 시작") { confirmNew = true }
    } else {
        Text("정말 처음부터 다시 시작할까요? 진행 상황이 사라집니다.", color = Palette.Health, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WoodButton("네, 초기화", highlight = true) {
                vm.newGame()
                confirmNew = false
            }
            WoodButton("아니요") { confirmNew = false }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecialSkillSlotEditor(
    vm: GameViewModel,
    actorKey: String,
    actorLabel: String,
    actorClass: ActorClass,
) {
    @Suppress("UNUSED_EXPRESSION")
    vm.specialSkillRevision
    var selectedSlot by remember(actorKey) { mutableStateOf(0) }
    val known = vm.knownSpecialSkills(actorKey)
    val slots = vm.slottedSpecialIds(actorKey)
    val points = vm.skillPointsOf(actorKey)
    Text(actorLabel, color = Palette.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Text(
        "${actorClass.label} · 습득 ${known.size}/6 · SP $points",
        color = Palette.ParchmentDim,
        fontSize = 11.sp,
    )
    Spacer(Modifier.height(4.dp))
    WoodButton("스킬맵 열기") {
        vm.openSkillMap(actorKey = actorKey, actorClass = actorClass, fromLevelUp = false)
    }
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        slots.forEachIndexed { index, id ->
            val def = id?.let { SpecialSkillCatalog.byId(it) }
            val rank = if (id != null) vm.skillRankOf(actorKey, id) else 0
            val selected = selectedSlot == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        if (selected) Color(0xFF5A4020) else Color(0xFF2A1C12),
                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    )
                    .clickable { selectedSlot = index }
                    .padding(6.dp),
            ) {
                com.medieval.village.ui.SkillIcon(
                    skillId = id,
                    size = 40.dp,
                    enabled = def != null,
                    circular = true,
                )
                Text(
                    text = when {
                        def == null -> "빈칸"
                        rank > 1 -> "Lv$rank"
                        else -> def.shortName
                    },
                    color = if (selected) Palette.Gold else Palette.Parchment,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        WoodButton("비우기") { vm.clearSpecialSlot(actorKey, selectedSlot) }
    }
    if (known.isEmpty()) {
        Text("스킬맵에서 SP로 스킬을 배우세요. (Lv.2+)", color = Palette.ParchmentDim, fontSize = 11.sp)
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            known.forEach { skill ->
                val rank = vm.skillRankOf(actorKey, skill.id)
                val mult = SpecialSkillCatalog.damageMultAt(skill, rank)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (skill.id in slots) Color(0xFF5A4020) else Palette.WoodDark,
                            androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        )
                        .clickable { vm.setSpecialSlot(actorKey, selectedSlot, skill.id) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    com.medieval.village.ui.SkillIcon(
                        skillId = skill.id,
                        size = 28.dp,
                        showBorder = false,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${skill.name} Lv.$rank ×${"%.1f".format(mult)}",
                        color = Palette.Parchment,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
