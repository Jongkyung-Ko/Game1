package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.model.Mercenary
import kotlin.math.sin

fun DrawScope.drawMercenary(
    atlas: KenneyAtlas,
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    val name = when (mercenary.role) {
        "검사" -> "knight_b"
        "궁수" -> "knight_g"
        "방패병" -> "knight_r"
        else -> "mage"
    }
    val bob = if (walking) sin(animTime * 13f) * 4f else sin(animTime * 2.2f + 1f) * 2f
    drawKenneySpriteAsset(
        image = atlas.sprite(name),
        cx = x,
        footY = y,
        worldHeight = WORLD_TILE * 2.35f,
        bob = bob,
        mirrorX = facing == Facing.LEFT,
    )
}
