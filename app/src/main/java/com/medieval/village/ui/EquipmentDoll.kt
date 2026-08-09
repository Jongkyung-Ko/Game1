package com.medieval.village.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.model.EquippedItem
import com.medieval.village.model.ItemType
import com.medieval.village.ui.theme.Palette

/**
 * 사람 윤곽 + 부위별 장비 슬롯 상자.
 * 헬멧은 머리 옆, 무기/방패는 손 옆, 갑옷은 몸 옆, 장신구는 허리 아래.
 */
@Composable
fun EquipmentDoll(
    equipment: Map<ItemType, EquippedItem>,
    onSlotClick: (ItemType) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFF1B120A), RoundedCornerShape(10.dp))
            .border(1.5.dp, Palette.Gold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        // silhouette
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawHumanSilhouette()
        }

        // HELMET — head right
        EquipSlotBox(
            label = "투구",
            item = equipment[ItemType.HELMET],
            onClick = { onSlotClick(ItemType.HELMET) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 72.dp, y = 4.dp)
        )

        // WEAPON — left hand
        EquipSlotBox(
            label = "무기",
            item = equipment[ItemType.WEAPON],
            onClick = { onSlotClick(ItemType.WEAPON) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 4.dp, y = 18.dp)
        )

        // SHIELD — right hand
        EquipSlotBox(
            label = "방패",
            item = equipment[ItemType.SHIELD],
            onClick = { onSlotClick(ItemType.SHIELD) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-4).dp, y = 18.dp)
        )

        // ARMOR — body right-ish under helmet
        EquipSlotBox(
            label = "갑옷",
            item = equipment[ItemType.ARMOR],
            onClick = { onSlotClick(ItemType.ARMOR) },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 78.dp, y = (-8).dp)
        )

        // ACCESSORY — waist below
        EquipSlotBox(
            label = "장신구",
            item = equipment[ItemType.ACCESSORY],
            onClick = { onSlotClick(ItemType.ACCESSORY) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-4).dp)
        )
    }
}

@Composable
private fun EquipSlotBox(
    label: String,
    item: EquippedItem?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(52.dp)
            .clickable(enabled = item != null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF2A1A10), RoundedCornerShape(6.dp))
                .border(2.dp, Palette.Gold.copy(alpha = 0.7f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            ItemIcon(item = item?.item, size = 40.dp, framed = false)
        }
        Text(
            text = item?.displayName?.take(6) ?: label,
            color = if (item != null) Palette.Parchment else Palette.ParchmentDim,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun DrawScope.drawHumanSilhouette() {
    val w = size.width
    val h = size.height
    val cx = w * 0.42f
    val stroke = Color(0xFFD9C8A4)
    val fill = Color(0x332A1A10)
    val sw = 3.2f

    // head
    val headR = h * 0.085f
    val headCy = h * 0.18f
    drawCircle(fill, headR, Offset(cx, headCy))
    drawCircle(stroke, headR, Offset(cx, headCy), style = Stroke(sw))

    // neck
    drawLine(stroke, Offset(cx, headCy + headR), Offset(cx, h * 0.30f), sw, StrokeCap.Round)

    // torso outline
    val torsoTop = h * 0.30f
    val torsoBot = h * 0.58f
    val shoulder = w * 0.11f
    val hip = w * 0.08f
    val torso = Path().apply {
        moveTo(cx - shoulder, torsoTop)
        lineTo(cx + shoulder, torsoTop)
        lineTo(cx + hip, torsoBot)
        lineTo(cx - hip, torsoBot)
        close()
    }
    drawPath(torso, fill)
    drawPath(torso, stroke, style = Stroke(sw))

    // arms
    drawLine(stroke, Offset(cx - shoulder, torsoTop + 4f), Offset(cx - w * 0.22f, h * 0.52f), sw, StrokeCap.Round)
    drawLine(stroke, Offset(cx + shoulder, torsoTop + 4f), Offset(cx + w * 0.22f, h * 0.52f), sw, StrokeCap.Round)
    // hands dots
    drawCircle(stroke, 4f, Offset(cx - w * 0.22f, h * 0.52f))
    drawCircle(stroke, 4f, Offset(cx + w * 0.22f, h * 0.52f))

    // legs
    drawLine(stroke, Offset(cx - hip * 0.5f, torsoBot), Offset(cx - w * 0.10f, h * 0.92f), sw, StrokeCap.Round)
    drawLine(stroke, Offset(cx + hip * 0.5f, torsoBot), Offset(cx + w * 0.10f, h * 0.92f), sw, StrokeCap.Round)

    // faint body guide box near torso (visual anchor for armor)
    drawRoundRect(
        Color(0x22E2B866),
        Offset(cx - shoulder * 0.7f, torsoTop + h * 0.04f),
        Size(shoulder * 1.4f, h * 0.18f),
        androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(1.2f)
    )
}
