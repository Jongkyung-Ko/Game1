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
import com.medieval.village.model.InteriorRoom
import com.medieval.village.model.Mercenary
import com.medieval.village.model.PlaceId
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawVillageFollowParty
import kotlin.math.sin

/**
 * 걸어다니는 실내를 InteriorRoom 월드 좌표계로 그린다.
 */
fun DrawScope.drawWalkableInterior(
    atlas: KenneyAtlas,
    art: CustomArt?,
    id: PlaceId,
    heroX: Float,
    heroY: Float,
    facing: Facing,
    walking: Boolean,
    walkPhase: Float,
    companions: List<Mercenary> = emptyList(),
    animTime: Float = 0f,
    speechNpcId: String? = null,
    speechText: String? = null,
) {
    val w = InteriorRoom.WORLD_W
    val h = InteriorRoom.WORLD_H
    drawInteriorBackground(atlas, id, w, h)
    drawInteriorFurniture(id)

    InteriorNpcCatalog.forPlace(id).forEachIndexed { index, npc ->
        val bob = sin(animTime * 2.6f + index) * 2f
        val cx = npc.worldX
        val footY = npc.worldY + bob
        val sprite = art?.npcSpriteOrNull(npcSpriteKey(npc))
        if (sprite != null) {
            drawCustomSprite(
                image = sprite,
                cx = cx,
                footY = footY,
                worldHeight = 112f,
            )
        } else {
            drawCleanNpcFallback(cx, footY, npc.kind)
        }
        drawLabelTiny("${npc.name} · ${npc.role}", cx - 55f, footY + 16f)
        when (npc.kind) {
            InteriorNpcKind.KEEPER -> drawLabelTiny("거래", cx - 18f, footY - 118f)
            InteriorNpcKind.VISITOR, InteriorNpcKind.HELPER ->
                drawLabelTiny("대화", cx - 18f, footY - 118f)
        }
        if (speechNpcId == npc.id && !speechText.isNullOrBlank()) {
            drawSpeechBubble(cx, footY - 128f, speechText, w)
        }
    }

    drawVillageFollowParty(
        heroX = heroX,
        heroY = heroY,
        facing = facing,
        walking = walking,
        walkPhase = walkPhase,
        mercs = companions,
        art = art,
        heroScale = 1.0f,
        mercScale = 0.92f,
    )
}

private fun DrawScope.drawCleanNpcFallback(cx: Float, footY: Float, kind: InteriorNpcKind) {
    val outfit = when (kind) {
        InteriorNpcKind.KEEPER -> Color(0xFF6B4A32)
        InteriorNpcKind.HELPER -> Color(0xFF4A6B58)
        InteriorNpcKind.VISITOR -> Color(0xFF4A5A78)
    }
    drawOval(Color(0x33000000), Offset(cx - 22f, footY - 6f), Size(44f, 14f))
    drawRoundRect(outfit, Offset(cx - 16f, footY - 70f), Size(32f, 52f), CornerRadius(8f, 8f))
    drawRoundRect(Color(0xFF3A2A1C), Offset(cx - 14f, footY - 24f), Size(11f, 24f), CornerRadius(3f, 3f))
    drawRoundRect(Color(0xFF3A2A1C), Offset(cx + 3f, footY - 24f), Size(11f, 24f), CornerRadius(3f, 3f))
    drawCircle(Color(0xFFE7B98F), 14f, Offset(cx, footY - 82f))
    drawArc(
        Color(0xFF4A3324),
        180f,
        180f,
        true,
        Offset(cx - 15f, footY - 96f),
        Size(30f, 22f)
    )
}

private fun DrawScope.drawLabelTiny(text: String, x: Float, y: Float) {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#F3E4C5")
        textSize = 16f
        isFakeBoldText = true
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun npcSpriteKey(npc: InteriorNpc): String =
    npc.spriteKey ?: when (npc.placeId) {
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
        PlaceId.EAST_FOREST -> "rogue"
        PlaceId.SOUTH_DESERT -> "rogue"
        PlaceId.NORTH_GLACIER -> "mage"
    }

private fun DrawScope.drawInteriorBackground(atlas: KenneyAtlas, id: PlaceId, w: Float, h: Float) {
    val tile = 80f
    drawRect(Color(0xFF2B1C12), Offset.Zero, Size(w, h))
    drawRect(Color(0xFF3E2A1C), Offset(0f, 0f), Size(w, h * 0.36f))
    for (c in 0 until 14) {
        drawKenneyTile(atlas.dungeon, DungeonTiles.WALL, c * tile - tile * 0.2f, h * 0.28f, tile)
    }
    for (r in 0 until 7) {
        for (c in 0 until 14) {
            drawKenneyTile(
                atlas.town,
                TownTiles.PATH,
                c * tile - tile * 0.1f,
                h * 0.40f + r * tile * 0.85f,
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
        textSize = 24f
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
