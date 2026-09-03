package com.medieval.village.ui.village

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.model.HeroJob
import kotlin.math.sin

private val Skin = Color(0xFFE7B98F)
private val SkinShade = Color(0xFFC99870)
private val Hair = Color(0xFF5A3A22)
private val Tunic = Color(0xFF8B5A2B)
private val TunicDark = Color(0xFF6E4520)
private val Vest = Color(0xFF4A3524)
private val VestLight = Color(0xFF6B5340)
private val Cloak = Color(0xFF5E7A48)
private val CloakDark = Color(0xFF4A6238)
private val Belt = Color(0xFF3B2A1A)
private val Pants = Color(0xFF5A4A38)
private val Boot = Color(0xFF2E2116)
private val Steel = Color(0xFFBFC5CC)
private val SteelDark = Color(0xFF7E858C)
private val Gold = Color(0xFFD9A441)

/** 주인공 스프라이트 표시 배율 (원래 대비 약간 크게, 1.5배보다는 작게) */
const val HERO_SIZE_MULT = 1.28f

/**
 * 걸어다니는 중세 모험가.
 * 커스텀 히어로 스프라이트가 있으면 그걸 쓰고, 없으면 Canvas 도형으로 그린다.
 * (x, y)는 발이 닿는 지점. walking + phase 로 흔들림을 준다.
 */
fun DrawScope.drawHero(
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    phase: Float,
    scale: Float = 1f,
    art: CustomArt? = null,
    animKind: HeroAnimKind = HeroAnimKind.IDLE,
    animFrame: Int = 0,
    specialSet: String? = null,
    heroJob: HeroJob = HeroJob.WARRIOR,
    heroRank: Int = 0,
) {
    val attacking = animKind == HeroAnimKind.SLASH ||
        animKind == HeroAnimKind.BOW ||
        animKind == HeroAnimKind.MAGIC ||
        specialSet != null
    val bob = if (walking && !attacking) sin(phase) * 2.5f else 0f
    val drawScale = scale * HERO_SIZE_MULT
    if (art != null) {
        drawCustomHero(
            art = art,
            x = x,
            y = y + bob,
            facing = facing,
            worldHeight = 96f * drawScale,
            walking = walking,
            walkPhase = phase,
            animKind = animKind,
            animFrame = animFrame,
            specialSet = specialSet,
            heroJob = heroJob,
            heroRank = heroRank,
        )
        return
    }
    if (drawScale != 1f) {
        scale(drawScale, drawScale, pivot = Offset(x, y)) {
            drawHeroBody(x, y, facing, walking, phase)
        }
    } else {
        drawHeroBody(x, y, facing, walking, phase)
    }
}

