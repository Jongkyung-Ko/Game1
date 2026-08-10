package com.medieval.village.ui.place

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.game.DungeonProjectile
import com.medieval.village.game.Facing
import com.medieval.village.game.MeleeSlashFx
import com.medieval.village.game.dirX
import com.medieval.village.game.dirY
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
        Canvas(Modifier = Modifier.size(124.dp)) {
            drawCircle(Color(0x44FFE29A), radius = size.minDimension * 0.48f, style = Stroke(3f))
            drawCircle(Color(0x33FFFFFF), radius = size.minDimension * 0.18f)
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
 */
@Composable
fun BoxScope.DungeonCombatHud(
    attackLabel: String,
    attackEnabled: Boolean,
    onPad: (Float, Float) -> Unit,
    onPadRelease: () -> Unit,
    onAttack: () -> Unit,
) {
    VirtualMovePad(
        onVector = onPad,
        onRelease = onPadRelease,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 10.dp, bottom = 10.dp)
    )
    AttackButton(
        label = attackLabel,
        enabled = attackEnabled,
        onAttack = onAttack,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 12.dp, bottom = 14.dp)
    )
}

fun DrawScope.drawMeleeSlashFx(fx: MeleeSlashFx) {
    val fade = 1f - fx.progress
    val angle = when (fx.facing) {
        Facing.RIGHT -> 0f
        Facing.DOWN -> 90f
        Facing.LEFT -> 180f
        Facing.UP -> 270f
    }
    val sweep = 110f
    val start = angle - sweep / 2f + fx.progress * 18f
    val radius = 54f + fx.progress * 18f
    val color = Color(0xFFE8E0D0).copy(alpha = 0.85f * fade)
    val edge = Color(0xFFFFF6D0).copy(alpha = 0.95f * fade)
    drawArc(
        color = color,
        startAngle = start,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(fx.x - radius, fx.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = 14f * fade, cap = StrokeCap.Round)
    )
    drawArc(
        color = edge,
        startAngle = start + 8f,
        sweepAngle = sweep - 16f,
        useCenter = false,
        topLeft = Offset(fx.x - radius * 0.82f, fx.y - radius * 0.82f),
        size = Size(radius * 1.64f, radius * 1.64f),
        style = Stroke(width = 5f * fade, cap = StrokeCap.Round)
    )
    val tipAng = Math.toRadians((start + sweep * 0.7f).toDouble())
    val tipX = fx.x + cos(tipAng).toFloat() * radius
    val tipY = fx.y + sin(tipAng).toFloat() * radius
    drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.7f * fade), 5f * fade, Offset(tipX, tipY))
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
