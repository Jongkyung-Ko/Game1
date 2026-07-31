package com.medieval.village.ui.place

import androidx.compose.ui.geometry.Offset
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
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawHero
import com.medieval.village.ui.village.drawKenneySprite
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawMercenary
import kotlin.math.sin

/** 실내 상단: Kenney 타일 배경 + 주인공/용병/NPC 스프라이트. */
fun DrawScope.drawInterior(
    atlas: KenneyAtlas,
    id: PlaceId,
    w: Float,
    h: Float,
    companions: List<Mercenary> = emptyList(),
    animTime: Float = 0f,
    speechNpcId: String? = null,
    speechText: String? = null,
) {
    drawInteriorBackground(atlas, id, w, h)

    val npcs = InteriorNpcCatalog.forPlace(id)
    npcs.forEachIndexed { index, npc ->
        val tile = npcTile(npc)
        val bob = sin(animTime * 2.6f + index) * 2.5f
        val wave = sin(animTime * 3.2f + index * 0.8f)
        drawKenneySprite(
            sheet = atlas.dungeon,
            tileId = tile,
            cx = w * npc.fx,
            footY = h * npc.fy,
            size = h * 0.38f,
            bob = bob + if (wave > 0.7f) -2f else 0f,
            mirrorX = false,
        )
        if (speechNpcId == npc.id && !speechText.isNullOrBlank()) {
            drawSpeechBubble(w * npc.fx, h * npc.fy - h * 0.32f, speechText, w)
        }
    }

    companions.forEachIndexed { index, mercenary ->
        drawMercenary(
            atlas = atlas,
            mercenary = mercenary,
            x = w * (0.30f + index * 0.12f),
            y = h * 0.90f,
            facing = Facing.RIGHT,
            walking = false,
            animTime = animTime + index * 0.35f,
        )
    }

    drawHero(
        atlas = atlas,
        x = w * 0.18f,
        y = h * 0.90f,
        facing = Facing.RIGHT,
        walking = false,
        animTime = animTime,
    )
}

private fun npcTile(npc: InteriorNpc): Int = when (npc.kind) {
    InteriorNpcKind.KEEPER -> when (npc.placeId) {
        PlaceId.SHOP, PlaceId.INN, PlaceId.PUB -> DungeonTiles.VILLAGER
        PlaceId.WEAPON_SHOP, PlaceId.BLACKSMITH -> DungeonTiles.HOODED
        PlaceId.HOSPITAL -> DungeonTiles.WOMAN
        PlaceId.CHURCH -> DungeonTiles.ELDER
        PlaceId.MAGIC_SCHOOL -> DungeonTiles.MAGE
        PlaceId.ARENA, PlaceId.MERCENARY -> DungeonTiles.KNIGHT_GOLD
        else -> DungeonTiles.VILLAGER
    }
    InteriorNpcKind.HELPER -> when (npc.placeId) {
        PlaceId.CHURCH -> DungeonTiles.WOMAN
        PlaceId.HOSPITAL -> DungeonTiles.YOUTH
        PlaceId.HOME -> DungeonTiles.YOUTH
        else -> DungeonTiles.VILLAGER
    }
    InteriorNpcKind.VISITOR -> DungeonTiles.KNIGHT_BLUE
}