private fun DrawScope.drawHeroBody(
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    phase: Float,
) {
    val h = 108f
    val bob = if (walking) sin(phase) * 2.5f else 0f
    val swing = if (walking) sin(phase) * 7f else 0f
    val baseY = y + bob

    drawOval(
        color = Color(0x3A000000),
        topLeft = Offset(x - 28f, y - 9f),
        size = Size(56f, 18f)
    )

    val headR = h * 0.155f
    val headCy = baseY - h + headR
    val bodyTop = headCy + headR * 0.75f
    val bodyBottom = baseY - h * 0.30f
    val bodyW = h * 0.36f

    if (facing != Facing.UP) {
        val cloakPath = Path().apply {
            moveTo(x - bodyW * 0.55f, bodyTop + 4f)
            lineTo(x + bodyW * 0.55f, bodyTop + 4f)
            quadraticTo(
                x + bodyW * 0.95f,
                bodyBottom + h * 0.05f,
                x + bodyW * 0.70f,
                bodyBottom + h * 0.12f
            )
            lineTo(x - bodyW * 0.70f, bodyBottom + h * 0.12f)
            quadraticTo(
                x - bodyW * 0.95f,
                bodyBottom + h * 0.05f,
                x - bodyW * 0.55f,
                bodyTop + 4f
            )
            close()
        }
        drawPath(cloakPath, if (facing == Facing.DOWN) CloakDark else Cloak)
        drawPath(cloakPath, Color(0x22000000), style = Stroke(2f))
    }

    when (facing) {
        Facing.LEFT -> {
            drawLine(SteelDark, Offset(x + bodyW * 0.15f, bodyBottom - 4f), Offset(x + bodyW * 0.95f, bodyBottom + 18f), 4.5f)
            drawRect(Belt, Offset(x + bodyW * 0.05f, bodyBottom - 8f), Size(10f, 8f))
        }
        Facing.RIGHT -> {
            drawLine(SteelDark, Offset(x - bodyW * 0.15f, bodyBottom - 4f), Offset(x - bodyW * 0.95f, bodyBottom + 18f), 4.5f)
            drawRect(Belt, Offset(x - bodyW * 0.15f, bodyBottom - 8f), Size(10f, 8f))
        }
        else -> {
            drawLine(SteelDark, Offset(x + bodyW * 0.55f, bodyTop + 8f), Offset(x + bodyW * 0.75f, bodyBottom + 8f), 4f)
            drawRect(Belt, Offset(x + bodyW * 0.48f, bodyTop + 4f), Size(9f, 8f))
            drawLine(Gold, Offset(x + bodyW * 0.52f, bodyTop + 2f), Offset(x + bodyW * 0.72f, bodyTop + 10f), 2.5f)
        }
    }

    val legW = bodyW * 0.30f
    val legTop = bodyBottom - 2f
    drawRect(Pants, Offset(x - legW - 2f, legTop), Size(legW, baseY - legTop - 9f + swing * 0.4f))
    drawRect(Pants, Offset(x + 2f, legTop), Size(legW, baseY - legTop - 9f - swing * 0.4f))
    drawRect(Boot, Offset(x - legW - 4f, baseY - 12f + swing * 0.4f), Size(legW + 6f, 12f))
    drawRect(Boot, Offset(x + 1f, baseY - 12f - swing * 0.4f), Size(legW + 6f, 12f))

    drawRect(Tunic, Offset(x - bodyW / 2f, bodyTop), Size(bodyW, bodyBottom - bodyTop))
    drawRect(TunicDark, Offset(x - bodyW / 2f, bodyTop), Size(bodyW * 0.22f, bodyBottom - bodyTop))

    drawRect(Vest, Offset(x - bodyW * 0.38f, bodyTop + 4f), Size(bodyW * 0.76f, (bodyBottom - bodyTop) * 0.72f))
    drawRect(VestLight, Offset(x - bodyW * 0.10f, bodyTop + 6f), Size(bodyW * 0.08f, (bodyBottom - bodyTop) * 0.68f))
    drawLine(Gold, Offset(x - bodyW * 0.08f, bodyTop + 10f), Offset(x - bodyW * 0.08f, bodyBottom - 18f), 1.5f)

    drawRect(Belt, Offset(x - bodyW / 2f, bodyBottom - 11f), Size(bodyW, 10f))
    drawRect(Gold, Offset(x - 5f, bodyBottom - 10f), Size(10f, 8f))
    drawRoundRect(
        Color(0xFF5A4030),
        Offset(x + bodyW * 0.18f, bodyBottom - 14f),
        Size(10f, 12f),
        CornerRadius(3f, 3f)
    )

    val armW = bodyW * 0.24f
    val armLen = (bodyBottom - bodyTop) * 0.78f
    drawRect(Tunic, Offset(x - bodyW / 2f - armW + 1f, bodyTop + 4f - swing * 0.5f), Size(armW, armLen))
    drawRect(Tunic, Offset(x + bodyW / 2f - 1f, bodyTop + 4f + swing * 0.5f), Size(armW, armLen))
    drawCircle(Skin, armW * 0.55f, Offset(x - bodyW / 2f - armW * 0.5f + 1f, bodyTop + 4f + armLen - swing * 0.5f))
    drawCircle(Skin, armW * 0.55f, Offset(x + bodyW / 2f + armW * 0.5f - 1f, bodyTop + 4f + armLen + swing * 0.5f))

    drawCircle(Skin, headR, Offset(x, headCy))
    drawCircle(SkinShade, headR, Offset(x, headCy), style = Stroke(width = 1.5f))

    val hairPath = Path().apply {
        moveTo(x - headR - 1f, headCy - headR * 0.05f)
        quadraticTo(x, headCy - headR * 1.85f, x + headR + 1f, headCy - headR * 0.05f)
        lineTo(x + headR * 0.9f, headCy - headR * 0.35f)
        quadraticTo(x, headCy - headR * 0.95f, x - headR * 0.9f, headCy - headR * 0.35f)
        close()
    }
    drawPath(hairPath, Hair)

    when (facing) {
        Facing.DOWN -> {
            drawCircle(Color(0xFF2C1E12), 2.3f, Offset(x - headR * 0.38f, headCy + headR * 0.08f))
            drawCircle(Color(0xFF2C1E12), 2.3f, Offset(x + headR * 0.38f, headCy + headR * 0.08f))
            drawCircle(Color(0x55FFFFFF), 0.9f, Offset(x - headR * 0.32f, headCy + headR * 0.02f))
            drawCircle(Color(0x55FFFFFF), 0.9f, Offset(x + headR * 0.44f, headCy + headR * 0.02f))
            drawLine(
                SkinShade,
                Offset(x - headR * 0.22f, headCy + headR * 0.52f),
                Offset(x + headR * 0.22f, headCy + headR * 0.52f),
                strokeWidth = 1.8f
            )
        }
        Facing.LEFT -> {
            drawCircle(Color(0xFF2C1E12), 2.3f, Offset(x - headR * 0.45f, headCy + headR * 0.08f))
            drawCircle(Hair, headR * 0.55f, Offset(x + headR * 0.55f, headCy - headR * 0.1f))
        }
        Facing.RIGHT -> {
            drawCircle(Color(0xFF2C1E12), 2.3f, Offset(x + headR * 0.45f, headCy + headR * 0.08f))
            drawCircle(Hair, headR * 0.55f, Offset(x - headR * 0.55f, headCy - headR * 0.1f))
        }
        Facing.UP -> {
            drawCircle(Hair, headR * 0.92f, Offset(x, headCy - headR * 0.12f))
            val backCloak = Path().apply {
                moveTo(x - bodyW * 0.5f, bodyTop + 2f)
                lineTo(x + bodyW * 0.5f, bodyTop + 2f)
                lineTo(x + bodyW * 0.65f, bodyBottom + 8f)
                lineTo(x - bodyW * 0.65f, bodyBottom + 8f)
                close()
            }
            drawPath(backCloak, CloakDark)
        }
    }
}
