package com.medieval.village.ui.village

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.medieval.village.game.Facing
import com.medieval.village.game.HeroAnimKind
import com.medieval.village.model.Mercenary
import kotlin.math.floor
import kotlin.math.sin

/**
 * 걸어다니는 용병 스프라이트.
 * 걷기/공격 시트가 있으면 애니메이션, 없으면 정적 전신 → Canvas 폴백.
 */
fun DrawScope.drawMercenary(
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean = false,
    phase: Float = 0f,
    scale: Float = 1f,
    art: CustomArt? = null,
    animKind: HeroAnimKind = HeroAnimKind.IDLE,
    animFrame: Int = 0,
) {
    val attacking = animKind == HeroAnimKind.SLASH ||
        animKind == HeroAnimKind.BOW ||
        animKind == HeroAnimKind.MAGIC
    val bob = if (walking && !attacking) sin(phase) * 2f else 0f
    val sprite = art?.mercAnimSprite(
        spriteKey = mercenary.spriteKey,
        kind = animKind,
        walking = walking,
        walkPhase = phase,
        animFrame = animFrame,
    )
    if (sprite != null) {
        val mirror = when (animKind) {
            HeroAnimKind.SLASH, HeroAnimKind.BOW, HeroAnimKind.MAGIC ->
                facing == Facing.LEFT || facing == Facing.UP
            else -> facing == Facing.LEFT
        }
        drawCustomSprite(
            image = sprite,
            cx = x,
            footY = y + bob,
            worldHeight = 72f * scale,
            mirrorX = mirror,
        )
        return
    }
    if (scale != 1f) {
        scale(scale, scale, pivot = Offset(x, y)) {
            drawMercenaryBody(mercenary, x, y, facing, walking, phase)
        }
    } else {
        drawMercenaryBody(mercenary, x, y, facing, walking, phase)
    }
}

fun CustomArt.mercAnimSprite(
    spriteKey: String,
    kind: HeroAnimKind,
    walking: Boolean,
    walkPhase: Float,
    animFrame: Int,
): androidx.compose.ui.graphics.ImageBitmap? {
    when (kind) {
        HeroAnimKind.SLASH -> {
            heroAnimFrameOrNull("${spriteKey}_slash", animFrame)?.let { return it }
            heroAnimFrameOrNull("${spriteKey}_cast", animFrame)?.let { return it }
        }
        HeroAnimKind.MAGIC, HeroAnimKind.BOW -> {
            heroAnimFrameOrNull("${spriteKey}_cast", animFrame)?.let { return it }
            heroAnimFrameOrNull("${spriteKey}_slash", animFrame)?.let { return it }
        }
        HeroAnimKind.WALK, HeroAnimKind.IDLE -> {
            val useWalk = kind == HeroAnimKind.WALK || walking
            val frame = if (useWalk) {
                if (kind == HeroAnimKind.WALK && animFrame in 0..3) animFrame
                else ((floor(((walkPhase % 6.2831855f) / 6.2831855f) * 4f).toInt() % 4) + 4) % 4
            } else 0
            heroAnimFrameOrNull("${spriteKey}_walk", frame)?.let { return it }
        }
    }
    return npcSpriteOrNull(spriteKey)
}

private fun DrawScope.drawMercenaryBody(
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    phase: Float,
) {
    val bob = if (walking) sin(phase) * 2f else 0f
    val foot = if (walking) sin(phase) * 5f else 0f
    val baseY = y + bob
    val roleColor = when (mercenary.role) {
        "전사" -> Color(0xFF496A8A)
        "도적" -> Color(0xFF4E753F)
        "성기사" -> Color(0xFF777D86)
        "마법사" -> Color(0xFF684A8F)
        else -> Color(0xFF684A8F)
    }
    val hair = when (mercenary.id) {
        "elara" -> Color(0xFFD4B36A)
        "bern" -> Color(0xFF2F241C)
        "shade" -> Color(0xFF3A2A40)
        "aldric" -> Color(0xFFB8B3C8)
        else -> Color(0xFF6B3F28)
    }

    drawOval(Color(0x33000000), Offset(x - 21f, y - 7f), Size(42f, 14f))
    drawRect(Color(0xFF453326), Offset(x - 13f, baseY - 25f + foot * 0.2f), Size(9f, 25f))
    drawRect(Color(0xFF453326), Offset(x + 4f, baseY - 25f - foot * 0.2f), Size(9f, 25f))

    val bodyTop = baseY - 65f
    val bodyBottom = baseY - 23f
    val cloak = Path().apply {
        moveTo(x - 18f, bodyTop)
        lineTo(x + 18f, bodyTop)
        lineTo(x + 23f, bodyBottom + 8f)
        lineTo(x - 23f, bodyBottom + 8f)
        close()
    }
    drawPath(cloak, roleColor.copy(alpha = 0.72f))
    drawRect(roleColor, Offset(x - 15f, bodyTop), Size(30f, bodyBottom - bodyTop))
    drawRect(Color(0xFF4A3524), Offset(x - 16f, bodyBottom - 7f), Size(32f, 6f))

    val head = Offset(x, baseY - 75f)
    drawCircle(Color(0xFFE1AF83), 13f, head)
    drawArc(
        hair,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(x - 14f, head.y - 14f),
        size = Size(28f, 22f)
    )

    when (mercenary.role) {
        "도적" -> {
            drawLine(Color(0xFFBFC5CC), Offset(x + 16f, bodyTop + 6f), Offset(x + 22f, bodyBottom + 4f), 3f)
            drawLine(Color(0xFFBFC5CC), Offset(x - 16f, bodyTop + 8f), Offset(x - 20f, bodyBottom + 2f), 3f)
        }
        "성기사" -> {
            drawCircle(Color(0xFF6D7680), 15f, Offset(x + 19f, bodyTop + 22f))
            drawCircle(Color(0xFFD9A441), 15f, Offset(x + 19f, bodyTop + 22f), style = Stroke(2f))
            drawLine(Color(0xFFBFC5CC), Offset(x - 18f, bodyTop - 2f), Offset(x - 16f, bodyBottom + 8f), 4f)
        }
        "마법사" -> {
            drawLine(Color(0xFF6B4B2E), Offset(x + 17f, bodyTop - 8f), Offset(x + 20f, baseY - 5f), 4f)
            drawCircle(Color(0xFF9D82D5), 6f, Offset(x + 17f, bodyTop - 10f))
        }
        else -> {
            drawLine(Color(0xFFBFC5CC), Offset(x + 18f, bodyTop - 5f), Offset(x + 20f, bodyBottom + 10f), 4f)
        }
    }

    if (facing == Facing.LEFT || facing == Facing.RIGHT) {
        drawCircle(Color(0xFF2C1E12), 1.8f, Offset(x + if (facing == Facing.RIGHT) 5f else -5f, head.y + 1f))
    } else if (facing == Facing.DOWN) {
        drawCircle(Color(0xFF2C1E12), 1.6f, Offset(x - 4f, head.y + 1f))
        drawCircle(Color(0xFF2C1E12), 1.6f, Offset(x + 4f, head.y + 1f))
    }
}
