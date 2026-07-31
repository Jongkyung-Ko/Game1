package com.medieval.village.ui.place

import androidx.compose.ui.geometry.CornerRadius
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
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawCustomHero
import com.medieval.village.ui.village.drawKenneySpriteAsset
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawMercenary
import kotlin.math.sin

fun DrawScope.drawInterior(
    atlas: KenneyAtlas,
    art: CustomArt,
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
        val bob = sin(animTime * 2.6f + index) * 3f
        val wave = sin(animTime * 3.2f + index * 0.8f)
        drawKenneySpriteAsset(
            image = atlas.sprite(npcSprite(npc)),
            cx = w * npc.fx,
            footY = h * npc.fy,
            worldHeight = h * 0.52f,
            bob = bob + if (wave > 0.65f) -3f else 0f,
        )
        if (speechNpcId == npc.id && !speechText.isNullOrBlank()) {
            drawSpeechBubble(w * npc.fx, h * npc.fy - h * 0.34f, speechText, w)
        }
    }

    companions.forEachIndexed { index, mercenary ->
        drawMercenary(
            atlas, mercenary,
            w * (0.30f + index * 0.12f), h * 0.90f,
            Facing.RIGHT, false, animTime + index * 0.35f
        )
    }
    drawCustomHero(art, w * 0.18f, h * 0.90f, Facing.RIGHT, worldHeight = h * 0.55f)
}

private fun npcSprite(npc: InteriorNpc): String = when (npc.kind) {
    InteriorNpcKind.KEEPER -> when (npc.placeId) {
        PlaceId.SHOP, PlaceId.INN, PlaceId.PUB -> "villager"
        PlaceId.WEAPON_SHOP, PlaceId.BLACKSMITH -> "elder"
        PlaceId.HOSPITAL -> "woman"
        PlaceId.CHURCH -> "elder"
        PlaceId.MAGIC_SCHOOL -> "mage"
        PlaceId.ARENA, PlaceId.MERCENARY -> "knight_g"
        else -> "villager"
    }
    InteriorNpcKind.HELPER -> when (npc.placeId) {
        PlaceId.CHURCH, PlaceId.HOSPITAL -> "woman"
        PlaceId.HOME -> "youth"
        else -> "villager"
    }
    InteriorNpcKind.VISITOR -> "knight_b"
}

private fun DrawScope.drawInteriorBackground(atlas: KenneyAtlas, id: PlaceId, w: Float, h: Float) {
    val tile = (h / 5f).coerceIn(28f, 56f)
    val cols = (w / tile).toInt() + 2
    val rows = (h / tile).toInt() + 2
    val floorY = (h * 0.58f / tile).toInt()
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (r < floorY) {
                val sheet = if (id == PlaceId.MAGIC_SCHOOL || id == PlaceId.DUNGEON) atlas.dungeon else atlas.town
                val tid = if (id == PlaceId.MAGIC_SCHOOL || id == PlaceId.DUNGEON) DungeonTiles.WALL else 73
                drawKenneyTile(sheet, tid, c * tile, r * tile, tile)
            } else {
                drawKenneyTile(atlas.town, TownTiles.PATH, c * tile, r * tile, tile)
            }
        }
    }
    // props
    when (id) {
        PlaceId.SHOP, PlaceId.WEAPON_SHOP -> {
            drawKenneySpriteAsset(atlas.sprite("crate"), w * 0.62f, h * 0.70f, tile * 1.2f)
            drawKenneySpriteAsset(atlas.sprite("basket"), w * 0.78f, h * 0.70f, tile * 1.2f)
        }
        PlaceId.HOSPITAL, PlaceId.HOME, PlaceId.INN -> {
            drawKenneyTile(atlas.dungeon, DungeonTiles.TABLE, w * 0.60f, h * 0.45f, tile)
        }
        PlaceId.ARENA -> drawKenneySpriteAsset(atlas.sprite("target"), w * 0.68f, h * 0.65f, tile * 1.4f)
        PlaceId.BLACKSMITH -> drawKenneySpriteAsset(atlas.sprite("crate"), w * 0.65f, h * 0.70f, tile * 1.2f)
        else -> Unit
    }
}

private fun DrawScope.drawSpeechBubble(cx: Float, cy: Float, text: String, canvasW: Float) {
    val paint = android.graphics.Paint().apply {
        color = Color(0xFF1B120A).toArgb()
        textSize = 20f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val width = (paint.measureText(text) + 24f).coerceIn(70f, canvasW * 0.55f)
    val left = (cx - width / 2f).coerceIn(6f, canvasW - width - 6f)
    drawRoundRect(
        color = Color(0xFFF7EFD8),
        topLeft = Offset(left, cy - 26f),
        size = Size(width, 34f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color(0xFF2B2118),
        topLeft = Offset(left, cy - 26f),
        size = Size(width, 34f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(2.5f)
    )
    val tip = Path().apply {
        moveTo(cx - 7f, cy + 6f)
        lineTo(cx, cy + 14f)
        lineTo(cx + 7f, cy + 6f)
        close()
    }
    drawPath(tip, Color(0xFFF7EFD8))
    drawContext.canvas.nativeCanvas.drawText(text, left + width / 2f, cy - 4f, paint)
}
