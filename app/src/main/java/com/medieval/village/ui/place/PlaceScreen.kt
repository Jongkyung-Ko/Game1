package com.medieval.village.ui.place

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.EQUIP_SLOTS
import com.medieval.village.model.InteriorNpcCatalog
import com.medieval.village.model.InteriorRoom
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
import kotlin.math.min

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
    WalkableInteriorScreen(vm = vm, id = id, rootModifier = modifier)
}

@Composable
private fun WalkableInteriorScreen(vm: GameViewModel, id: PlaceId, rootModifier: Modifier = Modifier) {
    val modifier = Modifier
    val place = Village.of(id)
    val atlas = rememberKenneyAtlas()
    val art = rememberCustomArtOrNull()
    val npcs = remember(id) { InteriorNpcCatalog.forPlace(id) }
    val panelOpen = vm.interiorPanelOpen

    Column(modifier = rootModifier.fillMaxSize().background(Palette.WoodDark)) {
        Text(
            text = "${place.name} · ${place.subtitle}",
            color = Palette.Gold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(if (panelOpen) 0.38f else 1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color(0xFF21150E), RoundedCornerShape(12.dp))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val scale = min(widthPx / InteriorRoom.WORLD_W, heightPx / InteriorRoom.WORLD_H)
            val offsetX = (widthPx - InteriorRoom.WORLD_W * scale) / 2f
            val offsetY = (heightPx - InteriorRoom.WORLD_H * scale) / 2f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(id, scale, offsetX, offsetY, panelOpen) {
                        detectTapGestures { tap ->
                            if (vm.interiorPanelOpen) return@detectTapGestures
                            val x = (tap.x - offsetX) / scale
                            val y = (tap.y - offsetY) / scale
                            val npc = npcs.minByOrNull {
                                hypot(x - it.worldX, y - it.worldY)
                            }?.takeIf { hypot(x - it.worldX, y - it.worldY) < 100f }
                            if (npc != null) vm.approachInteriorNpc(npc) else vm.walkInInterior(x, y)
                        }
                    }
            ) {
                withTransform({
                    translate(offsetX, offsetY)
                    scale(scale, scale, Offset.Zero)
                }) {
                    drawWalkableInterior(
                        atlas = atlas,
                        art = art,
                        id = id,
                        heroX = vm.pubHeroX,
                        heroY = vm.pubHeroY,
                        facing = vm.facing,
                        walking = vm.pubWalking,
                        walkPhase = vm.walkPhase,
                        companions = vm.activeParty,
                        animTime = vm.animTime,
                        speechNpcId = vm.interiorSpeakerId,
                        speechText = vm.interiorSpeech,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .background(Color(0xAA1B120A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    if (panelOpen) "거래 창이 열려 있다"
                    else "탭으로 이동 · 손님과 대화 · 주인에게 다가가면 구매 메뉴",
                    color = Palette.Parchment,
                    fontSize = 11.sp
                )
            }
        }

        if (panelOpen) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .background(Palette.Wood, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(panelTitle(id), Modifier.weight(1f))
                    Chip("${vm.player.gold}G", Palette.WoodLight)
                    WoodButton("닫기") { vm.closeInteriorPanel() }
                }
                ThinDivider()
                Spacer(modifier.height(4.dp))
                when (id) {
                    PlaceId.SHOP -> ShopSplitPanel(vm, ItemCatalog.generalGoods, "잡화 진열대")
                    PlaceId.WEAPON_SHOP -> ShopSplitPanel(vm, ItemCatalog.weaponGoods, "무기와 방어구")
                    PlaceId.MAGIC_SCHOOL -> MagicSplitPanel(vm)
                    else -> {
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            PlaceServiceBody(vm, id)
                        }
                        Spacer(modifier.height(6.dp))
                        InventoryBottomPanel(vm, sellable = false)
                    }
                }
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    "여행객·손님을 탭하면 대화, 주인을 탭하면 거래. 아래 버튼으로도 메뉴를 연다.",
                    color = Palette.ParchmentDim,
                    fontSize = 11.sp
                )
                Spacer(modifier.height(5.dp))
                MessageLog(vm.log, Modifier.height(72.dp))
                Spacer(modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WoodButton("메뉴 열기", Modifier.weight(1f), highlight = true) {
                        vm.openInteriorPanel()
                    }
                    WoodButton("마을로 나가기", Modifier.weight(1f)) { vm.leavePlace() }
                }
            }
        }
    }
}

private fun panelTitle(id: PlaceId): String = when (id) {
    PlaceId.SHOP -> "잡화 상점"
    PlaceId.WEAPON_SHOP -> "무기점"
    PlaceId.MAGIC_SCHOOL -> "마법 수업"
    PlaceId.HOME -> "오두막"
    PlaceId.HOSPITAL -> "치료"
    PlaceId.CHURCH -> "신전"
    PlaceId.INN -> "여관"
    PlaceId.ARENA -> "대련소"
    PlaceId.BLACKSMITH -> "대장간"
    PlaceId.MERCENARY -> "용병고용소"
    else -> "서비스"
}

