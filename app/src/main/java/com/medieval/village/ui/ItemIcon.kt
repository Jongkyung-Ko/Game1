package com.medieval.village.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.medieval.village.model.Item
import com.medieval.village.model.ItemType
import com.medieval.village.ui.skin.rememberItemArt
import com.medieval.village.ui.theme.Palette
import kotlin.math.roundToInt

/** 인벤토리·상점 목록용 아이템 아이콘. */
@Composable
fun ItemIcon(
    item: Item?,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    framed: Boolean = true,
) {
    val art = rememberItemArt()
    val painted = art?.iconOrNull(item?.id)
    if (art != null && (painted != null || item == null)) {
        PaintedItemIcon(
            icon = painted,
            slot = art.emptySlot.takeIf { framed },
            modifier = modifier,
            size = size,
        )
        return
    }

    val bg = when (item?.type) {
        ItemType.WEAPON -> Color(0xFF3A2418)
        ItemType.SHIELD -> Color(0xFF2A3040)
        ItemType.ARMOR -> Color(0xFF2A3228)
        ItemType.HELMET -> Color(0xFF32282A)
        ItemType.ACCESSORY -> Color(0xFF32284A)
        ItemType.CONSUMABLE -> Color(0xFF2A2830)
        null -> Color(0xFF221810)
    }
    val base = modifier
        .size(size)
        .background(bg, RoundedCornerShape(6.dp))
    Canvas(
        modifier = if (framed) {
            base.border(1.dp, Palette.Gold.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
        } else {
            base
        }
    ) {
        if (item == null) {
            drawEmptySlotMark()
        } else {
            drawItemGlyph(item.id, item.type)
        }
    }
}

/** 도안 슬롯 위에 아이템 그림을 비율 그대로 얹는다. */
@Composable
private fun PaintedItemIcon(
    icon: ImageBitmap?,
    slot: ImageBitmap?,
    modifier: Modifier,
    size: Dp,
) {
    Canvas(modifier = modifier.size(size)) {
        slot?.let {
            drawImage(
                image = it,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(it.width, it.height),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt()),
                filterQuality = FilterQuality.Medium,
            )
        }
        if (icon == null) {
            if (slot == null) drawEmptySlotMark()
            return@Canvas
        }
        val budget = this.size.minDimension * (if (slot != null) 0.74f else 0.94f)
        val scale = minOf(budget / icon.width, budget / icon.height)
        val w = (icon.width * scale).roundToInt().coerceAtLeast(1)
        val h = (icon.height * scale).roundToInt().coerceAtLeast(1)
        drawImage(
            image = icon,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(icon.width, icon.height),
            dstOffset = IntOffset(
                ((this.size.width - w) / 2f).roundToInt(),
                ((this.size.height - h) / 2f).roundToInt(),
            ),
            dstSize = IntSize(w, h),
            filterQuality = FilterQuality.Medium,
        )
    }
}

private fun DrawScope.drawEmptySlotMark() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    drawCircle(Color(0x44E2B866), 3f, Offset(cx, cy))
}

private fun DrawScope.drawItemGlyph(id: String, type: ItemType) {
    when (id) {
        "potion" -> drawPotion(Color(0xFFE85D4C))
        "hi_potion" -> drawPotion(Color(0xFFFF8A65))
        "ether" -> drawPotion(Color(0xFF5B8DEF))
        "bread" -> drawBread()
        "torch" -> drawTorch()
        "portal_stone" -> drawPortalStone()
        "rusty_sword" -> drawSword(Color(0xFFB08A5A), Color(0xFF8A5A32))
        "iron_sword" -> drawSword(Color(0xFFC0C8D0), Color(0xFF6A7078))
        "knight_sword" -> drawSword(Color(0xFFE8D9A0), Color(0xFFD4AF37))
        "battle_axe" -> drawAxe()
        "short_bow", "hunter_bow" -> drawBow()
        "oak_staff" -> drawStaff(Color(0xFF8A5A32), Color(0xFF7AD0FF))
        "flame_wand" -> drawStaff(Color(0xFF6B3A28), Color(0xFFE8582C))
        "wood_shield" -> drawShield(Color(0xFF8A5A32), Color(0xFFD9B15D))
        "iron_shield" -> drawShield(Color(0xFF7A8490), Color(0xFFD9C8A4))
        "leather_armor" -> drawArmor(Color(0xFF8A5A32))
        "chain_mail" -> drawArmor(Color(0xFF8A949E))
        "iron_helm" -> drawHelm(Color(0xFF9AA4AE))
        "lucky_ring" -> drawRing(Color(0xFFD4AF37))
        "mana_amulet" -> drawAmulet(Color(0xFF6A90D0))
        else -> when (type) {
            ItemType.WEAPON -> drawSword(Color(0xFFC0C8D0), Color(0xFF6A7078))
            ItemType.SHIELD -> drawShield(Color(0xFF7A8490), Color(0xFFD9C8A4))
            ItemType.ARMOR -> drawArmor(Color(0xFF8A5A32))
            ItemType.HELMET -> drawHelm(Color(0xFF9AA4AE))
            ItemType.ACCESSORY -> drawRing(Color(0xFFD4AF37))
            ItemType.CONSUMABLE -> drawPotion(Color(0xFFE85D4C))
        }
    }
}

