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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medieval.village.ui.skin.rememberUiSkin
import com.medieval.village.ui.theme.ClassicType
import com.medieval.village.game.DungeonProjectile
import com.medieval.village.game.Facing
import com.medieval.village.game.MagicBoltKind
import com.medieval.village.game.MeleeSlashFx
import com.medieval.village.game.SpecialSkillFx
import com.medieval.village.game.dirX
import com.medieval.village.game.dirY
import com.medieval.village.model.SkillSlotUi
import com.medieval.village.model.WeaponStyle
import com.medieval.village.ui.SkillIcon
import com.medieval.village.ui.theme.Palette
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.rememberCustomArtOrNull
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
        val dpad = rememberUiSkin()?.dpad
        if (dpad != null) {
            Canvas(modifier = Modifier.size(124.dp)) {
                drawImage(
                    image = dpad,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(dpad.width, dpad.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    filterQuality = FilterQuality.Medium,
                )
            }
            // 손가락 위치를 알려주는 은은한 광점
            Box(
                modifier = Modifier
                    .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
                    .size(38.dp)
                    .background(Color(0x55FFE29A), CircleShape)
            )
        } else {
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
}

/** 오른쪽 공격 버튼 */
@Composable
fun AttackButton(
    label: String,
    enabled: Boolean,
    onAttack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val face = rememberUiSkin()?.roundButton
    Box(
        modifier = modifier
            .size(86.dp)
            .then(
                if (face == null) {
                    Modifier.background(
                        if (enabled) Color(0xCC8B2E2E) else Color(0x66443333),
                        CircleShape,
                    )
                } else {
                    Modifier
                }
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onTap = { onAttack() })
            },
        contentAlignment = Alignment.Center
    ) {
        if (face != null) {
            Canvas(modifier = Modifier.size(86.dp)) {
                drawImage(
                    image = face,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(face.width, face.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    filterQuality = FilterQuality.Medium,
                    alpha = if (enabled) 1f else 0.5f,
                )
            }
        }
        Text(
            text = label,
            color = if (enabled) Color(0xFFF3E2B8) else Color(0x88C8B8A0),
            style = ClassicType.Button,
            modifier = Modifier.offset(y = 22.dp),
        )
    }
}

/**
 * @deprecated 특별스킬은 공격 버튼 옆(맵 밖)으로 이동함.
 */
@Composable
fun BoxScope.DungeonSpecialSkillOverlay(
    skillSlots: List<SkillSlotUi>,
    onSpecial: (Int) -> Unit,
) {
    // 하위 호환 — 빈 구현 (맵 위 배치 제거)
    if (skillSlots.isEmpty()) return
}

/** 맵 바깥용 — 이동 패드 + (선택) 로그 + 특별스킬·공격 */
@Composable
fun DungeonCombatControls(
    attackLabel: String,
    attackEnabled: Boolean,
    onPad: (Float, Float) -> Unit,
    onPadRelease: () -> Unit,
    onAttack: () -> Unit,
    modifier: Modifier = Modifier,
    logContent: (@Composable () -> Unit)? = null,
    skillSlots: List<SkillSlotUi> = emptyList(),
    onSpecial: (Int) -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        VirtualMovePad(
            onVector = onPad,
            onRelease = onPadRelease,
        )
        if (logContent != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            ) {
                logContent()
            }
        }
        // 휘두르기(공격) 쪽으로 특별스킬 배치
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
}

/** @deprecated 하위 호환 — 특별스킬 오버레이 + 하단 컨트롤로 분리됨 */
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
    DungeonSpecialSkillOverlay(skillSlots = skillSlots, onSpecial = onSpecial)
    // 패드는 맵에 두지 않음 — 호출측에서 DungeonCombatControls 사용
}

