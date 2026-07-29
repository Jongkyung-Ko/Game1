package com.medieval.village.ui.place

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.EQUIP_SLOTS
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

@Composable
fun PlaceScreen(vm: GameViewModel, id: PlaceId, modifier: Modifier = Modifier) {
    val place = Village.of(id)

    Column(modifier = modifier.fillMaxSize().background(Palette.WoodDark)) {

        // 배경 일러스트
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawInterior(id, size.width, size.height)
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
                    Text(place.subtitle, color = Palette.ParchmentDim, fontSize = 10.sp)
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
                PlaceId.ARENA -> ArenaActions(vm)
                PlaceId.DUNGEON -> DungeonActions(vm)
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
    SectionTitle("나의 오두막")
    Text(
        "침대와 벽난로뿐인 작은 집. 여기서 하루를 마무리할 수 있다.",
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
    SectionTitle("치유의 집")
    val cost = vm.hospitalHealCost()
    ListRow("상처 치료", if (cost == 0) "지금은 건강하다." else "HP를 모두 회복한다. 비용 ${cost}G") {
        WoodButton("치료", enabled = cost > 0 && vm.player.gold >= cost, highlight = cost > 0) {
            vm.hospitalHeal()
        }
    }
    ThinDivider()
    ListRow("영양제 처방", "최대 HP가 6 늘어난다. 비용 150G") {
        WoodButton("복용", enabled = vm.player.gold >= 150) { vm.hospitalTonic() }
    }
}

@Composable
private fun ColumnScope.ChurchActions(vm: GameViewModel) {
    SectionTitle("빛의 신전")
    ListRow("기도하기", "무료. MP를 회복하고 드물게 행운이 오른다.") {
        WoodButton("기도", highlight = true) { vm.pray() }
    }
    ThinDivider()
    ListRow("헌금하기", "100G. 3일간 축복을 받아 전투에서 유리해진다.") {
        WoodButton("100G 헌금", enabled = vm.player.gold >= 100) { vm.donate(100) }
    }
    Spacer(Modifier.height(8.dp))
    if (vm.player.blessing > 0) {
        Chip("현재 축복 ${vm.player.blessing}일 남음", Palette.Moss)
    }
}

@Composable
private fun ColumnScope.InnActions(vm: GameViewModel) {
    SectionTitle("여관 · 잠든 곰")
    ListRow("숙박하기", "60G. HP·MP를 모두 회복하고 하루가 지난다.") {
        WoodButton("60G 숙박", enabled = vm.player.gold >= 60, highlight = true) { vm.stayAtInn() }
    }
    ThinDivider()
    ListRow("소문 듣기", "무료. 마을 사람들의 이야기를 들어본다.") {
        WoodButton("듣기") { vm.listenRumor() }
    }
}

@Composable
private fun ColumnScope.ArenaActions(vm: GameViewModel) {
    SectionTitle("무인들의 터")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("${vm.arenaWins}승 ${vm.arenaLosses}패", Palette.WoodLight)
        Chip("공격 ${vm.totalAtk}", Palette.Blood)
        Chip("방어 ${vm.totalDef}", Palette.Sky)
    }
    Spacer(Modifier.height(8.dp))
    ListRow("대련 신청", "실력이 비슷한 상대와 겨룬다. 이기면 경험치와 상금.") {
        WoodButton("대련", highlight = true) { vm.spar() }
    }
}

@Composable
private fun ColumnScope.DungeonActions(vm: GameViewModel) {
    SectionTitle("잊혀진 지하")
    Text(
        "축축한 계단이 어둠 속으로 이어진다. 깊이 내려갈수록 위험하지만 보상도 커진다.",
        color = Palette.ParchmentDim, fontSize = 12.sp
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("최고 기록 ${vm.player.dungeonDepth}층", Palette.WoodLight)
        Chip("동료 ${vm.party.size}명", Palette.Moss)
    }
    Spacer(Modifier.height(10.dp))
    ListRow(
        "지하 ${vm.player.dungeonDepth + 1}층 탐험",
        "몬스터와 싸우고 전리품을 얻는다."
    ) {
        WoodButton("내려가기", highlight = true) { vm.exploreDungeon() }
    }
    ThinDivider()
    val potion = vm.inventory.toList().firstOrNull { it.item.healHp > 0 }
    if (potion != null) {
        ListRow("${potion.item.name} x${potion.count}", "지금 마셔 체력을 회복한다.") {
            WoodButton("마시기") { vm.useItem(potion.item) }
        }
    }
}

@Composable
private fun ColumnScope.BlacksmithActions(vm: GameViewModel) {
    SectionTitle("불꽃의 모루")
    Text(
        "착용 중인 장비를 강화한다. 단계가 오를수록 비용이 늘고 실패할 수도 있다.",
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
    SectionTitle("아르카나 학당")
    ListRow("고서 연구", "지능을 1 올린다. 비용 ${60 + vm.player.intel * 12}G") {
        WoodButton("연구", enabled = vm.player.gold >= 60 + vm.player.intel * 12) { vm.study() }
    }
    Spacer(Modifier.height(10.dp))
    ThinDivider()
    SectionTitle("마법 수업")
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
    SectionTitle("떠돌이 칼잡이")
    Text("최대 2명까지 동행할 수 있다. 동료는 던전 전투에 힘을 보탠다.", color = Palette.ParchmentDim, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))

    if (vm.party.isNotEmpty()) {
        SectionTitle("현재 동료")
        vm.party.toList().forEach { m ->
            ListRow("${m.name} (${m.role})", "전투 기여 +${m.power}") {
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
            subtitle = "${m.desc}\n전투 기여 +${m.power}"
        ) {
            WoodButton(
                text = if (hired) "동행 중" else "${m.cost}G",
                enabled = !hired && vm.player.gold >= m.cost && vm.party.size < 2,
                highlight = !hired
            ) { vm.hire(m) }
        }
        ThinDivider()
    }
}