private fun DrawScope.drawPotion(liquid: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    // neck
    drawRoundRect(Color(0xFFD9C8A4), Offset(cx - w * 0.08f, h * 0.12f), Size(w * 0.16f, h * 0.18f), CornerRadius(2f))
    // cork
    drawRoundRect(Color(0xFF6B4428), Offset(cx - w * 0.1f, h * 0.08f), Size(w * 0.2f, h * 0.08f), CornerRadius(2f))
    // body
    val path = Path().apply {
        moveTo(cx - w * 0.18f, h * 0.30f)
        lineTo(cx - w * 0.32f, h * 0.55f)
        quadraticBezierTo(cx - w * 0.34f, h * 0.88f, cx, h * 0.90f)
        quadraticBezierTo(cx + w * 0.34f, h * 0.88f, cx + w * 0.32f, h * 0.55f)
        lineTo(cx + w * 0.18f, h * 0.30f)
        close()
    }
    drawPath(path, liquid)
    drawPath(path, Color(0xFF3A2818), style = Stroke(1.5f))
    drawCircle(Color(0x55FFFFFF), w * 0.06f, Offset(cx - w * 0.08f, h * 0.55f))
}

private fun DrawScope.drawBread() {
    val w = size.width
    val h = size.height
    drawOval(Color(0xFFC9A876), Offset(w * 0.12f, h * 0.32f), Size(w * 0.76f, h * 0.40f))
    drawOval(Color(0xFFE8D0A0), Offset(w * 0.18f, h * 0.36f), Size(w * 0.64f, h * 0.28f))
    drawLine(Color(0xFF8A6A3A), Offset(w * 0.30f, h * 0.42f), Offset(w * 0.42f, h * 0.58f), 2f)
    drawLine(Color(0xFF8A6A3A), Offset(w * 0.50f, h * 0.40f), Offset(w * 0.58f, h * 0.58f), 2f)
}

private fun DrawScope.drawTorch() {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    drawRoundRect(Color(0xFF6B4428), Offset(cx - w * 0.07f, h * 0.38f), Size(w * 0.14f, h * 0.48f), CornerRadius(2f))
    val flame = Path().apply {
        moveTo(cx - w * 0.14f, h * 0.42f)
        quadraticBezierTo(cx - w * 0.08f, h * 0.12f, cx, h * 0.18f)
        quadraticBezierTo(cx + w * 0.08f, h * 0.12f, cx + w * 0.14f, h * 0.42f)
        close()
    }
    drawPath(flame, Color(0xFFE8582C))
    drawCircle(Color(0xFFFFC857), w * 0.08f, Offset(cx, h * 0.32f))
}

private fun DrawScope.drawPortalStone() {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val path = Path().apply {
        moveTo(cx, h * 0.14f)
        lineTo(w * 0.82f, h * 0.38f)
        lineTo(w * 0.72f, h * 0.82f)
        lineTo(w * 0.28f, h * 0.82f)
        lineTo(w * 0.18f, h * 0.38f)
        close()
    }
    drawPath(path, Color(0xFF4A3A8A))
    drawPath(path, Color(0xFFD4AF37), style = Stroke(2f))
    drawCircle(Color(0xAA7AD0FF), w * 0.14f, Offset(cx, cy))
    drawCircle(Color(0xEEFFFFFF), w * 0.05f, Offset(cx, cy))
}

private fun DrawScope.drawSword(blade: Color, hilt: Color) {
    val w = size.width
    val h = size.height
    // blade diagonal
    drawLine(blade, Offset(w * 0.28f, h * 0.78f), Offset(w * 0.72f, h * 0.22f), w * 0.10f, StrokeCap.Round)
    drawLine(Color(0x66FFFFFF), Offset(w * 0.34f, h * 0.72f), Offset(w * 0.66f, h * 0.30f), w * 0.03f, StrokeCap.Round)
    // guard
    drawLine(hilt, Offset(w * 0.22f, h * 0.62f), Offset(w * 0.48f, h * 0.78f), w * 0.08f, StrokeCap.Round)
    // pommel
    drawCircle(hilt, w * 0.07f, Offset(w * 0.26f, h * 0.80f))
}

