package com.medieval.village.ui.village

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.sin

/** Kenney-inspired chunky characters & critters (procedural sprites). */

fun DrawScope.drawKenneyHero(
    cx: Float,
    cy: Float,
    walking: Boolean,
    facingRight: Boolean,
    t: Float,
    scale: Float = 1f,
) {
    val bob = if (walking) sin(t * 11f) * 2.2f * scale else sin(t * 2.2f) * 1.2f * scale
    val leg = if (walking) sin(t * 11f) * 5f * scale else sin(t * 2.4f) * 1.2f * scale
    val arm = if (walking) -leg * 0.7f else sin(t * 2.1f + 1f) * 3f * scale
    translate(cx, cy + bob) {
        scale(scale, scale, Offset.Zero) {
            kOval(Color(0x55000000), -10f, 18f, 20f, 7f, outline = false)
            kRound(Color(0xFF3E2723), -7f, 6f + leg * 0.15f, 6f, 12f, 2f)
            kRound(Color(0xFF3E2723), 1f, 6f - leg * 0.15f, 6f, 12f, 2f)
            kRound(Color(0xFF1A120C), -8f, 16f + leg * 0.15f, 8f, 4f, 2f)
            kRound(Color(0xFF1A120C), 0f, 16f - leg * 0.15f, 8f, 4f, 2f)
            kRound(Color(0xFF6D4C41), -9f, -8f, 18f, 16f, 3f)
            kRound(Color(0xFF8D6E63), -8f, -6f, 16f, 12f, 2f)
            kRect(Color(0xFF3E2723), -9f, 4f, 18f, 3f)
            kRect(Color(0xFFC9A227), -2f, 3f, 4f, 5f)
            val cape = Path().apply {
                moveTo(-4f, -10f)
                lineTo(if (facingRight) -16f else 16f, -2f + sin(t * 3f) * 2f)
                lineTo(if (facingRight) -12f else 12f, 10f)
                lineTo(2f, 2f)
                close()
            }
            kPath(Color(0xFF2E7D32), cape)
            rotate(degrees = if (facingRight) arm else -arm, pivot = Offset(6f, -4f)) {
                kRound(Color(0xFFE0B090), if (facingRight) 7f else -13f, -4f, 6f, 12f, 2f)
            }
            rotate(degrees = if (facingRight) -arm * 0.6f else arm * 0.6f, pivot = Offset(-6f, -4f)) {
                kRound(Color(0xFFE0B090), if (facingRight) -13f else 7f, -4f, 6f, 11f, 2f)
            }
            kCircle(Color(0xFFE8C4A8), 8f, Offset(0f, -16f))
            kRound(Color(0xFF3E2723), -8f, -24f, 16f, 8f, 3f)
            val eyeX = if (facingRight) 2f else -2f
            drawCircle(Color(0xFF1A1A1A), 1.3f, Offset(eyeX - 2.5f, -17f))
            drawCircle(Color(0xFF1A1A1A), 1.3f, Offset(eyeX + 2.5f, -17f))
            if (facingRight) {
                kRect(Color(0xFFB0BEC5), 12f, -6f + arm * 0.1f, 3f, 16f)
                kRect(Color(0xFFC9A227), 10f, -7f, 7f, 3f)
            } else {
                kRect(Color(0xFFB0BEC5), -15f, -6f - arm * 0.1f, 3f, 16f)
                kRect(Color(0xFFC9A227), -17f, -7f, 7f, 3f)
            }
        }
    }
}

fun DrawScope.drawKenneyMerc(
    cx: Float,
    cy: Float,
    walking: Boolean,
    facingRight: Boolean,
    t: Float,
    tunic: Color,
    scale: Float = 0.85f,
) {
    val bob = if (walking) sin(t * 10f) * 2f * scale else sin(t * 2f) * 1f * scale
    val leg = if (walking) sin(t * 10f) * 4f * scale else sin(t * 2.2f) * 1f * scale
    val wave = if (!walking) sin(t * 3f) * 12f else 0f
    translate(cx, cy + bob) {
        scale(scale, scale, Offset.Zero) {
            kOval(Color(0x44000000), -8f, 14f, 16f, 5f, outline = false)
            kRound(Color(0xFF37474F), -6f, 4f + leg * 0.12f, 5f, 10f, 2f)
            kRound(Color(0xFF37474F), 1f, 4f - leg * 0.12f, 5f, 10f, 2f)
            kRound(tunic, -8f, -8f, 16f, 14f, 3f)
            kRect(Color(0xFF263238), -8f, 2f, 16f, 2.5f)
            rotate(degrees = if (facingRight) wave else -wave, pivot = Offset(6f, -4f)) {
                kRound(Color(0xFFE0B090), 6f, -4f, 5f, 10f, 2f)
            }
            kRound(Color(0xFFE0B090), -11f, -4f, 5f, 9f, 2f)
            kCircle(Color(0xFFE8C4A8), 6.5f, Offset(0f, -14f))
            kRound(Color(0xFF455A64), -6.5f, -20f, 13f, 6f, 2f)
            val eyeX = if (facingRight) 1.5f else -1.5f
            drawCircle(Color(0xFF1A1A1A), 1.1f, Offset(eyeX - 2f, -14.5f))
            drawCircle(Color(0xFF1A1A1A), 1.1f, Offset(eyeX + 2f, -14.5f))
        }
    }
}

