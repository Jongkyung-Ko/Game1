package com.medieval.village.ui.place

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.DungeonProjectile
import com.medieval.village.game.Facing
import com.medieval.village.game.MeleeSlashFx
import com.medieval.village.game.dirX
import com.medieval.village.game.dirY
import com.medieval.village.model.SkillSlotUi
import com.medieval.village.model.WeaponStyle
import com.medieval.village.ui.theme.Palette
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 던전/탐험용 가상 이동 패드 (왼쪽 하단).
 * 손가락을 드래그하면 정규화 벡터(-1..1)를 보낸다.
 */
@Composable
fun VirtualMovePad(
    onVector: (Float, Float) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { 58.dp.toPx() }
    var knobX by remember { mutableFloatStateOf(0f) }
    var knobY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .size(124.dp)
            .background(Color(0x661A120C), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val len = hypot(dx, dy).coerceAtLeast(0.01f)
                        val clamped = len.coerceAtMost(radiusPx)
                        knobX = dx / len * clamped
                        knobY = dy / len * clamped
                        onVector(knobX / radiusPx, knobY / radiusPx)
                    },
                    onDragEnd = {
                        knobX = 0f
                        knobY = 0f
                        onRelease()
                    },
                    onDragCancel = {
                        knobX = 0f
                        knobY = 0f
                        onRelease()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val nextX = knobX + dragAmount.x
                        val nextY = knobY + dragAmount.y
                        val len = hypot(nextX, nextY).coerceAtLeast(0.01f)
                        val clamped = len.coerceAtMost(radiusPx)
                        knobX = nextX / len * clamped
                        knobY = nextY / len * clamped
                        onVector(knobX / radiusPx, knobY / radiusPx)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(124.dp)) {
            val dim = this.size.minDimension
            drawCircle(Color(0x44FFE29A), radius = dim * 0.48f, style = Stroke(3f))
            drawCircle(Color(0x33FFFFFF), radius = dim * 0.18f)
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
                .size(46.dp)
                .background(Color(0xDDFFE29A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("이동", color = Color(0xFF2A1C12), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** 오른쪽 공격 버튼 */
@Composable
fun AttackButton(
    label: String,
    enabled: Boolean,
    onAttack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(86.dp)
            .background(
                if (enabled) Color(0xCC8B2E2E) else Color(0x66443333),
                CircleShape
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onTap = { onAttack() })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) Palette.Parchment else Color(0x88C8B8A0),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 부모 Box 안에서 좌·우 하단에 배치한다.
 * fillMaxSize 오버레이를 쓰지 않아 중앙 맵 탭(상자)이 막히지 않는다.
 * 공격 버튼 위에 특별스킬 슬롯 최대 3개.
 */
@Composable
fun BoxScope.DungeonCombatHud(
    attackLabel: String,
    attackEnabled: Boolean,
    skillSlots: List<SkillSlotUi> = emptyList(),
    onPad: (Float, Float) -> Unit,
    onPadRelease: () -> Unit,
    onAttack: () -> Unit,
    onSpecial: (Int) -> Unit = {},
) {
    VirtualMovePad(
        onVector = onPad,
        onRelease = onPadRelease,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 10.dp, bottom = 10.dp)
    )
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 12.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (skillSlots.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                skillSlots.forEach { slot ->
                    SpecialSkillButton(
                        slot = slot,
                        onClick = { onSpecial(slot.slotIndex) },
                    )
                }
            }
        }
        AttackButton(
            label = attackLabel,
            enabled = attackEnabled,
            onAttack = onAttack,
        )
    }
}

@Composable
private fun SpecialSkillButton(
    slot: SkillSlotUi,
    onClick: () -> Unit,
) {
    val filled = slot.skillId != null
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                when {
                    !filled -> Color(0x442A1C12)
                    slot.enabled -> Color(0xCC5A3A18)
                    else -> Color(0x66443322)
                },
                CircleShape,
            )
            .pointerInput(slot.enabled, slot.skillId) {
                if (!slot.enabled || !filled) return@pointerInput
                detectTapGestures(onTap = { onClick() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = slot.shortName,
                color = when {
                    !filled -> Color(0x66C8B8A0)
                    slot.enabled -> Palette.Gold
                    else -> Color(0x88C8B8A0)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            if (filled && slot.mpCost > 0) {
                Text(
                    "MP${slot.mpCost}",
                    color = if (slot.enabled) Palette.Mana else Color(0x6655AACC),
                    fontSize = 9.sp,
                )
            }
        }
    }
}

/** 칼 휘두르기 반달(초승달) 참격 이펙트 */
fun DrawScope.drawMeleeSlashFx(fx: MeleeSlashFx) {
    val fade = (1f - fx.progress).coerceIn(0f, 1f)
    val angle = when (fx.facing) {
        Facing.RIGHT -> 0f
        Facing.DOWN -> 90f
        Facing.LEFT -> 180f
        Facing.UP -> 270f
    }
    val sweep = 130f
    val start = angle - sweep / 2f + fx.progress * 22f
    val outerR = 62f + fx.progress * 22f
    val innerR = outerR * 0.58f
    val glow = Color(0xFFFFF1B0).copy(alpha = 0.35f * fade)
    val blade = Color(0xFFF5F0E4).copy(alpha = 0.92f * fade)
    val edge = Color(0xFFFFFFFF).copy(alpha = 0.95f * fade)

    // 반달 면 — 바깥 호 → 안쪽 호를 역방향으로 닫아 초승달 형태
    val crescent = Path().apply {
        val steps = 18
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val a = Math.toRadians((start + sweep * t).toDouble())
            val px = fx.x + cos(a).toFloat() * outerR
            val py = fx.y + sin(a).toFloat() * outerR
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        for (i in steps downTo 0) {
            val t = i / steps.toFloat()
            val a = Math.toRadians((start + sweep * t).toDouble())
            lineTo(
                fx.x + cos(a).toFloat() * innerR,
                fx.y + sin(a).toFloat() * innerR,
            )
        }
        close()
    }
    drawPath(crescent, glow)
    drawPath(crescent, blade.copy(alpha = 0.55f * fade))
    drawArc(
        color = edge,
        startAngle = start,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(fx.x - outerR, fx.y - outerR),
        size = Size(outerR * 2f, outerR * 2f),
        style = Stroke(width = 7f * fade + 2f, cap = StrokeCap.Round)
    )
    drawArc(
        color = Color(0xFFFFE29A).copy(alpha = 0.8f * fade),
        startAngle = start + 10f,
        sweepAngle = sweep - 20f,
        useCenter = false,
        topLeft = Offset(fx.x - outerR * 0.88f, fx.y - outerR * 0.88f),
        size = Size(outerR * 1.76f, outerR * 1.76f),
        style = Stroke(width = 3.5f * fade, cap = StrokeCap.Round)
    )
    val tipAng = Math.toRadians((start + sweep * 0.72f).toDouble())
    val tipX = fx.x + cos(tipAng).toFloat() * outerR
    val tipY = fx.y + sin(tipAng).toFloat() * outerR
    drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.85f * fade), 6.5f * fade, Offset(tipX, tipY))
}

fun DrawScope.drawDungeonProjectile(p: DungeonProjectile) {
    when (p.style) {
        WeaponStyle.BOW -> {
            val ang = atan2(p.vy, p.vx)
            val len = 22f
            val path = Path().apply {
                moveTo(p.x + cos(ang) * len, p.y + sin(ang) * len)
                lineTo(p.x - cos(ang) * len * 0.6f, p.y - sin(ang) * len * 0.6f)
            }
            drawPath(path, Color(0xFFD8C49A), style = Stroke(4.5f, cap = StrokeCap.Round))
            drawCircle(Color(0xFFB8A070), 3.5f, Offset(p.x + cos(ang) * len, p.y + sin(ang) * len))
        }
        WeaponStyle.MAGIC -> {
            drawCircle(Color(0x887B5CFF), 16f, Offset(p.x, p.y))
            drawCircle(Color(0xFFC9B6FF), 9f, Offset(p.x, p.y))
            drawCircle(Color(0xFFFFFFFF), 3.5f, Offset(p.x - 2f, p.y - 2f))
        }
        else -> {
            drawCircle(Color(0xFFE8D9B8), 6f, Offset(p.x, p.y))
        }
    }
}

fun slashOrigin(heroX: Float, heroY: Float, facing: Facing): Offset {
    return Offset(
        heroX + facing.dirX() * 18f,
        heroY + facing.dirY() * 10f - 28f
    )
}
