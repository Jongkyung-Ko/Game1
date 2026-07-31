package com.medieval.village.ui.village

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.model.Mercenary

/** Kenney A스타일 용병 스프라이트. 역할별 튜닉 색이 다르다. */
fun DrawScope.drawMercenary(
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    val tunic = when (mercenary.role) {
        "검사" -> Color(0xFF496A8A)
        "궁수" -> Color(0xFF4E753F)
        "방패병" -> Color(0xFF777D86)
        else -> Color(0xFF684A8F)
    }
    drawKenneyMerc(
        cx = x,
        cy = y - 14f,
        walking = walking,
        facingRight = facing != Facing.LEFT,
        t = animTime,
        tunic = tunic,
        scale = 1.35f,
    )
}
