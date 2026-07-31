package com.medieval.village.ui.place

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.medieval.village.game.Facing
import com.medieval.village.model.InteriorNpc
import com.medieval.village.model.InteriorNpcCatalog
import com.medieval.village.model.InteriorNpcKind
import com.medieval.village.model.Mercenary
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.village.drawKenneyNpc
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawMercenary
import com.medieval.village.ui.village.drawStar
import com.medieval.village.ui.village.kCircle
import com.medieval.village.ui.village.kOval
import com.medieval.village.ui.village.kRect
import com.medieval.village.ui.village.kRound

/** 장소별 실내 배경 + 주인공/용병/NPC 연출. */
fun DrawScope.drawInterior(
    id: PlaceId,
    w: Float,
    h: Float,
    companions: List<Mercenary> = emptyList(),
    animTime: Float = 0f,
    speechNpcId: String? = null,
    speechText: String? = null,
) {
    when (id) {
        PlaceId.DUNGEON -> dungeonMouth(w, h)
        PlaceId.ARENA -> arenaYard(w, h)
        PlaceId.MERCENARY -> campInterior(w, h)
        else -> roomBase(id, w, h)
    }

    val npcs = InteriorNpcCatalog.forPlace(id)
    npcs.forEachIndexed { index, npc ->
        drawInteriorNpcSprite(npc, w * npc.fx, h * npc.fy, animTime, index)
        if (speechNpcId == npc.id && !speechText.isNullOrBlank()) {
            drawSpeechBubble(w * npc.fx, h * npc.fy - h * 0.28f, speechText, w)
        }
    }

    companions.forEachIndexed { index, mercenary ->
        drawMercenary(
            mercenary = mercenary,
            x = w * (0.30f + index * 0.12f),
            y = h * (0.90f - index * 0.02f),
            facing = Facing.RIGHT,
            walking = false,
            animTime = animTime + index * 0.35f,
        )
    }

    drawHero(
        x = w * 0.18f,
        y = h * 0.90f,
        facing = Facing.RIGHT,
        walking = false,
        animTime = animTime,
    )
}

private fun DrawScope.drawInteriorNpcSprite(
    npc: InteriorNpc,
    x: Float,
    y: Float,
    t: Float,
    seed: Int,
) {
    val (tunic, apron, hat) = when (npc.kind) {
        InteriorNpcKind.KEEPER -> when (npc.placeId) {
            PlaceId.SHOP -> Triple(Color(0xFF5D8A4A), Color(0xFFE8D5A8), Color(0xFF8B5A2B))
            PlaceId.WEAPON_SHOP, PlaceId.BLACKSMITH -> Triple(Color(0xFF6B5344), Color(0xFF9E9E9E), Color(0xFF455A64))
            PlaceId.HOSPITAL -> Triple(Color(0xFFECEFF1), Color(0xFFFFFFFF), Color(0xFFB5453A))
            PlaceId.CHURCH -> Triple(Color(0xFF5C6BC0), null, Color(0xFF37474F))
            PlaceId.INN, PlaceId.PUB -> Triple(Color(0xFF8D6E63), Color(0xFFD7CCC8), Color(0xFF5D4037))
            PlaceId.MAGIC_SCHOOL -> Triple(Color(0xFF7E57C2), null, Color(0xFF4527A0))
            PlaceId.ARENA, PlaceId.MERCENARY -> Triple(Color(0xFF78909C), null, Color(0xFF37474F))
            else -> Triple(Color(0xFF8D6E63), null, Color(0xFF5D4037))
        }
        InteriorNpcKind.HELPER -> when (npc.placeId) {
            PlaceId.HOSPITAL -> Triple(Color(0xFFBBDEFB), Color(0xFFFFFFFF), null)
            PlaceId.CHURCH -> Triple(Color(0xFFD1C4E9), null, Color(0xFF5E35B1))
            PlaceId.SHOP -> Triple(Color(0xFFA5D6A7), Color(0xFFFFF8E1), null)
            else -> Triple(Color(0xFFBCAAA4), Color(0xFFEFEBE9), null)
        }
        InteriorNpcKind.VISITOR -> Triple(Color(0xFF90A4AE), null, Color(0xFF546E7A))
    }
    drawKenneyNpc(
        cx = x,
        cy = y - 10f,
        t = t,
        seed = seed + npc.id.hashCode() % 17,
        tunic = tunic,
        apron = apron,
        hat = hat,
        scale = 1.15f,
        wave = true,
    )
}