fun DrawScope.drawKenneyNpc(
    cx: Float,
    cy: Float,
    t: Float,
    seed: Int,
    tunic: Color,
    apron: Color? = null,
    hat: Color? = null,
    scale: Float = 1f,
    wave: Boolean = true,
) {
    val bob = sin(t * 2.1f + seed) * 1.4f * scale
    val armWave = if (wave) sin(t * 2.8f + seed * 0.7f) * 18f else sin(t * 1.8f + seed) * 4f
    val lean = sin(t * 1.4f + seed) * 2f
    translate(cx + lean, cy + bob) {
        scale(scale, scale, Offset.Zero) {
            kOval(Color(0x44000000), -9f, 16f, 18f, 6f, outline = false)
            val sway = sin(t * 2.3f + seed) * 1.5f
            kRound(Color(0xFF3E2723), -6f + sway, 6f, 5.5f, 11f, 2f)
            kRound(Color(0xFF3E2723), 1f - sway, 6f, 5.5f, 11f, 2f)
            kRound(Color(0xFF1A120C), -7f, 15f, 7f, 3.5f, 2f)
            kRound(Color(0xFF1A120C), 0f, 15f, 7f, 3.5f, 2f)
            kRound(tunic, -9f, -8f, 18f, 16f, 3f)
            if (apron != null) kRound(apron, -7f, -2f, 14f, 12f, 2f)
            kRect(Color(0xFF2C1810), -9f, 4f, 18f, 2.5f)
            rotate(degrees = -armWave * 0.35f, pivot = Offset(-8f, -4f)) {
                kRound(Color(0xFFE0B090), -13f, -4f, 5.5f, 11f, 2f)
            }
            rotate(degrees = armWave, pivot = Offset(8f, -5f)) {
                kRound(Color(0xFFE0B090), 8f, -5f, 5.5f, 12f, 2f)
                kCircle(Color(0xFFE0B090), 3f, Offset(11f, 8f))
            }
            kCircle(Color(0xFFE8C4A8), 8f, Offset(0f, -16f))
            if (hat != null) {
                kRound(hat, -9f, -24f, 18f, 7f, 2f)
                kRect(hat, -11f, -18f, 22f, 3f)
            } else {
                kRound(Color(0xFF4E342E), -8f, -23f, 16f, 7f, 3f)
            }
            if (sin(t * 0.7f + seed) > 0.92f) {
                kRect(Color(0xFF1A1A1A), -4f, -16.5f, 3f, 1.2f, outline = false)
                kRect(Color(0xFF1A1A1A), 1f, -16.5f, 3f, 1.2f, outline = false)
            } else {
                drawCircle(Color(0xFF1A1A1A), 1.2f, Offset(-2.5f, -16.5f))
                drawCircle(Color(0xFF1A1A1A), 1.2f, Offset(2.5f, -16.5f))
            }
        }
    }
}

fun DrawScope.drawKenneyBird(x: Float, y: Float, t: Float, facingRight: Boolean = true) {
    val flap = sin(t * 14f) * 8f
    translate(x, y) {
        kOval(Color(0xFF455A64), -5f, -3f, 10f, 6f)
        kCircle(Color(0xFF607D8B), 2.5f, Offset(if (facingRight) 5f else -5f, -2f))
        rotate(degrees = flap, pivot = Offset.Zero) {
            kOval(Color(0xFF78909C), -2f, -6f, 8f, 4f)
        }
        rotate(degrees = -flap, pivot = Offset.Zero) {
            kOval(Color(0xFF90A4AE), -6f, -1f, 8f, 3.5f)
        }
    }
}

fun DrawScope.drawKenneyDog(x: Float, y: Float, t: Float, facingRight: Boolean) {
    val bob = sin(t * 8f) * 1.5f
    val leg = sin(t * 10f) * 3f
    translate(x, y + bob) {
        kOval(Color(0xFF8D6E63), -10f, -6f, 18f, 10f)
        kCircle(Color(0xFFA1887F), 5f, Offset(if (facingRight) 8f else -8f, -8f))
        kRound(Color(0xFF6D4C41), -6f, 2f + leg * 0.2f, 3.5f, 7f, 1.5f)
        kRound(Color(0xFF6D4C41), 2f, 2f - leg * 0.2f, 3.5f, 7f, 1.5f)
        val tail = Path().apply {
            moveTo(if (facingRight) -10f else 10f, -2f)
            quadraticTo(
                if (facingRight) -16f else 16f,
                -8f + sin(t * 12f) * 4f,
                if (facingRight) -12f else 12f,
                -1f,
            )
        }
        drawPath(tail, Color(0xFF6D4C41), style = Stroke(width = 3f))
        drawCircle(Color(0xFF1A1A1A), 1.2f, Offset(if (facingRight) 10f else -10f, -8f))
    }
}

