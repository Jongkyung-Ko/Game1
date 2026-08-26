package com.medieval.village.ui.place

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medieval.village.game.GameViewModel
import com.medieval.village.model.ItemCatalog
import com.medieval.village.ui.ClassicButton
import com.medieval.village.ui.ClassicChip
import com.medieval.village.ui.SpellChoiceRow
import com.medieval.village.ui.SpellEntry

/**
 * 선두 표시 · 교대 · 상태 두루마리 한 줄.
 */
@Composable
fun DungeonPartyRow(vm: GameViewModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ClassicChip("Front · ${vm.frontActorName()}")
            ClassicButton(
                text = "Rotate",
                enabled = vm.partyActorCount > 1,
                highlight = vm.partyActorCount > 1,
            ) {
                vm.cyclePartyFront()
            }
        }
        DungeonStatusScroll(vm, Modifier.weight(1f))
    }
}

/**
 * 하단 행동열 — Escape / Potion / Magic (고전 각인 버튼).
 */
@Composable
fun DungeonActionRow(vm: GameViewModel, modifier: Modifier = Modifier) {
    var grimoireOpen by remember { mutableStateOf(false) }
    val spells = vm.learnedSpellEntries()
    val portalStone = vm.inventory.toList().firstOrNull {
        it.item.id == ItemCatalog.portalStone.id && it.count > 0
    }
    val potion = vm.inventory.toList().firstOrNull { it.item.healHp > 0 }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (grimoireOpen && spells.isNotEmpty()) {
            SpellChoiceRow(
                entries = spells.map { spell ->
                    SpellEntry(
                        id = spell.id,
                        title = "${spell.englishName} · ${spell.name} (MP ${spell.mpCost})",
                        enabled = spell.castable,
                    )
                },
                onCast = { id ->
                    vm.castLearnedSpell(id)
                    grimoireOpen = false
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (vm.dungeonHint) {
                "stairs_up" -> ClassicButton("Escape", Modifier.weight(1f), highlight = true) {
                    vm.escapeDungeon()
                }
                "stairs_down" -> ClassicButton("Descend", Modifier.weight(1f), highlight = true) {
                    vm.descendDungeon()
                }
                "portal" -> ClassicButton("Homeward", Modifier.weight(1f), highlight = true) {
                    vm.enterHomePortal()
                }
                "chest" -> ClassicButton("Open", Modifier.weight(1f), highlight = true) {
                    vm.openDungeonChest()
                }
                else -> ClassicButton("Escape", Modifier.weight(1f), enabled = false) {}
            }
            if (portalStone != null) {
                ClassicButton("Waystone", Modifier.weight(1f), highlight = true) {
                    vm.useItem(ItemCatalog.portalStone)
                }
            }
            ClassicButton(
                text = "Potion",
                modifier = Modifier.weight(1f),
                enabled = potion != null,
            ) {
                potion?.let { vm.useItem(it.item) }
            }
            ClassicButton(
                text = "Magic",
                modifier = Modifier.weight(1f),
                enabled = spells.isNotEmpty(),
                arcane = true,
            ) {
                grimoireOpen = !grimoireOpen
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

/** 하단 크롬 전체 — 파티 줄 + 전투 컨트롤 + 행동열 */
@Composable
fun DungeonBottomChrome(
    vm: GameViewModel,
    logContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DungeonPartyRow(vm)
        DungeonCombatControls(
            attackLabel = vm.attackLabel(),
            attackEnabled = vm.attackReady && vm.dungeonFloor != null && vm.levelUpSkillOffer == null,
            onPad = { dx, dy -> vm.setDungeonPad(dx, dy) },
            onPadRelease = { vm.clearDungeonPad() },
            onAttack = { vm.dungeonAttack() },
            logContent = logContent,
            skillSlots = vm.frontSkillSlotsUi(),
            onSpecial = { slot -> vm.dungeonSpecialAttack(slot) },
        )
        DungeonActionRow(vm)
    }
}
