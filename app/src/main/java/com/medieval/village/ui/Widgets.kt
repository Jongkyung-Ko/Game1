package com.medieval.village.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.ui.skin.SkinInsets
import com.medieval.village.ui.skin.nineSliceBackground
import com.medieval.village.ui.skin.rememberUiSkin
import com.medieval.village.ui.theme.Palette

@Composable
fun ParchmentPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(Palette.Wood, RoundedCornerShape(12.dp))
            .border(2.dp, Palette.Gold.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Palette.Gold,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun WoodButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(9.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (highlight) Palette.Gold else Palette.WoodLight,
            contentColor = if (highlight) Palette.Ink else Palette.Parchment,
            disabledContainerColor = Palette.WoodDark,
            disabledContentColor = Palette.ParchmentDim.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** 행동 결과가 쌓이는 대사창 */
@Composable
fun MessageLog(lines: List<String>, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    LaunchedEffect(lines.size) { scroll.animateScrollTo(scroll.maxValue) }
    val parchment = rememberUiSkin()?.logScroll
    val body = if (parchment != null) {
        modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp, max = 140.dp)
            .nineSliceBackground(parchment, SkinInsets.LogScroll)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
            .verticalScroll(scroll)
    } else {
        modifier
            .fillMaxWidth()
            .heightIn(min = 74.dp, max = 132.dp)
            .background(Palette.Ink, RoundedCornerShape(10.dp))
            .border(1.5.dp, Palette.WoodLight, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .verticalScroll(scroll)
    }
    val ink = if (parchment != null) Color(0xFF3E2C16) else Palette.Parchment
    Column(modifier = body, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.forEach { line ->
            Text(line, color = ink, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

/** 좌: (선택)아이콘 + 이름/설명, 우: 버튼 형태의 목록 한 줄 */
@Composable
fun ListRow(
    title: String,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(modifier = Modifier.padding(end = 8.dp)) { leading() }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Palette.Parchment, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, color = Palette.ParchmentDim, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun ThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Palette.Gold.copy(alpha = 0.25f))
    )
}

@Composable
fun Chip(text: String, color: Color = Palette.WoodDark) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .border(1.dp, Palette.Gold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = Palette.Parchment, fontSize = 11.sp)
    }
}
