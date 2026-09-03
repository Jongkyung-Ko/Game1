package com.medieval.village.ui.place

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.medieval.village.game.Facing
import com.medieval.village.game.SpecialSkillFx
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** 스킬마다 다른 화려한 캔버스 이펙트. */
fun DrawScope.drawUniqueSkillBurst(fx: SpecialSkillFx) {
    val t = fx.progress
    val fade = (1f - t * 0.55f).coerceIn(0.25f, 1f)
    val c = Offset(fx.x, fx.y)
    val s = fx.scale
    val ang = facingAngle(fx.facing)
    when (fx.skillId) {
        "adv_smash" -> smashBurst(c, t, fade, s, Color(0xFFFFC14A), Color(0xFFFF7A2A))
        "adv_flurry" -> slashFan(c, ang, t, fade, s, 3, Color(0xFFE8F0FF), Color(0xFF9AD0FF))
        "adv_charge" -> chargeRush(c, ang, t, fade, s, Color(0xFFFFE08A), Color(0xFFFF9A3A))
        "adv_shot" -> arrowFlash(c, ang, t, fade, s, Color(0xFFFFE29A), 1)
        "adv_bolt" -> fireNova(c, t, fade, s)
        "adv_finisher" -> beamCleave(c, ang, t, fade, s, Color(0xFFFFF4C8), Color(0xFFFFB020))
        "war_kill" -> critStar(c, t, fade, s, Color(0xFFFF4A4A))
        "war_bash" -> shockRing(c, t, fade, s, Color(0xFFC8B090), Color(0xFFFFE8B0))
        "war_spin" -> spinCrescents(c, t, fade, s, Color(0xFFFFE9A8))
        "war_rush" -> chargeRush(c, ang, t, fade, s, Color(0xFFFFD060), Color(0xFFE05020))
        "war_rage" -> smashBurst(c, t, fade, s * 1.15f, Color(0xFFFF3030), Color(0xFFFFB040))
        "war_quake" -> quakeBurst(c, t, fade, s)
        "rog_assassinate" -> daggerPierce(c, ang, t, fade, s, Color(0xFFC080FF))
        "rog_stab" -> daggerPierce(c, ang, t, fade, s, Color(0xFF80E0FF))
        "rog_vital" -> slashFan(c, ang, t, fade, s, 2, Color(0xFFFF80A0), Color(0xFFFFD0E0))
        "rog_smoke" -> smokeCloud(c, t, fade, s)
        "rog_dual" -> slashFan(c, ang, t, fade, s, 4, Color(0xFFE8D0FF), Color(0xFFB070FF))
        "rog_execute" -> critStar(c, t, fade, s * 1.2f, Color(0xFFFF2080))
        "mag_blast" -> fireNova(c, t, fade, s * 1.1f)
        "mag_orb" -> orbPulse(c, t, fade, s, Color(0xFF9B6CFF))
        "mag_meteor" -> meteorBloom(c, t, fade, s)
        "mag_ice" -> iceBurst(c, t, fade, s)
        "mag_chain" -> lightningWeb(c, ang, t, fade, s)
        "mag_ruin" -> holyCross(c, t, fade, s, Color(0xFFFFE080), Color(0xFFFF60C0))
        "pal_smite" -> holyCross(c, t, fade, s, Color(0xFFFFF0B0), Color(0xFFFFD060))
        "pal_judge" -> beamCleave(c, ang, t, fade, s, Color(0xFFFFF8D0), Color(0xFFFFC040))
        "pal_wrath" -> smashBurst(c, t, fade, s * 1.2f, Color(0xFFFFF0A0), Color(0xFFFF8030))
        "pal_slash" -> slashFan(c, ang, t, fade, s, 2, Color(0xFFFFF4C0), Color(0xFFFFE080))
        "pal_guard" -> shockRing(c, t, fade, s, Color(0xFF80C8FF), Color(0xFFFFF0C0))
        "pal_holy" -> holyCross(c, t, fade, s * 1.25f, Color(0xFFFFFFFF), Color(0xFFFFE080))
        "arc_shot" -> arrowFlash(c, ang, t, fade, s, Color(0xFFFFE29A), 1)
        "arc_double" -> arrowFlash(c, ang, t, fade, s, Color(0xFFFFC14A), 2)
        "arc_snipe" -> snipeLine(c, ang, t, fade, s)
        "arc_pierce" -> pierceBolt(c, ang, t, fade, s)
        "arc_rain" -> arrowRain(c, t, fade, s)
        "arc_storm" -> arrowStorm(c, t, fade, s)
        else -> smashBurst(c, t, fade, s, Color(0xFFFFE29A), Color(0xFFFFC14A))
    }
}