/** 상단: 구매 목록 / 하단: 내 가방 */
@Composable
private fun ColumnScope.ShopSplitPanel(vm: GameViewModel, goods: List<Item>, title: String) {
    val modifier = Modifier
    Column(modifier = Modifier.weight(1f)) {
        Text(title, color = Palette.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("구매할 물건을 고르세요", color = Palette.ParchmentDim, fontSize = 11.sp)
        Spacer(modifier.height(4.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            goods.forEach { item ->
                ListRow(
                    title = item.name,
                    subtitle = buildString {
                        append(item.type.label)
                        if (item.atk != 0) append(" · 공격 ${item.atk}")
                        if (item.def != 0) append(" · 방어 ${item.def}")
                        if (item.desc.isNotEmpty()) append("\n${item.desc}")
                    },
                    trailing = {
                        WoodButton(
                            text = "${item.price}G",
                            enabled = vm.player.gold >= item.price,
                            highlight = vm.player.gold >= item.price
                        ) { vm.buy(item) }
                    }
                )
                ThinDivider()
            }
        }
    }
    Spacer(modifier.height(6.dp))
    ThinDivider()
    Spacer(modifier.height(4.dp))
    InventoryBottomPanel(vm, sellable = true)
}

@Composable
private fun ColumnScope.MagicSplitPanel(vm: GameViewModel) {
    val modifier = Modifier
    Column(modifier = Modifier.weight(1f)) {
        Text("정화 마법 · 연구", color = Palette.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier.height(4.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            val studyCost = 60 + vm.player.intel * 12
            ListRow("해독 고서 연구", "지능 +1 · ${studyCost}G", trailing = {
                WoodButton("연구", enabled = vm.player.gold >= studyCost) { vm.study() }
            })
            ThinDivider()
            SkillCatalog.all.forEach { skill ->
                val owned = vm.skills.any { it.id == skill.id }
                ListRow(
                    title = skill.name + if (owned) " (습득함)" else "",
                    subtitle = "${skill.desc}\nMP ${skill.mpCost} · 수업료 ${skill.cost}G",
                    trailing = {
                        WoodButton(
                            text = if (owned) "완료" else "${skill.cost}G",
                            enabled = !owned && vm.player.gold >= skill.cost,
                            highlight = !owned
                        ) { vm.learn(skill) }
                    }
                )
                ThinDivider()
            }
        }
    }
    Spacer(modifier.height(6.dp))
    ThinDivider()
    Spacer(modifier.height(4.dp))
    InventoryBottomPanel(vm, sellable = false)
}

@Composable
private fun ColumnScope.InventoryBottomPanel(vm: GameViewModel, sellable: Boolean) {
    val modifier = Modifier
    Text("내 가방", color = Palette.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Text(
        if (sellable) "아래에서 아이템을 팔 수 있다" else "소지 중인 아이템",
        color = Palette.ParchmentDim,
        fontSize = 11.sp
    )
    Spacer(modifier.height(4.dp))
    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
        val bag = vm.inventory.toList()
        if (bag.isEmpty()) {
            Text("가방이 비어 있다.", color = Palette.ParchmentDim, fontSize = 12.sp)
        } else {
            bag.forEach { e ->
                ListRow(
                    title = "${e.item.name} x${e.count}",
                    subtitle = buildString {
                        append(e.item.type.label)
                        if (e.item.atk != 0) append(" · 공격 ${e.item.atk}")
                        if (e.item.def != 0) append(" · 방어 ${e.item.def}")
                    },
                    trailing = {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            if (e.item.healHp > 0 || e.item.healMp > 0) {
                                WoodButton("사용") { vm.useItem(e.item) }
                            }
                            if (sellable) {
                                WoodButton("${e.item.sellPrice}G") { vm.sell(e) }
                            }
                        }
                    }
                )
                ThinDivider()
            }
        }
    }
}

@Composable
private fun ColumnScope.PlaceServiceBody(vm: GameViewModel, id: PlaceId) {
    val modifier = Modifier
    when (id) {
        PlaceId.HOME -> HomeActions(vm)
        PlaceId.HOSPITAL -> HospitalActions(vm)
        PlaceId.CHURCH -> ChurchActions(vm)
        PlaceId.INN -> InnActions(vm)
        PlaceId.ARENA -> ArenaActions(vm)
        PlaceId.BLACKSMITH -> BlacksmithActions(vm)
        PlaceId.MERCENARY -> MercenaryActions(vm)
        else -> Text("이용할 서비스가 없다.", color = Palette.ParchmentDim, fontSize = 12.sp)
    }
}

// ---------------------------------------------------------------- 각 장소 서비스 본문

@Composable
private fun ColumnScope.HomeActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("풍요의 마을 오두막")
    Text(
        "신성한 포도주의 향기가 희미한 작은 집. 저주가 스며든 밤에도 여기서 하루를 마무리할 수 있다.",
        color = Palette.ParchmentDim, fontSize = 12.sp
    )
    Spacer(modifier.height(8.dp))
    WoodButton("잠자기 (무료 · 완전 회복)", highlight = true) { vm.sleepAtHome() }
}

@Composable
private fun ColumnScope.HospitalActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("오염 상처를 돌보는 집")
    val cost = vm.hospitalHealCost()
    ListRow(
        "상처 치료",
        if (cost == 0) "지금은 건강하다." else "좀비의 생채기와 타박을 치료한다. 비용 ${cost}G",
        trailing = {
            WoodButton("치료", enabled = cost > 0 && vm.player.gold >= cost, highlight = cost > 0) {
                vm.hospitalHeal()
            }
        }
    )
    ThinDivider()
    ListRow(
        "해독 영양제",
        "최대 HP가 6 늘어난다. 좀비석 잔향에 버티는 몸을 만든다. 150G",
        trailing = {
            WoodButton("복용", enabled = vm.player.gold >= 150) { vm.hospitalTonic() }
        }
    )
}

