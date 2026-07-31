package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import kotlin.math.sin

fun DrawScope.drawHero(
    atlas: KenneyAtlas,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    val bob = if (walking) sin(animTime * 14f) * 5f else sin(animTime * 2.4f) * 2.5f
    drawKenneySpriteAsset(
        image = atlas.sprite("hero"),
        cx = x,
        footY = y,
        worldHeight = WORLD_TILE * 2.6f,
        bob = bob,
        mirrorX = facing == Facing.LEFT,
    )
}