private fun facingAngle(facing: Facing): Float = when (facing) {
    Facing.RIGHT -> 0f
    Facing.DOWN -> 90f
    Facing.LEFT -> 180f
    Facing.UP -> 270f
}

private fun DrawScope.smashBurst(c: Offset, t: Float, fade: Float, s: Float, hot: Color, core: Color) {
    val r = (28f + t * 78f) * s
    drawCircle(hot.copy(alpha = 0.22f * fade), r, c)
    drawCircle(core.copy(alpha = 0.5f * fade), r * 0.55f, c)
    drawCircle(Color.White.copy(alpha = 0.75f * fade), r * 0.22f, c)
    repeat(10) { i ->
        val a = (i * 36f + t * 40f) * (PI.toFloat() / 180f)
        val len = r * (0.55f + (i % 3) * 0.12f)
        drawLine(
            hot.copy(alpha = 0.7f * fade),
            c,
            Offset(c.x + cos(a) * len, c.y + sin(a) * len),
            3.2f * s,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.slashFan(c: Offset, ang: Float, t: Float, fade: Float, s: Float, blades: Int, edge: Color, glow: Color) {
    rotate(ang, c) {
        repeat(blades) { i ->
            val sweep = 70f + i * 18f
            val start = -sweep / 2f + t * 30f + i * 8f
            val r = (48f + i * 14f + t * 26f) * s
            drawArc(
                glow.copy(alpha = 0.28f * fade),
                start, sweep, false,
                Offset(c.x - r, c.y - r), Size(r * 2, r * 2),
                style = Stroke(14f * s * fade, cap = StrokeCap.Round),
            )
            drawArc(
                edge.copy(alpha = 0.9f * fade),
                start + 8f, sweep - 16f, false,
                Offset(c.x - r * 0.92f, c.y - r * 0.92f), Size(r * 1.84f, r * 1.84f),
                style = Stroke(4.5f * s, cap = StrokeCap.Round),
            )
        }
    }
}

private fun DrawScope.chargeRush(c: Offset, ang: Float, t: Float, fade: Float, s: Float, a: Color, b: Color) {
    rotate(ang, c) {
        val w = (90f + t * 70f) * s
        val h = (22f + (1f - t) * 10f) * s
        drawOval(a.copy(alpha = 0.28f * fade), Offset(c.x - 8f, c.y - h), Size(w, h * 2))
        drawOval(b.copy(alpha = 0.55f * fade), Offset(c.x + w * 0.15f, c.y - h * 0.55f), Size(w * 0.7f, h * 1.1f))
        drawCircle(Color.White.copy(alpha = 0.8f * fade), 7f * s, Offset(c.x + w * 0.78f, c.y))
        repeat(5) { i ->
            val x = c.x + i * 16f * s
            drawLine(a.copy(alpha = 0.45f * fade), Offset(x, c.y - 16f * s), Offset(x + 22f * s, c.y), 2.4f)
            drawLine(a.copy(alpha = 0.45f * fade), Offset(x, c.y + 16f * s), Offset(x + 22f * s, c.y), 2.4f)
        }
    }
}

private fun DrawScope.arrowFlash(c: Offset, ang: Float, t: Float, fade: Float, s: Float, col: Color, count: Int) {
    rotate(ang, c) {
        repeat(count) { i ->
            val y = c.y + (i - (count - 1) / 2f) * 14f * s
            val len = (70f + t * 50f) * s
            drawLine(col.copy(alpha = 0.85f * fade), Offset(c.x - 10f, y), Offset(c.x + len, y), 5f * s, StrokeCap.Round)
            drawCircle(Color.White.copy(alpha = 0.9f * fade), 5f * s, Offset(c.x + len, y))
        }
    }
}

private fun DrawScope.fireNova(c: Offset, t: Float, fade: Float, s: Float) {
    val r = (22f + t * 70f) * s
    drawCircle(Color(0x88FF4A10).copy(alpha = 0.35f * fade), r, c)
    drawCircle(Color(0xFFFF8A20).copy(alpha = 0.55f * fade), r * 0.62f, c)
    drawCircle(Color(0xFFFFF0A0).copy(alpha = 0.8f * fade), r * 0.28f, c)
    repeat(8) { i ->
        val a = (i * 45f + t * 80f) * (PI.toFloat() / 180f)
        drawCircle(
            Color(0xFFFF6A20).copy(alpha = 0.65f * fade),
            6f * s * (1f - t * 0.4f),
            Offset(c.x + cos(a) * r * 0.8f, c.y + sin(a) * r * 0.8f),
        )
    }
}

private fun DrawScope.beamCleave(c: Offset, ang: Float, t: Float, fade: Float, s: Float, edge: Color, core: Color) {
    rotate(ang, c) {
        val len = (120f + t * 50f) * s
        val w = (18f + (1f - t) * 10f) * s
        drawLine(core.copy(alpha = 0.35f * fade), Offset(c.x - 20f, c.y), Offset(c.x + len, c.y), w * 2.2f, StrokeCap.Round)
        drawLine(edge.copy(alpha = 0.9f * fade), Offset(c.x - 10f, c.y), Offset(c.x + len, c.y), w * 0.7f, StrokeCap.Round)
        drawCircle(Color.White.copy(alpha = 0.85f * fade), 10f * s, Offset(c.x + len * 0.15f, c.y))
    }
}

private fun DrawScope.critStar(c: Offset, t: Float, fade: Float, s: Float, col: Color) {
    val r = (36f + t * 40f) * s
    val path = Path()
    repeat(8) { i ->
        val a = (i * 45f + t * 25f) * (PI.toFloat() / 180f)
        val rad = if (i % 2 == 0) r else r * 0.42f
        val p = Offset(c.x + cos(a) * rad, c.y + sin(a) * rad)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    drawPath(path, col.copy(alpha = 0.55f * fade))
    drawCircle(Color.White.copy(alpha = 0.8f * fade), r * 0.22f, c)
}

private fun DrawScope.shockRing(c: Offset, t: Float, fade: Float, s: Float, ring: Color, core: Color) {
    val r = (20f + t * 72f) * s
    drawCircle(ring.copy(alpha = 0.55f * fade), r, c, style = Stroke(8f * s * fade))
    drawCircle(core.copy(alpha = 0.4f * fade), r * 0.55f, c, style = Stroke(4f * s))
    drawCircle(Color.White.copy(alpha = 0.7f * fade), 8f * s * (1f - t), c)
}

private fun DrawScope.spinCrescents(c: Offset, t: Float, fade: Float, s: Float, col: Color) {
    repeat(3) { i ->
        val r = (40f + i * 18f + t * 30f) * s
        drawArc(
            col.copy(alpha = 0.55f * fade),
            t * 220f + i * 40f, 110f, false,
            Offset(c.x - r, c.y - r), Size(r * 2, r * 2),
            style = Stroke(7f * s, cap = StrokeCap.Round),
        )
    }
    drawCircle(Color.White.copy(alpha = 0.55f * fade), 10f * s, c)
}

private fun DrawScope.quakeBurst(c: Offset, t: Float, fade: Float, s: Float) {
    repeat(3) { i ->
        val r = (18f + t * (70f + i * 22f)) * s
        drawCircle(Color(0xFFC07030).copy(alpha = 0.28f * fade), r, c, style = Stroke(6f * s))
    }
    smashBurst(c, t, fade, s, Color(0xFFFFB040), Color(0xFF8A4010))
}

private fun DrawScope.daggerPierce(c: Offset, ang: Float, t: Float, fade: Float, s: Float, col: Color) {
    rotate(ang, c) {
        val path = Path().apply {
            moveTo(c.x + (70f + t * 30f) * s, c.y)
            lineTo(c.x - 16f * s, c.y - 10f * s)
            lineTo(c.x - 8f * s, c.y)
            lineTo(c.x - 16f * s, c.y + 10f * s)
            close()
        }
        drawPath(path, col.copy(alpha = 0.75f * fade))
        drawCircle(Color.White.copy(alpha = 0.7f * fade), 5f * s, Offset(c.x + 20f * s, c.y))
    }
}

private fun DrawScope.smokeCloud(c: Offset, t: Float, fade: Float, s: Float) {
    repeat(7) { i ->
        val a = i * 51f * (PI.toFloat() / 180f)
        val r = (16f + (i % 3) * 10f + t * 28f) * s
        drawCircle(
            Color(0xAA4A4A58).copy(alpha = 0.4f * fade),
            r,
            Offset(c.x + cos(a) * 18f * s, c.y + sin(a) * 12f * s),
        )
    }
}

private fun DrawScope.orbPulse(c: Offset, t: Float, fade: Float, s: Float, col: Color) {
    val r = (20f + sin(t * 9f) * 8f + t * 20f) * s
    drawCircle(col.copy(alpha = 0.3f * fade), r * 1.4f, c)
    drawCircle(col.copy(alpha = 0.65f * fade), r, c)
    drawCircle(Color.White.copy(alpha = 0.85f * fade), r * 0.35f, Offset(c.x - 4f, c.y - 5f))
}

private fun DrawScope.meteorBloom(c: Offset, t: Float, fade: Float, s: Float) {
    fireNova(c, t, fade, s * 1.15f)
    rotate(-25f, c) {
        drawLine(Color(0xFFFFC060).copy(alpha = 0.7f * fade), Offset(c.x - 40f * s, c.y - 70f * s), c, 10f * s, StrokeCap.Round)
    }
}

private fun DrawScope.iceBurst(c: Offset, t: Float, fade: Float, s: Float) {
    val r = (24f + t * 52f) * s
    drawCircle(Color(0x669BE8FF).copy(alpha = 0.4f * fade), r, c)
    repeat(6) { i ->
        val a = (i * 60f) * (PI.toFloat() / 180f)
        val tip = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
        val path = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(c.x + cos(a + 0.25f) * r * 0.4f, c.y + sin(a + 0.25f) * r * 0.4f)
            lineTo(c.x + cos(a - 0.25f) * r * 0.4f, c.y + sin(a - 0.25f) * r * 0.4f)
            close()
        }
        drawPath(path, Color(0xCCE8FAFF).copy(alpha = 0.85f * fade))
    }
    drawCircle(Color.White.copy(alpha = 0.8f * fade), 8f * s, c)
}

private fun DrawScope.lightningWeb(c: Offset, ang: Float, t: Float, fade: Float, s: Float) {
    rotate(ang, c) {
        val path = Path().apply {
            moveTo(c.x - 20f * s, c.y)
            lineTo(c.x + 18f * s, c.y - 16f * s)
            lineTo(c.x + 36f * s, c.y + 8f * s)
            lineTo(c.x + 70f * s, c.y - 10f * s)
            lineTo(c.x + (110f + t * 30f) * s, c.y)
        }
        drawPath(path, Color(0xFFFFF4A0).copy(alpha = 0.9f * fade), style = Stroke(5.5f * s, cap = StrokeCap.Round))
        drawPath(path, Color.White.copy(alpha = 0.75f * fade), style = Stroke(2f * s, cap = StrokeCap.Round))
    }
    drawCircle(Color(0xAAFFF8C8).copy(alpha = 0.6f * fade), 16f * s, c)
}

private fun DrawScope.holyCross(c: Offset, t: Float, fade: Float, s: Float, arm: Color, glow: Color) {
    val len = (36f + t * 48f) * s
    drawCircle(glow.copy(alpha = 0.28f * fade), len * 0.9f, c)
    drawLine(arm.copy(alpha = 0.9f * fade), Offset(c.x - len, c.y), Offset(c.x + len, c.y), 6f * s, StrokeCap.Round)
    drawLine(arm.copy(alpha = 0.9f * fade), Offset(c.x, c.y - len), Offset(c.x, c.y + len), 6f * s, StrokeCap.Round)
    drawCircle(Color.White.copy(alpha = 0.85f * fade), 9f * s, c)
}

private fun DrawScope.snipeLine(c: Offset, ang: Float, t: Float, fade: Float, s: Float) {
    rotate(ang, c) {
        val len = (160f + t * 40f) * s
        drawLine(Color(0x66FFE080).copy(alpha = 0.4f * fade), Offset(c.x - 20f, c.y), Offset(c.x + len, c.y), 10f * s)
        drawLine(Color(0xFFFFF6C8).copy(alpha = 0.95f * fade), Offset(c.x, c.y), Offset(c.x + len, c.y), 2.6f * s, StrokeCap.Round)
        drawCircle(Color.White.copy(alpha = 0.9f * fade), 4f * s, Offset(c.x + len, c.y))
    }
}

private fun DrawScope.pierceBolt(c: Offset, ang: Float, t: Float, fade: Float, s: Float) {
    rotate(ang, c) {
        val len = (100f + t * 40f) * s
        drawLine(Color(0xAA40E0E8).copy(alpha = 0.5f * fade), Offset(c.x - 8f, c.y), Offset(c.x + len, c.y), 12f * s)
        drawLine(Color(0xFF8FF8FF).copy(alpha = 0.95f * fade), Offset(c.x, c.y), Offset(c.x + len, c.y), 4f * s, StrokeCap.Round)
        repeat(3) { i ->
            drawCircle(Color(0xCC8FF8FF).copy(alpha = 0.6f * fade), 6f * s, Offset(c.x + len * (0.3f + i * 0.22f), c.y))
        }
    }
}

private fun DrawScope.arrowRain(c: Offset, t: Float, fade: Float, s: Float) {
    repeat(9) { i ->
        val x = c.x + (i - 4) * 16f * s
        val y0 = c.y - 70f * s + t * 90f * s + (i % 3) * 10f
        drawLine(Color(0xFFFFE08A).copy(alpha = 0.8f * fade), Offset(x, y0), Offset(x + 6f, y0 + 28f * s), 3.2f * s, StrokeCap.Round)
        drawCircle(Color.White.copy(alpha = 0.7f * fade), 2.5f * s, Offset(x + 6f, y0 + 28f * s))
    }
}

private fun DrawScope.arrowStorm(c: Offset, t: Float, fade: Float, s: Float) {
    repeat(14) { i ->
        val a = (i * 26f + t * 90f) * (PI.toFloat() / 180f)
        val r0 = 18f * s
        val r1 = (70f + t * 36f) * s
        drawLine(
            Color(0xFFFFD060).copy(alpha = 0.75f * fade),
            Offset(c.x + cos(a) * r0, c.y + sin(a) * r0),
            Offset(c.x + cos(a) * r1, c.y + sin(a) * r1),
            3.4f * s,
            StrokeCap.Round,
        )
    }
    drawCircle(Color(0xAAFFE080).copy(alpha = 0.45f * fade), 22f * s, c)
}
