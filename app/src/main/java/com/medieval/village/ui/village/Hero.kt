package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import kotlin.math.sin

/** Kenney Tiny Dungeon 히어로 스프라이트 (실제 PNG 타일). */
fun DrawScope.drawHero(
    atlas: KenneyAtlas,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    val bob = if (walking) sin(animTime * 14f) * 4f else sin(animTime * 2.4f) * 2f
    val size = WORLD_TILE * 2.1f
    drawKenneySprite(
        sheet = atlas.dungeon,
        tileId = DungeonTiles.HERO,
        cx = x,
        footY = y,
        size = size,
        bob = bob,
        mirrorX = facing == Facing.LEFT,
    )
}
