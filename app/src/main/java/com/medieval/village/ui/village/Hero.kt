package com.medieval.village.ui.village

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.medieval.village.game.Facing
import kotlin.math.sin

private val Skin = Color(0xFFE7B98F)
private val SkinShade = Color(0xFFC99870)
private val Hair = Color(0xFF5A3A22)
private val Tunic = Color(0xFF3E6B8A)
private val TunicDark = Color(0xFF2E5069)
private val Cloak = Color(0xFF8C2F28)
private val CloakDark = Color(0xFF6E241F)
private val Belt = Color(0xFF4A3524)
private val Pants = Color(0xFF6B5B44)
private val Boot = Color(0xFF3B2A1A)
private val Steel = Color(0xFFBFC5CC)
private val SteelDark = Color(0xFF7E858C)

/**
 * 중세 남자 주인공. (x, y)는 발이 닿는 지점.
 * 몸통·망토·검을 도형으로 직접 그린다.
 */
fun DrawScope.drawHero(
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    phase: Float
) {
    val h = 104f                 // 전체 키
    val bob = if (walking) sin(phase) * 2.5f else 0f
    val swing = if (walking) sin(phase) * 7f else 0f
    val baseY = y + bob

    // 그림자
    drawOval(
        color = Color(0x3A000000),
        topLeft = Offset(x - 26f, y - 9f),
        size = Size(52f, 18f)
    )

    val headR = h * 0.155f
    val headCy = baseY - h + headR
    val bodyTop = headCy + headR * 0.75f
    val bodyBottom = baseY - h * 0.30f
    val bodyW = h * 0.34f

    // 망토 (뒤쪽)
    if (facing != Facing.UP) {
        val cloakPath = Path().apply {
            moveTo(x - bodyW * 0.62f, bodyTop + 2f)
            lineTo(x + bodyW * 0.62f, bodyTop + 2f)
            lineTo(x + bodyW * 0.80f, bodyBottom + h * 0.17f)
            lineTo(x - bodyW * 0.80f, bodyBottom + h * 0.17f)
            close()
        }
        drawPath(cloakPath, if (facing == Facing.DOWN) CloakDark else Cloak)
    }

    // 등에 멘 검
    if (facing == Facing.DOWN || facing == Facing.UP) {
        drawLine(
            color = SteelDark,
            start = Offset(x + bodyW * 0.72f, bodyTop - 6f),
            end = Offset(x - bodyW * 0.30f, bodyBottom + 6f),
            strokeWidth = 5f
        )
        drawLine(
            color = Belt,
            start = Offset(x + bodyW * 0.78f, bodyTop - 10f),
            end = Offset(x + bodyW * 0.55f, bodyTop + 2f),
            strokeWidth = 7f
        )
    }

    // 다리 + 부츠
    val legW = bodyW * 0.30f
    val legTop = bodyBottom - 2f
    drawRect(Pants, Offset(x - legW - 2f, legTop), Size(legW, baseY - legTop - 9f + swing * 0.4f))
    drawRect(Pants, Offset(x + 2f, legTop), Size(legW, baseY - legTop - 9f - swing * 0.4f))
    drawRect(Boot, Offset(x - legW - 4f, baseY - 11f + swing * 0.4f), Size(legW + 5f, 11f))
    drawRect(Boot, Offset(x + 1f, baseY - 11f - swing * 0.4f), Size(legW + 5f, 11f))

    // 몸통 (튜닉)
    drawRect(Tunic, Offset(x - bodyW / 2f, bodyTop), Size(bodyW, bodyBottom - bodyTop))
    drawRect(
        TunicDark,
        Offset(x - bodyW / 2f, bodyTop),
        Size(bodyW * 0.28f, bodyBottom - bodyTop)
    )
    // 허리띠
    drawRect(Belt, Offset(x - bodyW / 2f, bodyBottom - 10f), Size(bodyW, 9f))
    drawRect(
        Color(0xFFD9A441),
        Offset(x - 5f, bodyBottom - 10f),
        Size(10f, 9f)
    )

    // 팔
    val armW = bodyW * 0.24f
    val armLen = (bodyBottom - bodyTop) * 0.78f
    drawRect(Tunic, Offset(x - bodyW / 2f - armW + 1f, bodyTop + 4f - swing * 0.5f), Size(armW, armLen))
    drawRect(Tunic, Offset(x + bodyW / 2f - 1f, bodyTop + 4f + swing * 0.5f), Size(armW, armLen))
    drawCircle(Skin, armW * 0.55f, Offset(x - bodyW / 2f - armW * 0.5f + 1f, bodyTop + 4f + armLen - swing * 0.5f))
    drawCircle(Skin, armW * 0.55f, Offset(x + bodyW / 2f + armW * 0.5f - 1f, bodyTop + 4f + armLen + swing * 0.5f))

    // 머리
    drawCircle(Skin, headR, Offset(x, headCy))
    drawCircle(SkinShade, headR, Offset(x, headCy), style = Stroke(width = 1.5f))

    // 머리카락
    val hairPath = Path().apply {
        moveTo(x - headR - 1f, headCy - headR * 0.05f)
        quadraticBezierTo(x, headCy - headR * 1.85f, x + headR + 1f, headCy - headR * 0.05f)
        lineTo(x + headR * 0.9f, headCy - headR * 0.35f)
        quadraticBezierTo(x, headCy - headR * 0.95f, x - headR * 0.9f, headCy - headR * 0.35f)
        close()
    }
    drawPath(hairPath, Hair)

    when (facing) {
        Facing.DOWN -> {
            drawCircle(Color(0xFF2C1E12), 2.2f, Offset(x - headR * 0.38f, headCy + headR * 0.08f))
            drawCircle(Color(0xFF2C1E12), 2.2f, Offset(x + headR * 0.38f, headCy + headR * 0.08f))
            drawLine(
                SkinShade,
                Offset(x - headR * 0.28f, headCy + headR * 0.55f),
                Offset(x + headR * 0.28f, headCy + headR * 0.55f),
                strokeWidth = 1.8f
            )
        }
        Facing.LEFT -> {
            drawCircle(Color(0xFF2C1E12), 2.2f, Offset(x - headR * 0.45f, headCy + headR * 0.08f))
            drawCircle(Hair, headR * 0.55f, Offset(x + headR * 0.55f, headCy - headR * 0.1f))
        }
        Facing.RIGHT -> {
            drawCircle(Color(0xFF2C1E12), 2.2f, Offset(x + headR * 0.45f, headCy + headR * 0.08f))
            drawCircle(Hair, headR * 0.55f, Offset(x - headR * 0.55f, headCy - headR * 0.1f))
        }
        Facing.UP -> {
            drawCircle(Hair, headR * 0.92f, Offset(x, headCy - headR * 0.12f))
        }
    }

    // 어깨 견갑
    drawArc(
        color = Steel,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(x - bodyW * 0.62f, bodyTop - 3f),
        size = Size(bodyW * 0.42f, bodyW * 0.34f)
    )
    drawArc(
        color = Steel,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(x + bodyW * 0.20f, bodyTop - 3f),
        size = Size(bodyW * 0.42f, bodyW * 0.34f)
    )
}
