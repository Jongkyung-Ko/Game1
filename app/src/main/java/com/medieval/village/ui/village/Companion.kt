package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.model.Mercenary

fun DrawScope.drawMercenary(
    atlas: KenneyAtlas,
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    @Suppress("UNUSED_PARAMETER") walking: Boolean,
    @Suppress("UNUSED_PARAMETER") animTime: Float
) {
    val name = when (mercenary.role) {
        "검사" -> "knight_b"
        "궁수" -> "knight_g"
        "방패병" -> "knight_r"
        else -> "mage"
    }
    drawKenneySpriteAsset(
        image = atlas.sprite(name),
        cx = x,
        footY = y,
        worldHeight = WORLD_TILE * 2.0f,
        bob = 0f,
        mirrorX = facing == Facing.LEFT,
    )
}