@Composable
private fun SpecialSkillButton(
    slot: SkillSlotUi,
    onClick: () -> Unit,
) {
    val filled = slot.skillId != null
    val art = rememberCustomArtOrNull()
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                when {
                    !filled -> Color(0x332A1C12)
                    slot.enabled -> Color(0xCC5A3A18)
                    else -> Color(0x55443322)
                },
                CircleShape,
            )
            .pointerInput(slot.enabled, slot.skillId) {
                if (!slot.enabled || !filled) return@pointerInput
                detectTapGestures(onTap = { onClick() })
            },
        contentAlignment = Alignment.Center,
    ) {
        if (filled) {
            SkillIcon(
                skillId = slot.skillId,
                size = 44.dp,
                art = art,
                enabled = slot.enabled,
                showBorder = true,
                circular = true,
            )
            if (slot.rank > 1) {
                Text(
                    "Lv${slot.rank}",
                    color = if (slot.enabled) Palette.Gold else Color(0x88C8B8A0),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color(0xAA1A120C), CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        } else {
            Text(
                text = "—",
                color = Color(0x66C8B8A0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** 칼 휘두르기 반달(초승달) 참격 이펙트 */
fun DrawScope.drawMeleeSlashFx(fx: MeleeSlashFx) {
    val fade = (1f - fx.progress).coerceIn(0f, 1f)
    val power = fx.power.coerceAtLeast(0.8f)
    val angle = when (fx.facing) {
        Facing.RIGHT -> 0f
        Facing.DOWN -> 90f
        Facing.LEFT -> 180f
        Facing.UP -> 270f
    }
    val sweep = 130f + (power - 1f) * 28f
    val start = angle - sweep / 2f + fx.progress * 22f
    val outerR = (62f + fx.progress * 22f) * power
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
        style = Stroke(width = 7f * fade * power + 2f, cap = StrokeCap.Round)
    )
    drawArc(
        color = Color(0xFFFFE29A).copy(alpha = 0.8f * fade),
        startAngle = start + 10f,
        sweepAngle = sweep - 20f,
        useCenter = false,
        topLeft = Offset(fx.x - outerR * 0.88f, fx.y - outerR * 0.88f),
        size = Size(outerR * 1.76f, outerR * 1.76f),
        style = Stroke(width = 3.5f * fade * power, cap = StrokeCap.Round)
    )
    val tipAng = Math.toRadians((start + sweep * 0.72f).toDouble())
    val tipX = fx.x + cos(tipAng).toFloat() * outerR
    val tipY = fx.y + sin(tipAng).toFloat() * outerR
    drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.85f * fade), 6.5f * fade * power, Offset(tipX, tipY))
}

/** 특별스킬 스프라이트 FX */
fun DrawScope.drawSpecialSkillFx(fx: SpecialSkillFx, art: CustomArt?) {
    val fade = (1f - fx.progress * 0.45f).coerceIn(0.35f, 1f)
    val bmp = art?.heroAnimFrameOrNull(fx.spriteKey, fx.frame)
    if (bmp != null) {
        val mirror = fx.facing == Facing.LEFT || fx.facing == Facing.UP
        val h = 88f * fx.scale * (1f + fx.progress * 0.12f)
        // drawCustomSprite 은 alpha 미지원 → 크기·위치로 연출
        drawCustomSprite(
            image = bmp,
            cx = fx.x,
            footY = fx.y + h * 0.45f,
            worldHeight = h,
            mirrorX = mirror,
        )
        return
    }
    // 폴백: 골드 버스트
    val r = (36f + fx.progress * 28f) * fx.scale
    drawCircle(Color(0x66FFE29A).copy(alpha = 0.45f * fade), r, Offset(fx.x, fx.y))
    drawCircle(Color(0xAAFFF8E0).copy(alpha = 0.55f * fade), r * 0.45f, Offset(fx.x, fx.y))
}

fun DrawScope.drawDungeonProjectile(p: DungeonProjectile, art: CustomArt? = null) {
    val key = p.fxSpriteKey
    if (key != null && art != null) {
        val frame = (((1.4f - p.life) * 8f).toInt() % 4 + 4) % 4
        val bmp = art.heroAnimFrameOrNull(key, frame)
        if (bmp != null) {
            val ang = atan2(p.vy, p.vx)
            val mirror = cos(ang) < 0f
            val h = when (p.magicKind) {
                MagicBoltKind.FIRE -> 52f
                MagicBoltKind.ORB, MagicBoltKind.HOLY -> 50f
                else -> when (p.style) {
                    WeaponStyle.BOW -> 36f
                    WeaponStyle.MAGIC -> 48f
                    else -> 40f
                }
            }
            drawCustomSprite(
                image = bmp,
                cx = p.x,
                footY = p.y + h * 0.35f,
                worldHeight = h,
                mirrorX = mirror,
            )
            return
        }
    }
    when {
        p.magicKind != null -> drawMagicBolt(p)
        p.style == WeaponStyle.BOW -> {
            val ang = atan2(p.vy, p.vx)
            val len = 22f
            val path = Path().apply {
                moveTo(p.x + cos(ang) * len, p.y + sin(ang) * len)
                lineTo(p.x - cos(ang) * len * 0.6f, p.y - sin(ang) * len * 0.6f)
            }
            drawPath(path, Color(0xFFD8C49A), style = Stroke(4.5f, cap = StrokeCap.Round))
            drawCircle(Color(0xFFB8A070), 3.5f, Offset(p.x + cos(ang) * len, p.y + sin(ang) * len))
        }
        p.style == WeaponStyle.MAGIC -> {
            drawCircle(Color(0x887B5CFF), 16f, Offset(p.x, p.y))
            drawCircle(Color(0xFFC9B6FF), 9f, Offset(p.x, p.y))
            drawCircle(Color(0xFFFFFFFF), 3.5f, Offset(p.x - 2f, p.y - 2f))
        }
        else -> {
            drawCircle(Color(0xFFE8D9B8), 6f, Offset(p.x, p.y))
        }
    }
}

private fun DrawScope.drawMagicBolt(p: DungeonProjectile) {
    val ang = atan2(p.vy, p.vx)
    val c = Offset(p.x, p.y)
    val spark = ((1.4f - p.life) * 18f)
    when (p.magicKind) {
        MagicBoltKind.BASIC -> {
            drawCircle(Color(0x667AD0FF), 11f, c)
            drawCircle(Color(0xBB9BE8FF), 6.5f, c)
            drawCircle(Color(0xFFFFFFFF), 2.6f, Offset(p.x - 1.5f, p.y - 1.8f))
            drawCircle(
                Color(0x99C8F4FF),
                2.2f,
                Offset(p.x - cos(ang) * 12f, p.y - sin(ang) * 12f),
            )
        }
        MagicBoltKind.FIRE -> {
            drawCircle(Color(0x99E8582C), 18f, c)
            drawCircle(Color(0xFFE07A28), 12f, c)
            drawCircle(Color(0xFFFFE08A), 6f, c)
            drawCircle(Color(0xFFFFF6D0), 2.8f, Offset(p.x - 2f, p.y - 3f))
            drawCircle(
                Color(0xCCFF7A3A),
                4f,
                Offset(p.x - cos(ang) * (10f + spark % 6f), p.y - sin(ang) * (10f + spark % 6f)),
            )
        }
        MagicBoltKind.ICE -> {
            val len = 16f
            val path = Path().apply {
                moveTo(p.x + cos(ang) * len, p.y + sin(ang) * len)
                lineTo(p.x + cos(ang + 2.2f) * 8f, p.y + sin(ang + 2.2f) * 8f)
                lineTo(p.x - cos(ang) * 10f, p.y - sin(ang) * 10f)
                lineTo(p.x + cos(ang - 2.2f) * 8f, p.y + sin(ang - 2.2f) * 8f)
                close()
            }
            drawPath(path, Color(0xCC9BE8FF))
            drawPath(path, Color(0xFFDFF6FF), style = Stroke(2.2f))
            drawCircle(Color(0xFFFFFFFF), 3f, c)
        }
        MagicBoltKind.LIGHTNING -> {
            val len = 20f
            val path = Path().apply {
                moveTo(p.x + cos(ang) * len, p.y + sin(ang) * len)
                lineTo(p.x + cos(ang + 0.7f) * 6f, p.y + sin(ang + 0.7f) * 6f)
                lineTo(p.x - cos(ang) * 4f, p.y - sin(ang) * 4f)
                lineTo(p.x + cos(ang - 0.7f) * 8f, p.y + sin(ang - 0.7f) * 8f)
                lineTo(p.x - cos(ang) * 16f, p.y - sin(ang) * 16f)
            }
            drawPath(path, Color(0xFFFFF4A0), style = Stroke(4.2f, cap = StrokeCap.Round))
            drawPath(path, Color(0xFFFFFFFF), style = Stroke(1.8f, cap = StrokeCap.Round))
            drawCircle(Color(0xAAFFF8C8), 10f, c)
        }
        MagicBoltKind.ORB -> {
            drawCircle(Color(0xAA6A3BB5), 20f, c)
            drawCircle(Color(0xCC9B6CFF), 13f, c)
            drawCircle(Color(0xFFE8D6FF), 7f, c)
            drawCircle(Color(0xFFFFFFFF), 3f, Offset(p.x - 3f, p.y - 3f))
        }
        MagicBoltKind.HOLY -> {
            drawCircle(Color(0x88FFE08A), 20f, c)
            drawCircle(Color(0xFFFFE08A), 12f, c, style = Stroke(3.2f))
            drawCircle(Color(0xFFFFF6D0), 7f, c)
            drawCircle(Color(0xFFFFFFFF), 3.2f, Offset(p.x - 2f, p.y - 2.5f))
            val arm = 14f
            drawLine(Color(0xDDFFE9A0), Offset(p.x - arm, p.y), Offset(p.x + arm, p.y), 2.4f)
            drawLine(Color(0xDDFFE9A0), Offset(p.x, p.y - arm), Offset(p.x, p.y + arm), 2.4f)
        }
        null -> Unit
    }
}

fun slashOrigin(heroX: Float, heroY: Float, facing: Facing): Offset {
    return Offset(
        heroX + facing.dirX() * 18f,
        heroY + facing.dirY() * 10f - 28f
    )
}