@Composable
private fun ColumnScope.ChurchActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("저주를 씻는 신전")
    ListRow("기도하기", "무료. 좀비석 기운을 밀어내며 MP를 회복한다.", trailing = {
        WoodButton("기도", highlight = true) { vm.pray() }
    })
    ThinDivider()
    ListRow("헌금하기", "100G. 3일간 축복을 받아 좀비와의 싸움에서 유리해진다.", trailing = {
        WoodButton("100G 헌금", enabled = vm.player.gold >= 100) { vm.donate(100) }
    })
    Spacer(modifier.height(8.dp))
    if (vm.player.blessing > 0) {
        Chip("현재 축복 ${vm.player.blessing}일 남음", Palette.Moss)
    }
}

@Composable
private fun ColumnScope.InnActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("여관 · 잠든 포도송이")
    ListRow("숙박하기", "60G. HP·MP를 모두 회복하고 하루가 지난다.", trailing = {
        WoodButton("60G 숙박", enabled = vm.player.gold >= 60, highlight = true) { vm.stayAtInn() }
    })
    ThinDivider()
    ListRow("소문 듣기", "무료. 좀비석과 영주, 지하 비극에 대한 이야기를 듣는다.", trailing = {
        WoodButton("듣기") { vm.listenRumor() }
    })
}

@Composable
private fun ColumnScope.ArenaActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("지상의 칼날 연마터")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("${vm.arenaWins}승 ${vm.arenaLosses}패", Palette.WoodLight)
        Chip("공격 ${vm.totalAtk}", Palette.Blood)
        Chip("방어 ${vm.totalDef}", Palette.Sky)
    }
    Spacer(modifier.height(8.dp))
    ListRow("대련 신청", "지하에 들어가기 전, 비슷한 상대와 겨룬다.", trailing = {
        WoodButton("대련", highlight = true) { vm.spar() }
    })
}

@Composable
private fun ColumnScope.BlacksmithActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("좀비 이빨을 부수는 모루")
    Text(
        "착용 장비를 강화한다. 좀비 둥지에서는 무딘 칼이 곧 죽음이다.",
        color = Palette.ParchmentDim, fontSize = 12.sp
    )
    Spacer(modifier.height(8.dp))
    EQUIP_SLOTS.forEach { slot ->
        val eq = vm.equipment[slot]
        ListRow(
            title = "[${slot.label}] ${eq?.displayName ?: "―"}",
            subtitle = if (eq == null) "착용한 장비가 없다." else
                "공격 ${eq.atk} · 방어 ${eq.def} · 강화비 ${eq.upgradeCost}G",
            trailing = {
                if (eq != null) {
                    WoodButton(
                        text = "강화",
                        enabled = vm.player.gold >= eq.upgradeCost && eq.plus < 9,
                        highlight = true
                    ) { vm.upgrade(slot) }
                }
            }
        )
        ThinDivider()
    }
}

@Composable
private fun ColumnScope.MercenaryActions(vm: GameViewModel) {
    val modifier = Modifier
    SectionTitle("좀비 사냥 용병")
    Text(
        "용병은 Status에서 최대 2명을 원정대로 선택한다. 장비·레벨업도 Status에서.",
        color = Palette.ParchmentDim,
        fontSize = 12.sp
    )
    Spacer(modifier.height(8.dp))
    if (vm.party.isNotEmpty()) {
        SectionTitle("현재 동료")
        vm.party.toList().forEach { m ->
            val active = m.id in vm.activeMercenaryIds
            ListRow(
                "${m.name} (${m.role}) · Lv.${m.level}",
                "기여 +${m.power} · ${if (active) "원정대" else "대기"}",
                trailing = { WoodButton("해고") { vm.dismiss(m) } }
            )
        }
        Spacer(modifier.height(8.dp))
        ThinDivider()
    }
    SectionTitle("고용 가능")
    MercenaryCatalog.all.forEach { m ->
        val hired = vm.party.any { it.id == m.id }
        ListRow(
            title = "${m.name} · ${m.role}",
            subtitle = "${m.desc}\n기본 기여 +${m.basePower}",
            trailing = {
                WoodButton(
                    text = if (hired) "고용함" else "${m.cost}G",
                    enabled = !hired && vm.player.gold >= m.cost,
                    highlight = !hired
                ) { vm.hire(m) }
            }
        )
        ThinDivider()
    }
}
