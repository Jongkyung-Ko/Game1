package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medieval.village.ui.theme.ClassicType
import com.medieval.village.ui.theme.Palette

private val CarvedShape = RoundedCornerShape(7.dp)

/**
 * 각인된 참나무 버튼 — 고전 영문 라벨용.
 */
@Composable
fun ClassicButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlight: Boolean = false,
    arcane: Boolean = false,
    onClick: () -> Unit,
) {
    val face = when {
        !enabled -> Brush.verticalGradient(listOf(Color(0xFF33261A), Color(0xFF2A1D12)))
        arcane -> Brush.verticalGradient(listOf(Color(0xFF3D4E74), Color(0xFF23304C)))
        highlight -> Brush.verticalGradient(listOf(Color(0xFF8A6633), Color(0xFF5C401F)))
        else -> Brush.verticalGradient(listOf(Palette.WoodLight, Palette.Wood))
    }
    val edge = when {
        !enabled -> Palette.Gold.copy(alpha = 0.2f)
        arcane -> Color(0xFF9FC4E8)
        else -> Palette.Gold.copy(alpha = 0.8f)
    }
    val ink = when {
        !enabled -> Palette.ParchmentDim.copy(alpha = 0.45f)
        arcane -> Color(0xFFE4EEFF)
        else -> Palette.Parchment
    }
    Box(
        modifier = modifier
            .clip(CarvedShape)
            .background(face)
            .border(1.5.dp, edge, CarvedShape)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = ink,
            style = ClassicType.Button,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 양피지 두루마리 — 작은 고전 영문 안내문.
 */
@Composable
fun ClassicScroll(
    heading: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFFEADCB8), Color(0xFFD8C69C)))
            )
            .border(1.5.dp, Color(0xFF8A6B3A), RoundedCornerShape(8.dp))
            .heightIn(min = 46.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = heading,
            color = Color(0xFF5A3D18),
            style = ClassicType.ScrollHead,
            maxLines = 1,
        )
        lines.forEach { line ->
            Text(
                text = line,
                color = Color(0xFF3E2C16),
                style = ClassicType.Scroll,
            )
        }
    }
}

/** 고전 라벨 칩 */
@Composable
fun ClassicChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC1B120A))
            .border(1.dp, Palette.Gold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(text, color = Palette.Gold, style = ClassicType.Label, maxLines = 1)
    }
}

/** 배운 마법 선택 목록 — 하단 Magic 버튼에서 펼친다. */
@Composable
fun SpellChoiceRow(
    entries: List<SpellEntry>,
    modifier: Modifier = Modifier,
    onCast: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xE6221A2E))
            .border(1.5.dp, Color(0xFF7E93C4), RoundedCornerShape(9.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("GRIMOIRE", color = Color(0xFFCFDCF6), style = ClassicType.ScrollHead)
        entries.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = entry.title,
                    color = if (entry.enabled) Color(0xFFEAF0FF) else Color(0x88C8C8D8),
                    style = ClassicType.Label,
                    modifier = Modifier.weight(1f),
                )
                ClassicButton(
                    text = "Cast",
                    enabled = entry.enabled,
                    arcane = true,
                ) {
                    onCast(entry.id)
                }
            }
        }
    }
}

data class SpellEntry(
    val id: String,
    val title: String,
    val enabled: Boolean,
)
