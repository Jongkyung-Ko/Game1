package com.medieval.village.ui.place

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.EQUIP_SLOTS
import com.medieval.village.model.InteriorNpcCatalog
import com.medieval.village.model.Item
import com.medieval.village.model.ItemCatalog
import com.medieval.village.model.MercenaryCatalog
import com.medieval.village.model.PlaceId
import com.medieval.village.model.SkillCatalog
import com.medieval.village.model.Village
import com.medieval.village.ui.Chip
import com.medieval.village.ui.ListRow
import com.medieval.village.ui.MessageLog
import com.medieval.village.ui.SectionTitle
import com.medieval.village.ui.ThinDivider
import com.medieval.village.ui.WoodButton
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.rememberCustomArtOrNull
import com.medieval.village.ui.village.rememberKenneyAtlas
import kotlin.math.hypot

@Composable
fun PlaceScreen(vm: GameViewModel, id: PlaceId, modifier: Modifier = Modifier) {
    if (id == PlaceId.PUB) {
        PubScreen(vm = vm, modifier = modifier)
        return
    }
    if (id == PlaceId.DUNGEON) {
        DungeonScreen(vm = vm, modifier = modifier)
        return
    }
    val place = Village.of(id)
    val atlas = rememberKenneyAtlas()
    val art = rememberCustomArtOrNull()
    val animTime = vm.animTime
    val speechId = vm.interiorSpeakerId
    val speechText = vm.interiorSpeech
    val npcs = remember(id) { InteriorNpcCatalog.forPlace(id) }

    Column(modifier = modifier.fillMaxSize().background(Palette.WoodDark)) {

        // 배경 일러스트 (주인공·용병·실내 NPC + 말풍선)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(npcs) {
                        detectTapGestures { tap ->
                            val hit = npcs.minByOrNull { npc ->
                                hypot(tap.x - size.width * npc.fx, tap.y - size.height * npc.fy)
                            }
                            if (hit != null) {
                                val dist = hypot(
                                    tap.x - size.width * hit.fx,
                                    tap.y - size.height * hit.fy
                                )
                                if (dist < minOf(size.width, size.height) * 0.18f) {
                                    vm.talkToInteriorNpc(hit.id)
                                }
                            }
                        }
                    }
            ) {
                drawInterior(
                    atlas = atlas,
                    art = art,
                    id = id,
                    w = size.width,
                    h = size.height,
                    companions = vm.activeParty,
                    animTime = animTime,
                    speechNpcId = speechId,
                    speechText = speechText,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color(0xCC1B120A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Column {
                    Text(place.name, color = Palette.Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (npcs.isEmpty()) place.subtitle else "NPC를 탭하면 인사한다 · ${place.subtitle}",
                        color = Palette.ParchmentDim,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 행동
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (id) {
                PlaceId.HOME -> HomeActions(vm)
                PlaceId.SHOP -> ShopActions(vm, ItemCatalog.generalGoods, "잡화 진열대")
                PlaceId.WEAPON_SHOP -> ShopActions(vm, ItemCatalog.weaponGoods, "무기와 방어구")
                PlaceId.HOSPITAL -> HospitalActions(vm)
                PlaceId.CHURCH -> ChurchActions(vm)
                PlaceId.INN -> InnActions(vm)
                PlaceId.PUB -> Unit
                PlaceId.ARENA -> ArenaActions(vm)
                PlaceId.DUNGEON -> Unit
                PlaceId.BLACKSMITH -> BlacksmithActions(vm)
                PlaceId.MAGIC_SCHOOL -> MagicSchoolActions(vm)
                PlaceId.MERCENARY -> MercenaryActions(vm)
            }
            Spacer(Modifier.height(10.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            MessageLog(vm.log)
            Spacer(Modifier.height(8.dp))
            WoodButton(
                text = "마을로 나가기",
                highlight = true,
                modifier = Modifier.fillMaxWidth()
            ) { vm.leavePlace() }
        }
    }
}

// ---------------------------------------------------------------- 각 장소

@Composable
private fun ColumnScope.HomeActions(vm: GameViewModel) {
    SectionTitle("풍요의 마을 오두막")
    Text(
        "신성한 포도주의 향기가 희미한 작은 집. 저주가 스며든 밤에도 여기서 하루를 마무리할 수 있다.",
        color = Palette.ParchmentDim, fontSize = 12.sp
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WoodButton("잠자기 (무료 · 완전 회복)", highlight = true) { vm.sleepAtHome() }
    }
    Spacer(Modifier.height(10.dp))
    ThinDivider()
    Spacer(Modifier.height(8.dp))
    SectionTitle("가방 정리")
    val potions = vm.inventory.toList().filter { it.item.healHp > 0 || it.item.healMp > 0 }
    if (potions.isEmpty()) {
        Text("쓸 만한 회복 아이템이 없다.", color = Palette.ParchmentDim, fontSize = 12.sp)
    } else {
        potions.forEach { e ->
            ListRow("${e.item.name} x${e.count}", e.item.desc) {
                WoodButton("사용") { vm.useItem(e.item) }
            }
        }
    }
}

@Composable
private fun ColumnScope.ShopActions(vm: GameViewModel, goods: List<Item>, title: String) {
    var selling by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionTitle(if (selling) "물건 팔기" else title, Modifier.weight(1f))
        Chip("${vm.player.gold}G", Palette.WoodLight)
    }
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WoodButton("사기", highlight = !selling) { selling = false }
        WoodButton("팔기", highlight = selling) { selling = true }
    }
    Spacer(Modifier.height(8.dp))

    if (!selling) {
        goods.forEach { item ->
            ListRow(
                title = item.name,
                subtitle = buildString {
                    append(item.type.label)
                    if (item.atk != 0) append(" · 공격 ${item.atk}")
                    if (item.def != 0) append(" · 방어 ${item.def}")
                    if (item.desc.isNotEmpty()) append("\n${item.desc}")
                }
            ) {
                WoodButton(
                    text = "${item.price}G",
                    enabled = vm.player.gold >= item.price,
                    highlight = vm.player.gold >= item.price
                ) { vm.buy(item) }
            }
            ThinDivider()
        }
    } else {
        val bag = vm.inventory.toList()
        if (bag.isEmpty()) {
            Text("팔 물건이 없다.", color = Palette.ParchmentDim, fontSize = 12.sp)
        } else {
            bag.forEach { e ->
                ListRow("${e.item.name} x${e.count}", e.item.type.label) {
                    WoodButton("${e.item.sellPrice}G에 팔기") { vm.sell(e) }
                }
                ThinDivider()
            }
        }
    }
}

@Composable
private fun ColumnScope.HospitalActions(vm: GameViewModel) {
    SectionTitle("오염 상처를 돌보는 집")
    val cost = vm.hospitalHealCost()
    ListRow("상처 치료", if (cost == 0) "지금은 건강하다." else "좀비의 생채기와 타박을 치료한다. 비용 ${cost}G") {
        WoodButton("치료", enabled = cost > 0 && vm.player.gold >= cost, highlight = cost > 0) {
            vm.hospitalHeal()
        }
    }
    ThinDivider()
    ListRow("해독 영양제", "최대 HP가 6 늘어난다. 좀비석 잔향에 버티는 몸을 만든다. 150G") {
        WoodButton("복용", enabled = vm.player.gold >= 150) { vm.hospitalTonic() }
    }
}

@Composable
private fun ColumnScope.ChurchActions(vm: GameViewModel) {
    SectionTitle("저주를 씻는 신전")
    ListRow("기도하기", "무료. 좀비석 기운을 밀어내며 MP를 회복한다.") {
        WoodButton("기도", highlight = true) { vm.pray() }
    }
    ThinDivider()
    ListRow("헌금하기", "100G. 3일간 축복을 받아 좀비와의 싸움에서 유리해진다.") {
        WoodButton("100G 헌금", enabled = vm.player.gold >= 100) { vm.donate(100) }
    }
    Spacer(Modifier.height(8.dp))
    if (vm.player.blessing > 0) {
        Chip("현재 축복 ${vm.player.blessing}일 남음", Palette.Moss)
    }
}

@Composable
private fun ColumnScope.InnActions(vm: GameViewModel) {
    SectionTitle("여관 · 잠든 포도송이")
    ListRow("숙박하기", "60G. HP·MP를 모두 회복하고 하루가 지난다. 문은 꼭 잠근다.") {
        WoodButton("60G 숙박", enabled = vm.player.gold >= 60, highlight = true) { vm.stayAtInn() }
    }
    ThinDivider()
    ListRow("소문 듣기", "무료. 좀비석과 영주, 지하 비극에 대한 이야기를 듣는다.") {
        WoodButton("듣기") { vm.listenRumor() }
    }
}

@Composable
private fun ColumnScope.ArenaActions(vm: GameViewModel) {
    SectionTitle("지상의 칼날 연마터")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("${vm.arenaWins}승 ${vm.arenaLosses}패", Palette.WoodLight)
        Chip("공격 ${vm.totalAtk}", Palette.Blood)
        Chip("방어 ${vm.totalDef}", Palette.Sky)
    }
    Spacer(Modifier.height(8.dp))
    ListRow("대련 신청", "지하에 들어가기 전, 비슷한 상대와 겨룬다.") {
        WoodButton("대련", highlight = true) { vm.spar() }
    }
}

@Composable
private fun ColumnScope.BlacksmithActions(vm: GameViewModel) {
    SectionTitle("좀비 이빨을 부수는 모루")
    Text(
        "착용 장비를 강화한다. 좀비 둥지에서는 무딘 칼이 곧 죽음이다.",
        color = Palette.ParchmentDim, fontSize = 12.sp
    )
    Spacer(Modifier.height(8.dp))
    EQUIP_SLOTS.forEach { slot ->
        val eq = vm.equipment[slot]
        ListRow(
            title = "[${slot.label}] ${eq?.displayName ?: "―"}",
            subtitle = if (eq == null) "착용한 장비가 없다." else
                "공격 ${eq.atk} · 방어 ${eq.def} · 강화비 ${eq.upgradeCost}G"
        ) {
            if (eq != null) {
                WoodButton(
                    text = "강화",
                    enabled = vm.player.gold >= eq.upgradeCost && eq.plus < 9,
                    highlight = true
                ) { vm.upgrade(slot) }
            }
        }
        ThinDivider()
    }
}

@Composable
private fun ColumnScope.MagicSchoolActions(vm: GameViewModel) {
    SectionTitle("해독 연금 학당")
    Text(
        "영주의 욕망이 부른 비극 이후, 학당은 좀비석 해독과 정화 연구에 매달린다.",
        color = Palette.ParchmentDim, fontSize = 12.sp
    )
    Spacer(Modifier.height(8.dp))
    ListRow("해독 고서 연구", "지능을 1 올린다. 비용 ${60 + vm.player.intel * 12}G") {
        WoodButton("연구", enabled = vm.player.gold >= 60 + vm.player.intel * 12) { vm.study() }
    }
    Spacer(Modifier.height(10.dp))
    ThinDivider()
    SectionTitle("정화 마법 수업")
    SkillCatalog.all.forEach { skill ->
        val owned = vm.skills.any { it.id == skill.id }
        ListRow(
            title = skill.name + if (owned) " (습득함)" else "",
            subtitle = "${skill.desc}\nMP ${skill.mpCost} · 수업료 ${skill.cost}G"
        ) {
            WoodButton(
                text = if (owned) "완료" else "${skill.cost}G",
                enabled = !owned && vm.player.gold >= skill.cost,
                highlight = !owned
            ) { vm.learn(skill) }
        }
        ThinDivider()
    }
}

@Composable
private fun ColumnScope.MercenaryActions(vm: GameViewModel) {
    SectionTitle("좀비 사냥 용병")
    Text("용병은 여러 명 고용할 수 있고, Status에서 최대 2명을 원정대로 선택한다. 던전 전투로 레벨업하며 Status에서 장비를 장착할 수 있다.", color = Palette.ParchmentDim, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))

    if (vm.party.isNotEmpty()) {
        SectionTitle("현재 동료")
        vm.party.toList().forEach { m ->
            val active = m.id in vm.activeMercenaryIds
            ListRow(
                "${m.name} (${m.role}) · Lv.${m.level}",
                "전투 기여 +${m.power} · EXP ${m.exp}/${m.expToNext} · ${if (active) "원정대" else "대기 중"}\n장비는 Status 메뉴에서 장착"
            ) {
                WoodButton("해고") { vm.dismiss(m) }
            }
        }
        Spacer(Modifier.height(8.dp))
        ThinDivider()
    }

    SectionTitle("고용 가능")
    MercenaryCatalog.all.forEach { m ->
        val hired = vm.party.any { it.id == m.id }
        ListRow(
            title = "${m.name} · ${m.role}",
            subtitle = "${m.desc}\n기본 전투 기여 +${m.basePower} (레벨·장비로 성장)"
        ) {
            WoodButton(
                text = if (hired) "고용함" else "${m.cost}G",
                enabled = !hired && vm.player.gold >= m.cost,
                highlight = !hired
            ) { vm.hire(m) }
        }
        ThinDivider()
    }
}
