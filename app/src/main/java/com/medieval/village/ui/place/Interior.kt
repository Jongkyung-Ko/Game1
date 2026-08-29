package com.medieval.village.ui.place

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.medieval.village.game.Facing
import com.medieval.village.game.PartyDrawSlot
import com.medieval.village.model.InteriorNpc
import com.medieval.village.model.InteriorNpcCatalog
import com.medieval.village.model.InteriorNpcKind
import com.medieval.village.model.InteriorRoom
import com.medieval.village.model.Mercenary
import com.medieval.village.model.PlaceId
import com.medieval.village.model.SettlementId
import com.medieval.village.model.WorldFlags
import com.medieval.village.ui.village.CustomArt
import com.medieval.village.ui.village.DungeonTiles
import com.medieval.village.ui.village.KenneyAtlas
import com.medieval.village.ui.village.TownTiles
import com.medieval.village.ui.village.drawCustomSprite
import com.medieval.village.ui.village.drawKenneyTile
import com.medieval.village.ui.village.drawLevelUpBurst
import com.medieval.village.ui.village.drawVillageFollowParty
import kotlin.math.roundToInt
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
    frontIndex: Int = 0,
    partySlots: List<PartyDrawSlot>? = null,
    settlementId: SettlementId = SettlementId.OAKHAVEN,
    flags: WorldFlags = WorldFlags(),
    levelUpFxActorKey: String? = null,
    levelUpFxUntil: Float = 0f,
) {
    val w = InteriorRoom.WORLD_W
    val h = InteriorRoom.WORLD_H
    val hasCartoonRoom = drawInteriorBackground(atlas, art, id, w, h)
    // 풀룸 일러스트에 가구가 이미 들어가 있으면 벡터 가구는 생략
    if (!hasCartoonRoom) {
        drawInteriorFurniture(id)
    }

    InteriorNpcCatalog.forPlace(id, settlementId, flags).forEachIndexed { index, npc ->
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
        heroScale = 1.05f,
        mercScale = 0.74f,
        frontIndex = frontIndex,
        slots = partySlots,
    )
    if (levelUpFxActorKey != null) {
        val slots = partySlots.orEmpty()
        val slot = slots.firstOrNull { it.actorKey == levelUpFxActorKey }
            ?: slots.firstOrNull()
            ?: PartyDrawSlot(null, heroX, heroY, facing, true)
        val rem = (levelUpFxUntil - animTime).coerceAtLeast(0f)
        val progress = (1f - rem / 2f).coerceIn(0f, 1f)
        drawLevelUpBurst(slot.x, slot.y, progress, animTime)
    }
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
        PlaceId.GRAY_CASTLE -> "paladin"
        PlaceId.IGLOO_GLACIER -> "mage"
        PlaceId.SEA_CAVE -> "rogue"
        PlaceId.WINTER_KEEP -> "warrior"
        PlaceId.EAST_FOREST -> "rogue"
        PlaceId.SOUTH_DESERT -> "rogue"
        PlaceId.NORTH_GLACIER -> "mage"
    }

/** @return 카툰 풀룸 배경을 그렸으면 true (가구 오버레이 생략용) */
private fun DrawScope.drawInteriorBackground(
    atlas: KenneyAtlas,
    art: CustomArt?,
    id: PlaceId,
    w: Float,
    h: Float,
): Boolean {
    val interiorKey = when (id) {
        PlaceId.HOME -> "home"
        PlaceId.SHOP -> "shop"
        PlaceId.WEAPON_SHOP -> "weapon_shop"
        PlaceId.INN -> "inn"
        PlaceId.HOSPITAL -> "hospital"
        PlaceId.CHURCH -> "church"
        PlaceId.BLACKSMITH -> "blacksmith"
        PlaceId.MAGIC_SCHOOL -> "magic_school"
        PlaceId.ARENA -> "arena"
        PlaceId.MERCENARY -> "mercenary"
        PlaceId.PUB -> "pub"
        else -> null
    }
    val roomArt = interiorKey?.let { art?.interiorOrNull(it) }
    if (roomArt != null) {
        drawImage(
            image = roomArt,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(roomArt.width, roomArt.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(w.roundToInt(), h.roundToInt()),
            filterQuality = FilterQuality.Medium,
        )
        // 살짝 가장자리로 걷이도록 살짝 비네트
        drawRect(Color(0x22000000), Offset.Zero, Size(w, h * 0.08f))
        drawRect(Color(0x33000000), Offset(0f, h * 0.92f), Size(w, h * 0.08f))
        return true
    }

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
    return false
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
