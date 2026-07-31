package com.medieval.village.ui.village

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.medieval.village.game.Facing

/**
 * Kenney A스타일 청크 모험가 스프라이트.
 * 갈색 튜닉 + 가죽 조끼 + 녹색 망토 + 허리 검.
 * @param animTime 초 단위 누적 시간 (걷기/대기 애니메이션 공용)
 */
fun DrawScope.drawHero(
    x: Float,
    y: Float,
    facing: Facing,
    walking: Boolean,
    animTime: Float
) {
    drawKenneyHero(
        cx = x,
        cy = y - 18f,
        walking = walking,
        facingRight = facing != Facing.LEFT,
        t = animTime,
        scale = 1.55f,
    )
}