private fun DrawScope.drawAxe() {
    val w = size.width
    val h = size.height
    drawLine(Color(0xFF6B4428), Offset(w * 0.30f, h * 0.78f), Offset(w * 0.68f, h * 0.22f), w * 0.08f, StrokeCap.Round)
    val blade = Path().apply {
        moveTo(w * 0.52f, h * 0.18f)
        lineTo(w * 0.88f, h * 0.28f)
        lineTo(w * 0.78f, h * 0.52f)
        lineTo(w * 0.48f, h * 0.38f)
        close()
    }
    drawPath(blade, Color(0xFFAAB0B8))
    drawPath(blade, Color(0xFF3A2818), style = Stroke(1.5f))
}

private fun DrawScope.drawBow() {
    val w = size.width
    val h = size.height
    val arc = Path().apply {
        moveTo(w * 0.28f, h * 0.18f)
        quadraticBezierTo(w * 0.78f, h * 0.50f, w * 0.28f, h * 0.82f)
    }
    drawPath(arc, Color(0xFF8A5A32), style = Stroke(w * 0.08f, cap = StrokeCap.Round))
    drawLine(Color(0xFFE8D9B8), Offset(w * 0.30f, h * 0.20f), Offset(w * 0.30f, h * 0.80f), 2f)
    drawLine(Color(0xFFD8C49A), Offset(w * 0.32f, h * 0.50f), Offset(w * 0.78f, h * 0.50f), 2.5f, StrokeCap.Round)
}

private fun DrawScope.drawStaff(wood: Color, gem: Color) {
    val w = size.width
    val h = size.height
    drawLine(wood, Offset(w * 0.32f, h * 0.82f), Offset(w * 0.68f, h * 0.22f), w * 0.09f, StrokeCap.Round)
    drawCircle(gem, w * 0.12f, Offset(w * 0.70f, h * 0.20f))
    drawCircle(Color(0x66FFFFFF), w * 0.05f, Offset(w * 0.67f, h * 0.17f))
}

private fun DrawScope.drawShield(body: Color, boss: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.50f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.28f)
        quadraticBezierTo(w * 0.86f, h * 0.58f, w * 0.50f, h * 0.90f)
        quadraticBezierTo(w * 0.14f, h * 0.58f, w * 0.18f, h * 0.28f)
        close()
    }
    drawPath(path, body)
    drawPath(path, Color(0xFF2A1A10), style = Stroke(2f))
    drawCircle(boss, w * 0.10f, Offset(w * 0.50f, h * 0.45f))
}

private fun DrawScope.drawArmor(color: Color) {
    val w = size.width
    val h = size.height
    // torso
    drawRoundRect(color, Offset(w * 0.28f, h * 0.28f), Size(w * 0.44f, h * 0.50f), CornerRadius(6f))
    // shoulders
    drawRoundRect(color, Offset(w * 0.12f, h * 0.30f), Size(w * 0.22f, h * 0.18f), CornerRadius(4f))
    drawRoundRect(color, Offset(w * 0.66f, h * 0.30f), Size(w * 0.22f, h * 0.18f), CornerRadius(4f))
    drawRoundRect(Color(0x33FFFFFF), Offset(w * 0.34f, h * 0.34f), Size(w * 0.32f, h * 0.10f), CornerRadius(3f))
    drawRoundRect(Color(0xFF2A1A10), Offset(w * 0.28f, h * 0.28f), Size(w * 0.44f, h * 0.50f), CornerRadius(6f), style = Stroke(1.5f))
}

private fun DrawScope.drawHelm(color: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    drawOval(color, Offset(w * 0.18f, h * 0.18f), Size(w * 0.64f, h * 0.55f))
    drawRect(color, Offset(w * 0.22f, h * 0.48f), Size(w * 0.56f, h * 0.28f))
    // visor slit
    drawRoundRect(Color(0xFF1A1210), Offset(w * 0.28f, h * 0.48f), Size(w * 0.44f, h * 0.10f), CornerRadius(2f))
    drawLine(Color(0xFFD4AF37), Offset(cx, h * 0.16f), Offset(cx, h * 0.42f), 3f)
    drawCircle(Color(0xFFD4AF37), w * 0.05f, Offset(cx, h * 0.14f))
}

private fun DrawScope.drawRing(color: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f + h * 0.06f
    drawCircle(color, w * 0.28f, Offset(cx, cy), style = Stroke(w * 0.10f))
    drawCircle(Color(0xFF7AD0FF), w * 0.10f, Offset(cx, cy - h * 0.22f))
}

private fun DrawScope.drawAmulet(gem: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    drawArc(Color(0xFFD4AF37), 200f, 140f, false, Offset(w * 0.18f, h * 0.08f), Size(w * 0.64f, h * 0.55f), style = Stroke(2.5f))
    val gemPath = Path().apply {
        moveTo(cx, h * 0.42f)
        lineTo(cx + w * 0.16f, h * 0.58f)
        lineTo(cx, h * 0.82f)
        lineTo(cx - w * 0.16f, h * 0.58f)
        close()
    }
    drawPath(gemPath, gem)
    drawPath(gemPath, Color(0xFFD4AF37), style = Stroke(1.5f))
}
