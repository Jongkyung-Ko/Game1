package com.medieval.village.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.model.SpecialSkillCatalog
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.rememberCustomArtOrNull

/** 스킬 아이콘 — circular=true 면 슬롯용 원형 크롭. */
@Composable
fun SkillIcon(
    skillId: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    art: CustomArt? = rememberCustomArtOrNull(),
    enabled: Boolean = true,
    showBorder: Boolean = true,
    circular: Boolean = false,
) {
    val bmp = skillId?.let { art?.skillIconOrNull(it) }
    val shape = if (circular) CircleShape else RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                if (skillId != null) Color(0xFF2A1C12) else Color(0x442A1C12),
                shape,
            )
            .then(
                if (showBorder) {
                    Modifier.border(
                        if (circular) 2.dp else 1.dp,
                        if (enabled && skillId != null) Palette.Gold else Color(0xFF5A4030),
                        shape,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = skillId,
                contentScale = if (circular) ContentScale.Crop else ContentScale.Fit,
                filterQuality = FilterQuality.Medium,
                alpha = if (enabled) 1f else 0.45f,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
            )
        } else {
            val short = skillId?.let { SpecialSkillCatalog.byId(it)?.shortName } ?: "—"
            Text(
                short,
                color = if (enabled) Palette.Parchment else Color(0x66C8B8A0),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
