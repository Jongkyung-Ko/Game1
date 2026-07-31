package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.model.Mercenary
import kotlin.math.sin

/** Kenney Tiny Dungeon 용병 스프라이트. */
fun DrawScope.drawMercenary(
    atlas: KenneyAtlas,
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    val tile = when (mercenary.role) {
        "검사" -> DungeonTiles.KNIGHT_BLUE
        "궁수" -> DungeonTiles.KNIGHT_GOLD
        "방패병" -> DungeonTiles.KNIGHT_RED
        else -> DungeonTiles.MAGE
    }
    val bob = if (walking) sin(animTime * 13f) * 2.5f else sin(animTime * 2.2f + 1f) * 1.2f
    drawKenneySprite(
        sheet = atlas.dungeon,
        tileId = tile,
        cx = x,
        footY = y,
        size = WORLD_TILE * 1.2f,
        bob = bob,
        mirrorX = facing == Facing.LEFT,
    )
}