private fun DrawScope.drawInteriorBackground(atlas: KenneyAtlas, id: PlaceId, w: Float, h: Float) {
    val tile = (h / 5f).coerceIn(28f, 56f)
    val cols = (w / tile).toInt() + 2
    val rows = (h / tile).toInt() + 2
    val floorY = (h * 0.58f / tile).toInt()

    val wallTile = when (id) {
        PlaceId.MAGIC_SCHOOL -> DungeonTiles.WALL
        PlaceId.BLACKSMITH -> TownTiles.STONE_C
        PlaceId.CHURCH, PlaceId.HOSPITAL -> TownTiles.WALL_GREY_M
        else -> TownTiles.WALL_TAN_M
    }
    val floorTile = when (id) {
        PlaceId.MAGIC_SCHOOL -> DungeonTiles.FLOOR
        PlaceId.ARENA, PlaceId.MERCENARY, PlaceId.DUNGEON -> TownTiles.PATH
        else -> TownTiles.PATH
    }
    val useDungeonWall = id == PlaceId.MAGIC_SCHOOL || id == PlaceId.DUNGEON

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val sheet = if (r < floorY) {
                if (useDungeonWall) atlas.dungeon else atlas.town
            } else {
                if (id == PlaceId.MAGIC_SCHOOL) atlas.dungeon else atlas.town
            }
            val tid = if (r < floorY) {
                if (useDungeonWall) DungeonTiles.WALL else wallTile
            } else {
                if (id == PlaceId.MAGIC_SCHOOL) DungeonTiles.FLOOR else floorTile
            }
            drawKenneyTile(sheet, tid, c * tile, r * tile, tile)
        }
    }

    // Props
    when (id) {
        PlaceId.SHOP, PlaceId.WEAPON_SHOP -> {
            drawKenneyTile(atlas.dungeon, DungeonTiles.CHEST, w * 0.55f, h * 0.35f, tile)
            drawKenneyTile(atlas.town, TownTiles.CRATE, w * 0.70f, h * 0.55f, tile)
            drawKenneyTile(atlas.town, TownTiles.BARREL, w * 0.82f, h * 0.55f, tile)
        }
        PlaceId.HOSPITAL -> {
            drawKenneyTile(atlas.dungeon, DungeonTiles.TABLE, w * 0.60f, h * 0.45f, tile)
            drawKenneyTile(atlas.town, TownTiles.SIGN, w * 0.45f, h * 0.20f, tile)
        }
        PlaceId.BLACKSMITH -> {
            drawKenneyTile(atlas.town, TownTiles.BARREL, w * 0.55f, h * 0.55f, tile)
            drawKenneyTile(atlas.dungeon, DungeonTiles.CHEST, w * 0.70f, h * 0.50f, tile)
        }
        PlaceId.CHURCH -> {
            drawKenneyTile(atlas.town, TownTiles.STONE_A, w * 0.60f, h * 0.30f, tile)
            drawKenneyTile(atlas.town, TownTiles.STONE_B, w * 0.72f, h * 0.30f, tile)
        }
        PlaceId.HOME, PlaceId.INN -> {
            drawKenneyTile(atlas.dungeon, DungeonTiles.TABLE, w * 0.58f, h * 0.48f, tile)
            drawKenneyTile(atlas.town, TownTiles.CRATE, w * 0.75f, h * 0.55f, tile)
        }
        PlaceId.ARENA -> {
            drawKenneyTile(atlas.town, TownTiles.TARGET, w * 0.65f, h * 0.45f, tile)
            drawKenneyTile(atlas.town, TownTiles.FENCE_H, w * 0.40f, h * 0.35f, tile)
        }
        PlaceId.MERCENARY -> {
            drawKenneyTile(atlas.town, TownTiles.ROOF_RED_M, w * 0.55f, h * 0.25f, tile * 1.2f)
            drawKenneyTile(atlas.town, TownTiles.BARREL, w * 0.70f, h * 0.55f, tile)
        }
        PlaceId.DUNGEON -> {
            drawKenneyTile(atlas.town, TownTiles.CASTLE_BM, w * 0.45f, h * 0.35f, tile * 1.4f)
            drawKenneyTile(atlas.town, TownTiles.CASTLE_BM, w * 0.60f, h * 0.35f, tile * 1.4f)
        }
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
        size = androidx.compose.ui.geometry.Size(width, 34f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color(0xFF2B2118),
        topLeft = Offset(left, cy - 26f),
        size = androidx.compose.ui.geometry.Size(width, 34f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
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
