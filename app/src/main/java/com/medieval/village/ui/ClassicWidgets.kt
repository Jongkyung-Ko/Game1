package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medieval.village.ui.skin.SkinInsets
import com.medieval.village.ui.skin.nineSliceBackground
import com.medieval.village.ui.skin.rememberUiSkin
import com.medieval.village.ui.theme.ClassicType
import com.medieval.village.ui.theme.Palette

private val CarvedShape = RoundedCornerShape(7.dp)

/**
 * 각인된 참나무 버튼. 도안에서 잘라낸 텍스처를 9분할로 늘여 쓰고,
 * 텍스처가 없으면 그라디언트 폴백으로 그린다.
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
    val skin = rememberUiSkin()
    val texture = if (arcane) skin?.buttonArcane else skin?.buttonWood
    val insets = if (arcane) SkinInsets.Arcane else SkinInsets.Button
    val ink = when {
        !enabled -> Color(0xFF8B7A5F)
        arcane -> Color(0xFFE8EFFF)
        highlight -> Color(0xFFFFE9B4)
        else -> Color(0xFFEBD8A8)
    }

    val base = modifier
        .defaultMinSize(minHeight = 42.dp)
        .clickable(enabled = enabled) { onClick() }

    val styled = if (texture != null) {
        base.alpha(if (enabled) 1f else 0.55f)
            .nineSliceBackground(texture, insets)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    } else {
        val face = when {
            !enabled -> Brush.verticalGradient(listOf(Color(0xFF33261A), Color(0xFF2A1D12)))
            arcane -> Brush.verticalGradient(listOf(Color(0xFF3D4E74), Color(0xFF23304C)))
            highlight -> Brush.verticalGradient(listOf(Color(0xFF8A6633), Color(0xFF5C401F)))
            else -> Brush.verticalGradient(listOf(Palette.WoodLight, Palette.Wood))
        }
        base.clip(CarvedShape)
            .background(face)
            .border(1.5.dp, Palette.Gold.copy(alpha = if (enabled) 0.8f else 0.2f), CarvedShape)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    }

    Box(modifier = styled, contentAlignment = Alignment.Center) {
        Text(
            text = text,
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
    val skin = rememberUiSkin()
    val texture = skin?.scroll
    val body = if (texture != null) {
        modifier
            .nineSliceBackground(texture, SkinInsets.Scroll)
            .heightIn(min = 78.dp)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 18.dp)
    } else {
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFEADCB8), Color(0xFFD8C69C))))
            .border(1.5.dp, Color(0xFF8A6B3A), RoundedCornerShape(8.dp))
            .heightIn(min = 46.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    }
    Column(modifier = body, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(heading, color = Color(0xFF5A3D18), style = ClassicType.ScrollHead, maxLines = 1)
        lines.forEach { line ->
            Text(line, color = Color(0xFF3E2C16), style = ClassicType.Scroll)
        }
    }
}

/** 문장이 새겨진 명패 — 선두 표시용 */
@Composable
fun ClassicChip(text: String, modifier: Modifier = Modifier) {
    val skin = rememberUiSkin()
    val texture = skin?.plaque
    if (texture != null) {
        Box(
            modifier = modifier
                .defaultMinSize(minHeight = 38.dp)
                .nineSliceBackground(texture, SkinInsets.Plaque)
                .padding(start = 46.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text, color = Color(0xFFEBD8A8), style = ClassicType.Label, maxLines = 1)
        }
        return
    }
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
    val skin = rememberUiSkin()
    val texture = skin?.scroll
    val body = if (texture != null) {
        modifier
            .fillMaxWidth()
            .nineSliceBackground(texture, SkinInsets.Scroll)
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xE6221A2E))
            .border(1.5.dp, Color(0xFF7E93C4), RoundedCornerShape(9.dp))
            .padding(8.dp)
    }
    Column(modifier = body, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("GRIMOIRE", color = Color(0xFF5A3D18), style = ClassicType.ScrollHead)
        entries.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = entry.title,
                    color = if (entry.enabled) Color(0xFF3E2C16) else Color(0x883E2C16),
                    style = ClassicType.Label,
                    modifier = Modifier.weight(1f),
                )
                ClassicButton(text = "Cast", enabled = entry.enabled, arcane = true) {
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