fun DrawScope.drawKenneyCat(x: Float, y: Float, t: Float, facingRight: Boolean) {
    val bob = sin(t * 6f) * 1.2f
    translate(x, y + bob) {
        kOval(Color(0xFFFFB74D), -8f, -5f, 14f, 8f)
        kCircle(Color(0xFFFFCC80), 4.5f, Offset(if (facingRight) 6f else -6f, -7f))
        val ear = Path().apply {
            moveTo(if (facingRight) 3f else -3f, -10f)
            lineTo(if (facingRight) 6f else -6f, -15f)
            lineTo(if (facingRight) 8f else -8f, -9f)
            close()
        }
        kPath(Color(0xFFFFB74D), ear)
        val ear2 = Path().apply {
            moveTo(if (facingRight) 5f else -5f, -10f)
            lineTo(if (facingRight) 9f else -9f, -14f)
            lineTo(if (facingRight) 10f else -10f, -8f)
            close()
        }
        kPath(Color(0xFFFFB74D), ear2)
        val tail = Path().apply {
            moveTo(if (facingRight) -8f else 8f, -1f)
            quadraticTo(
                if (facingRight) -14f else 14f,
                -10f + sin(t * 5f) * 3f,
                if (facingRight) -6f else 6f,
                -6f,
            )
        }
        drawPath(tail, Color(0xFFFFA726), style = Stroke(width = 2.5f))
        drawCircle(Color(0xFF1A1A1A), 1f, Offset(if (facingRight) 7.5f else -7.5f, -7f))
    }
}

fun DrawScope.drawKenneyChicken(x: Float, y: Float, t: Float, facingRight: Boolean = true) {
    val bob = sin(t * 9f) * 1.5f
    val peck = (sin(t * 4f).coerceAtMost(0.3f)) * 4f
    translate(x, y + bob) {
        kOval(Color(0xFFFFF8E1), -5f, -4f, 10f, 8f)
        kCircle(Color(0xFFFFF8E1), 3.5f, Offset(if (facingRight) 5f else -5f, -5f - peck))
        kCircle(Color(0xFFFF7043), 1.5f, Offset(if (facingRight) 7f else -7f, -5f - peck))
        kRound(Color(0xFFFFA726), -2f, 2f, 2.5f, 5f, 1f)
        kRound(Color(0xFFFFA726), 1f, 2f, 2.5f, 5f, 1f)
        rotate(degrees = sin(t * 11f) * 10f, pivot = Offset(0f, -1f)) {
            kOval(Color(0xFFFFECB3), -1f, -3f, 7f, 4f)
        }
    }
}

fun DrawScope.drawKenneyButterfly(x: Float, y: Float, t: Float) {
    val flap = sin(t * 16f) * 20f
    translate(x, y) {
        rotate(degrees = flap, pivot = Offset.Zero) {
            kOval(Color(0xFFFF8A65), -6f, -3f, 6f, 5f)
        }
        rotate(degrees = -flap, pivot = Offset.Zero) {
            kOval(Color(0xFFFFAB91), 0f, -3f, 6f, 5f)
        }
        drawCircle(Color(0xFF5D4037), 1.5f, Offset.Zero)
    }
}

fun DrawScope.drawKenneyHorse(x: Float, y: Float, t: Float) {
    val bob = sin(t * 2.2f) * 1.5f
    val headNod = sin(t * 1.8f) * 2f
    translate(x, y + bob) {
        kOval(Color(0x55000000), -22f, -6f, 48f, 14f, outline = false)
        kOval(Color(0xFF6B4B2E), -18f, -34f, 36f, 22f)
        kRound(Color(0xFF6B4B2E), -16f, -18f, 6f, 18f, 2f)
        kRound(Color(0xFF6B4B2E), -4f, -18f, 6f, 18f, 2f)
        kRound(Color(0xFF6B4B2E), 6f, -18f, 6f, 18f, 2f)
        kRound(Color(0xFF6B4B2E), 14f, -18f, 6f, 18f, 2f)
        kOval(Color(0xFF5A3A22), 12f, -48f + headNod, 18f, 16f)
        drawLine(Color(0xFF4A3524), Offset(28f, -42f + headNod), Offset(36f, -30f + headNod), 3f)
        drawCircle(Color(0xFF3A322C), 2f, Offset(26f, -44f + headNod))
        val tail = Path().apply {
            moveTo(-18f, -28f)
            quadraticTo(-28f, -20f + sin(t * 4f) * 4f, -22f, -8f)
        }
        drawPath(tail, Color(0xFF4A3524), style = Stroke(3.5f))
    }
}
