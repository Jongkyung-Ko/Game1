package com.medieval.village.ui.place

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.medieval.village.game.Facing
import com.medieval.village.model.InteriorNpc
import com.medieval.village.model.InteriorNpcCatalog
import com.medieval.village.model.InteriorNpcKind
import com.medieval.village.model.Mercenary
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawMercenary
import kotlin.math.sin

fun DrawScope.drawInterior(
    atlas: KenneyAtlas,
    art: CustomArt?,
    id: PlaceId,
    w: Float,
    h: Float,
    companions: List<Mercenary> = emptyList(),
    animTime: Float = 0f,
    speechNpcId: String? = null,
    speechText: String? = null,
) {
    drawInteriorBackground(atlas, id, w, h)

    InteriorNpcCatalog.forPlace(id).forEachIndexed { index, npc ->
        val bob = sin(animTime * 2.6f + index) * 2f
        val sprite = art?.npcSpriteOrNull(npcSpriteKey(npc))
        if (sprite != null) {
            drawCustomSprite(
                image = sprite,
                cx = w * npc.fx,
                footY = h * npc.fy + bob,
                worldHeight = h * 0.55f,
            )
        } else {
            drawCircle(Color(0xFFE7B98F), h * 0.06f, Offset(w * npc.fx, h * npc.fy - h * 0.28f + bob))
            drawRect(Color(0xFF3E6B8A), Offset(w * npc.fx - h * 0.05f, h * npc.fy - h * 0.22f + bob), Size(h * 0.10f, h * 0.22f))
        }
        if (speechNpcId == npc.id && !speechText.isNullOrBlank()) {
            drawSpeechBubble(w * npc.fx, h * npc.fy - h * 0.34f, speechText, w)
        }
    }

    val heroScale = (h * 0.58f) / 108f
    companions.forEachIndexed { index, mercenary ->
        drawMercenary(
            mercenary = mercenary,
            x = w * (0.30f + index * 0.12f),
            y = h * 0.90f,
            facing = Facing.RIGHT,
            walking = false,
            phase = animTime + index * 0.35f,
            scale = heroScale * 0.9f,
        )
    }
    drawHero(
        x = w * 0.18f,
        y = h * 0.90f,
        facing = Facing.RIGHT,
        walking = false,
        phase = animTime,
        scale = heroScale,
    )
}

private fun npcSpriteKey(npc: InteriorNpc): String = when (npc.placeId) {
    PlaceId.SHOP -> if (npc.kind == InteriorNpcKind.KEEPER) "shopkeeper" else "merchant"
    PlaceId.WEAPON_SHOP -> "blacksmith"
    PlaceId.HOSPITAL -> if (npc.kind == InteriorNpcKind.KEEPER) "doctor" else "teacher"
    PlaceId.CHURCH -> if (npc.kind == InteriorNpcKind.KEEPER) "mage" else "paladin"
    PlaceId.INN -> if (npc.kind == InteriorNpcKind.KEEPER) "merchant" else "chef"
    PlaceId.ARENA -> "warrior"
    PlaceId.BLACKSMITH -> "blacksmith"
    PlaceId.MAGIC_SCHOOL -> if (npc.kind == InteriorNpcKind.KEEPER) "mage" else "teacher"
    PlaceId.MERCENARY -> if (npc.kind == InteriorNpcKind.KEEPER) "warrior" else "rogue"
    PlaceId.HOME -> "farmer"
    PlaceId.PUB -> "merchant"
    PlaceId.DUNGEON -> "warrior"
}

private fun DrawScope.drawInteriorBackground(atlas: KenneyAtlas, id: PlaceId, w: Float, h: Float) {
    val tile = minOf(w, h) / 8f
    drawRect(Color(0xFF2B1C12), Offset.Zero, Size(w, h))
    drawRect(Color(0xFF3E2A1C), Offset(0f, 0f), Size(w, h * 0.36f))
    for (c in 0 until 12) {
        drawKenneyTile(atlas.dungeon, DungeonTiles.WALL, c * tile - tile * 0.2f, h * 0.30f, tile)
    }
    for (r in 0 until 6) {
        for (c in 0 until 12) {
            drawKenneyTile(
                atlas.town,
                TownTiles.PATH,
                c * tile - tile * 0.1f,
                h * 0.42f + r * tile * 0.85f,
                tile
            )
        }
    }
    val accent = when (id) {
        PlaceId.HOSPITAL -> Color(0x446090B0)
        PlaceId.CHURCH -> Color(0x44C9B27A)
        PlaceId.BLACKSMITH, PlaceId.WEAPON_SHOP -> Color(0x44A05030)
        PlaceId.MAGIC_SCHOOL -> Color(0x445060A0)
        else -> Color(0x33FFFFFF)
    }
    drawRect(accent, Offset(0f, h * 0.36f), Size(w, 8f))
}

private fun DrawScope.drawSpeechBubble(cx: Float, top: Float, text: String, roomW: Float) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (roomW * 0.045f).coerceIn(22f, 34f)
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val width = (paint.measureText(text) + 28f).coerceAtMost(roomW * 0.7f)
    val height = paint.textSize + 22f
    val left = (cx - width / 2f).coerceIn(8f, roomW - width - 8f)
    drawRoundRect(
        Color(0xF0FFF8E7),
        Offset(left, top - height),
        Size(width, height),
        CornerRadius(12f, 12f)
    )
    drawRoundRect(
        Color(0xFF5A4030),
        Offset(left, top - height),
        Size(width, height),
        CornerRadius(12f, 12f),
        style = Stroke(width = 2f)
    )
    val path = Path().apply {
        moveTo(cx - 8f, top)
        lineTo(cx, top + 10f)
        lineTo(cx + 8f, top)
        close()
    }
    drawPath(path, Color(0xF0FFF8E7))
    drawContext.canvas.nativeCanvas.drawText(
        text,
        left + width / 2f,
        top - 12f,
        paint.apply { color = android.graphics.Color.parseColor("#2A1A10") }
    )
}
