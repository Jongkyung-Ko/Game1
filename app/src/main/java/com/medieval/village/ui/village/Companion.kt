package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing
import com.medieval.village.model.Mercenary

/** 용병: 캐릭터 시트의 직업 스프라이트를 그대로 사용 (합성 없음). */
fun DrawScope.drawMercenary(
    art: CustomArt,
    mercenary: Mercenary,
    x: Float,
    y: Float,
    facing: Facing,
    @Suppress("UNUSED_PARAMETER") walking: Boolean = false,
    @Suppress("UNUSED_PARAMETER") animTime: Float = 0f,
) {
    drawCustomSprite(
        image = art.mercSprite(mercenary.role),
        cx = x,
        footY = y,
        worldHeight = 70f,
        mirrorX = facing == Facing.LEFT,
    )
}