private fun DrawScope.drawSpeechBubble(cx: Float, cy: Float, text: String, canvasW: Float) {
    val paint = android.graphics.Paint().apply {
        color = Color(0xFF1B120A).toArgb()
        textSize = 22f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val width = (paint.measureText(text) + 28f).coerceIn(80f, canvasW * 0.55f)
    val left = (cx - width / 2f).coerceIn(6f, canvasW - width - 6f)
    kRound(Color(0xFFF7EFD8), left, cy - 28f, width, 36f, 10f, stroke = 3f)
    val tip = Path().apply {
        moveTo(cx - 8f, cy + 6f)
        lineTo(cx, cy + 16f)
        lineTo(cx + 8f, cy + 6f)
        close()
    }
    drawPath(tip, Color(0xFFF7EFD8))
    drawPath(tip, Color(0xFF2B2118), style = Stroke(2.5f))
    drawContext.canvas.nativeCanvas.drawText(text, left + width / 2f, cy - 4f, paint)
}

private fun DrawScope.roomBase(id: PlaceId, w: Float, h: Float) {
    val wallColor = when (id) {
        PlaceId.CHURCH -> Color(0xFFD7D2C2)
        PlaceId.HOSPITAL -> Color(0xFFE4E7E2)
        PlaceId.MAGIC_SCHOOL -> Color(0xFF3C3560)
        PlaceId.BLACKSMITH -> Color(0xFF4A423A)
        else -> Color(0xFFC8AC7E)
    }
    val floorColor = when (id) {
        PlaceId.CHURCH, PlaceId.HOSPITAL -> Color(0xFF9E9A90)
        PlaceId.MAGIC_SCHOOL -> Color(0xFF2A2440)
        else -> Color(0xFF7A5533)
    }
    val floorY = h * 0.62f

    kRect(wallColor, 0f, 0f, w, floorY, outline = false)
    kRect(floorColor, 0f, floorY, w, h - floorY, outline = false)
    var x = 0f
    while (x < w) {
        drawLine(Color(0x22000000), Offset(x, floorY), Offset(x - h * 0.10f, h), strokeWidth = 2f)
        x += w * 0.11f
    }
    drawLine(Color(0x44000000), Offset(0f, floorY), Offset(w, floorY), strokeWidth = 3f)

    when (id) {
        PlaceId.HOME -> homeProps(w, h, floorY)
        PlaceId.SHOP -> shopProps(w, h, floorY)
        PlaceId.WEAPON_SHOP -> weaponProps(w, h, floorY)
        PlaceId.HOSPITAL -> hospitalProps(w, h, floorY)
        PlaceId.CHURCH -> churchProps(w, h, floorY)
        PlaceId.INN -> innProps(w, h, floorY)
        PlaceId.PUB -> innProps(w, h, floorY)
        PlaceId.BLACKSMITH -> forgeProps(w, h, floorY)
        PlaceId.MAGIC_SCHOOL -> magicProps(w, h, floorY)
        else -> Unit
    }
}

private fun DrawScope.window(x: Float, y: Float, w: Float, h: Float) {
    kRect(Color(0xFF6E8FC4), x, y, w, h, stroke = 3.5f)
    drawLine(Color(0xFF4A3524), Offset(x + w / 2f, y), Offset(x + w / 2f, y + h), strokeWidth = 3f)
    drawLine(Color(0xFF4A3524), Offset(x, y + h / 2f), Offset(x + w, y + h / 2f), strokeWidth = 3f)
}

private fun DrawScope.homeProps(w: Float, h: Float, floorY: Float) {
    window(w * 0.10f, h * 0.14f, w * 0.16f, h * 0.26f)
    kRect(Color(0xFF6B4B2E), w * 0.60f, floorY - h * 0.06f, w * 0.33f, h * 0.30f, stroke = 3f)
    kRect(Color(0xFFE6DCC4), w * 0.62f, floorY - h * 0.02f, w * 0.29f, h * 0.18f, stroke = 2.5f)
    kRect(Color(0xFF8C2F28), w * 0.62f, floorY + h * 0.06f, w * 0.29f, h * 0.10f, stroke = 2.5f)
    kRect(Color(0xFF7D766C), w * 0.36f, h * 0.22f, w * 0.16f, h * 0.40f, stroke = 3f)
    kRect(Color(0xFF2B211A), w * 0.39f, h * 0.38f, w * 0.10f, h * 0.24f, outline = false)
    kCircle(Color(0xFFE8843A), h * 0.06f, Offset(w * 0.44f, h * 0.55f), stroke = 2.5f)
    kCircle(Color(0xFFF7D46A), h * 0.03f, Offset(w * 0.44f, h * 0.56f), stroke = 2f)
    kRect(Color(0xFF8A5A2B), w * 0.30f, floorY + h * 0.16f, w * 0.12f, h * 0.16f, stroke = 3f)
}

private fun DrawScope.shopProps(w: Float, h: Float, floorY: Float) {
    for (i in 0..2) {
        val sy = h * (0.14f + i * 0.15f)
        kRect(Color(0xFF6B4B2E), w * 0.36f, sy, w * 0.56f, h * 0.03f, stroke = 2.5f)
        for (j in 0..4) {
            val cx = w * (0.40f + j * 0.11f)
            val c = listOf(
                Color(0xFFC0392B), Color(0xFFE0A430), Color(0xFF6A8F3C),
                Color(0xFF4A7FC1), Color(0xFFB07CC6)
            )[(i + j) % 5]
            kCircle(c, h * 0.035f, Offset(cx, sy - h * 0.04f), stroke = 2f)
        }
    }
    kRect(Color(0xFF8A5A2B), w * 0.42f, floorY + h * 0.04f, w * 0.42f, h * 0.24f, stroke = 3.5f)
    kRect(Color(0xFF6B4B2E), w * 0.42f, floorY + h * 0.04f, w * 0.42f, h * 0.04f, stroke = 2.5f)
}

private fun DrawScope.weaponProps(w: Float, h: Float, floorY: Float) {
    kRect(Color(0xFF6B4B2E), w * 0.38f, h * 0.14f, w * 0.54f, h * 0.04f, stroke = 3f)
    val steel = Color(0xFFBFC5CC)
    for (j in 0..3) {
        val x = w * (0.44f + j * 0.13f)
        kRect(steel, x - 3f, h * 0.18f, 6f, h * 0.26f, stroke = 2.5f)
        kRect(Color(0xFF7A5230), x - 9f, h * 0.44f, 18f, h * 0.05f, stroke = 2.5f)
    }
    val shield = Path().apply {
        moveTo(w * 0.86f, h * 0.56f)
        lineTo(w * 0.96f, h * 0.60f)
        lineTo(w * 0.91f, h * 0.80f)
        lineTo(w * 0.81f, h * 0.60f)
        close()
    }
    drawPath(shield, Color(0xFF9B3B2E))
    drawPath(shield, Color(0xFFD9A441), style = Stroke(width = 3f))
    kRect(Color(0xFF8A5A2B), w * 0.40f, floorY + h * 0.08f, w * 0.34f, h * 0.20f, stroke = 3.5f)
}

private fun DrawScope.hospitalProps(w: Float, h: Float, floorY: Float) {
    kRect(Color(0xFFB5453A), w * 0.46f, h * 0.10f, w * 0.05f, h * 0.24f, stroke = 3f)
    kRect(Color(0xFFB5453A), w * 0.40f, h * 0.17f, w * 0.17f, h * 0.05f, stroke = 3f)
    kRect(Color(0xFFB9B3A6), w * 0.62f, floorY - h * 0.04f, w * 0.32f, h * 0.28f, stroke = 3f)
    kRect(Color(0xFFFFFFFF), w * 0.64f, floorY, w * 0.28f, h * 0.16f, stroke = 2.5f)
    kRect(Color(0xFF7FA7C9), w * 0.64f, floorY + h * 0.08f, w * 0.28f, h * 0.09f, stroke = 2.5f)
    kRect(Color(0xFF8A7A62), w * 0.34f, h * 0.36f, w * 0.16f, h * 0.26f, stroke = 3f)
    kCircle(Color(0xFF6FBF73), h * 0.025f, Offset(w * 0.38f, h * 0.44f), stroke = 2f)
    kCircle(Color(0xFFD86A6A), h * 0.025f, Offset(w * 0.45f, h * 0.44f), stroke = 2f)
}

private fun DrawScope.churchProps(w: Float, h: Float, floorY: Float) {
    listOf(0.34f, 0.60f, 0.86f).forEach { fx ->
        val x = w * fx - w * 0.05f
        drawArc(
            Color(0xFF6E8FC4), 180f, 180f, true,
            topLeft = Offset(x, h * 0.08f), size = Size(w * 0.10f, w * 0.10f)
        )
        kRect(Color(0xFF8B6FC0), x, h * 0.08f + w * 0.05f, w * 0.10f, h * 0.24f, stroke = 3f)
    }
    kRect(Color(0xFFE6DCC4), w * 0.56f, floorY + h * 0.02f, w * 0.30f, h * 0.22f, stroke = 3.5f)
    kRect(Color(0xFFC9A227), w * 0.69f, floorY - h * 0.14f, w * 0.02f, h * 0.16f, stroke = 2.5f)
    kRect(Color(0xFFC9A227), w * 0.65f, floorY - h * 0.10f, w * 0.10f, h * 0.02f, stroke = 2.5f)
    listOf(0.58f, 0.84f).forEach { fx ->
        kRect(Color(0xFFF2E4C6), w * fx, floorY - h * 0.06f, w * 0.012f, h * 0.08f, stroke = 2f)
        kCircle(Color(0xFFF7D46A), h * 0.016f, Offset(w * fx + w * 0.006f, floorY - h * 0.07f), stroke = 1.5f)
    }
}

private fun DrawScope.innProps(w: Float, h: Float, floorY: Float) {
    kRect(Color(0xFF6B4B2E), w * 0.42f, floorY - h * 0.02f, w * 0.50f, h * 0.30f, stroke = 3.5f)
    kRect(Color(0xFF8A5A2B), w * 0.42f, floorY - h * 0.02f, w * 0.50f, h * 0.05f, stroke = 2.5f)
    kRect(Color(0xFF7A5230), w * 0.36f, h * 0.30f, w * 0.12f, h * 0.20f, stroke = 3f)
    listOf(0.52f, 0.62f, 0.72f).forEach { fx ->
        kRect(Color(0xFFD9CDB4), w * fx, floorY - h * 0.10f, w * 0.05f, h * 0.08f, stroke = 2.5f)
    }
    for (i in 0..3) {
        kRect(Color(0xFF5A3F27), w * 0.80f, h * (0.20f + i * 0.09f), w * 0.18f, h * 0.05f, stroke = 2.5f)
    }
}

private fun DrawScope.forgeProps(w: Float, h: Float, floorY: Float) {
    kRect(Color(0xFF6E655B), w * 0.56f, h * 0.14f, w * 0.34f, h * 0.44f, stroke = 3.5f)
    kRect(Color(0xFF17110C), w * 0.61f, h * 0.24f, w * 0.24f, h * 0.30f, outline = false)
    kCircle(Color(0xFFE8843A), h * 0.09f, Offset(w * 0.73f, h * 0.44f), stroke = 3f)
    kCircle(Color(0xFFF7D46A), h * 0.045f, Offset(w * 0.73f, h * 0.45f), stroke = 2f)
    kRect(Color(0xFF4E4A45), w * 0.40f, floorY + h * 0.06f, w * 0.18f, h * 0.07f, stroke = 3f)
    kRect(Color(0xFF3A3733), w * 0.45f, floorY + h * 0.13f, w * 0.08f, h * 0.13f, stroke = 2.5f)
    kRect(Color(0xFF6B4B2E), w * 0.30f, floorY + h * 0.02f, w * 0.015f, h * 0.18f, stroke = 2f)
    kRect(Color(0xFF7E858C), w * 0.26f, floorY, w * 0.09f, h * 0.05f, stroke = 2.5f)
}

private fun DrawScope.magicProps(w: Float, h: Float, floorY: Float) {
    kRect(Color(0xFF4A3524), w * 0.34f, h * 0.08f, w * 0.26f, h * 0.52f, stroke = 3.5f)
    for (i in 0..2) {
        val sy = h * (0.14f + i * 0.16f)
        for (j in 0..4) {
            val c = listOf(
                Color(0xFFC0392B), Color(0xFFE0A430), Color(0xFF4A7FC1),
                Color(0xFF6A8F3C), Color(0xFFB07CC6)
            )[(i * 2 + j) % 5]
            kRect(c, w * (0.36f + j * 0.045f), sy, w * 0.03f, h * 0.11f, stroke = 2f)
        }
    }
    kOval(Color(0x664A7FC1), w * 0.74f - h * 0.14f, floorY + h * 0.16f - h * 0.14f, h * 0.28f, h * 0.28f, outline = false)
    drawCircle(Color(0xAA7FB6E8), h * 0.14f, Offset(w * 0.74f, floorY + h * 0.16f), style = Stroke(3f))
    drawStar(w * 0.74f, floorY + h * 0.16f, h * 0.10f, Color(0x99CFE4FF))
    kRect(Color(0xFF6B4B2E), w * 0.64f, h * 0.44f, w * 0.07f, h * 0.16f, stroke = 2.5f)
    kCircle(Color(0xFF9B7FD4), h * 0.06f, Offset(w * 0.675f, h * 0.40f), stroke = 2.5f)
}

private fun DrawScope.dungeonMouth(w: Float, h: Float) {
    kRect(Color(0xFF2A2621), 0f, 0f, w, h, outline = false)
    val arch = Path().apply {
        moveTo(w * 0.12f, h)
        cubicTo(w * 0.18f, h * 0.10f, w * 0.82f, h * 0.10f, w * 0.88f, h)
        close()
    }
    drawPath(arch, Color(0xFF56504A))
    val hole = Path().apply {
        moveTo(w * 0.26f, h)
        cubicTo(w * 0.30f, h * 0.26f, w * 0.70f, h * 0.26f, w * 0.74f, h)
        close()
    }
    drawPath(hole, Color(0xFF0D0B09))
    for (i in 0..3) {
        kRect(
            Color(0xFF3E3932),
            w * (0.34f + i * 0.02f),
            h * (0.72f + i * 0.07f),
            w * (0.32f - i * 0.04f),
            h * 0.04f,
            stroke = 2.5f
        )
    }
    listOf(0.20f, 0.80f).forEach { fx ->
        kRect(Color(0xFF5A3A22), w * fx - 4f, h * 0.42f, 8f, h * 0.26f, stroke = 2.5f)
        kCircle(Color(0xFFE8843A), h * 0.055f, Offset(w * fx, h * 0.40f), stroke = 2.5f)
        kCircle(Color(0xFFF9DE85), h * 0.026f, Offset(w * fx, h * 0.41f), stroke = 2f)
    }
}

private fun DrawScope.arenaYard(w: Float, h: Float) {
    kRect(Color(0xFF8FB8D8), 0f, 0f, w, h * 0.42f, outline = false)
    kCircle(Color(0xFFF7E9A8), h * 0.10f, Offset(w * 0.84f, h * 0.12f), stroke = 3f)
    kRect(Color(0xFFD9BE8B), 0f, h * 0.42f, w, h * 0.58f, outline = false)
    kOval(Color(0xFFC7A876), w * 0.06f, h * 0.50f, w * 0.88f, h * 0.44f, stroke = 3.5f)
    for (i in 0..11) {
        val x = w * (0.03f + i * 0.086f)
        kRect(Color(0xFF7A5230), x, h * 0.34f, w * 0.018f, h * 0.14f, stroke = 2.5f)
    }
    kRect(Color(0xFF7A5230), 0f, h * 0.38f, w, h * 0.02f, stroke = 2.5f)
    kRect(Color(0xFF6B4B2E), w * 0.70f, h * 0.50f, w * 0.02f, h * 0.36f, stroke = 2.5f)
    kRect(Color(0xFF6B4B2E), w * 0.63f, h * 0.58f, w * 0.16f, h * 0.02f, stroke = 2.5f)
    kCircle(Color(0xFFD9C08A), h * 0.06f, Offset(w * 0.71f, h * 0.52f), stroke = 2.5f)
}

private fun DrawScope.campInterior(w: Float, h: Float) {
    kRect(Color(0xFF8FB8D8), 0f, 0f, w, h * 0.46f, outline = false)
    kRect(Color(0xFF6F9A54), 0f, h * 0.46f, w, h * 0.54f, outline = false)
    listOf(0.42f to 0.9f, 0.68f to 0.72f, 0.88f to 0.6f).forEach { (fx, sc) ->
        val bx = w * fx
        val bw = w * 0.20f * sc
        val bh = h * 0.42f * sc
        val tent = Path().apply {
            moveTo(bx - bw, h * 0.86f)
            lineTo(bx, h * 0.86f - bh)
            lineTo(bx + bw, h * 0.86f)
            close()
        }
        drawPath(tent, Color(0xFF8B9668))
        drawPath(tent, Color(0xFF2B2118), style = Stroke(3.5f))
        val flap = Path().apply {
            moveTo(bx - bw * 0.30f, h * 0.86f)
            lineTo(bx, h * 0.86f - bh * 0.55f)
            lineTo(bx + bw * 0.30f, h * 0.86f)
            close()
        }
        drawPath(flap, Color(0xFF2F3324))
    }
    kCircle(Color(0xFF4A3524), h * 0.07f, Offset(w * 0.52f, h * 0.92f), stroke = 3f)
    kCircle(Color(0xFFE8843A), h * 0.05f, Offset(w * 0.52f, h * 0.90f), stroke = 2.5f)
    kCircle(Color(0xFFF9DE85), h * 0.025f, Offset(w * 0.52f, h * 0.90f), stroke = 2f)
    kRect(Color(0xFF5A4231), w * 0.34f, h * 0.30f, 5f, h * 0.52f, stroke = 2.5f)
    val flag = Path().apply {
        moveTo(w * 0.34f + 5f, h * 0.32f)
        lineTo(w * 0.46f, h * 0.38f)
        lineTo(w * 0.34f + 5f, h * 0.44f)
        close()
    }
    drawPath(flag, Color(0xFF9B3B2E))
    drawPath(flag, Color(0xFF2B2118), style = Stroke(3f))
}
